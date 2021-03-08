package com.jinguduo.spider.spider.iqiyi;

import com.google.common.collect.Lists;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.constant.FrequencyConstant;
import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.data.text.BarrageText;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.selector.Html;
import com.jinguduo.spider.webmagic.selector.Selectable;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.List;

@Worker
public class IqiyiBarrageSpider extends CrawlSpider {

    private static Logger log = LoggerFactory.getLogger(IqiyiBarrageSpider.class);

    private final static String BARRAGE_SUCCESS_STATUS = "A00000";

    private final static Integer INCREASE_SCOPE = 1;//请求增长区间

    private Site site = SiteBuilder.builder()
            .setDomain("cmts.iqiyi.com")
            .setCharset("UTF-8")
            .setPageHandler(new IqiyiBarrageSpiderPageHandler())
            .build();

    private PageRule rule = PageRule.build()
            .add("cmts\\.iqiyi\\.com/bullet/", page -> content(page));

    /***
     * getContent : http://cmts.iqiyi.com/bullet/70/00/583507000_300_1.z?rn=0.21184565359726548&business=danmu&is_iqiyi=true&is_video_page=true&tvid=583507000&albumid=205221401&categoryid=2&qypid=01010011010000000000
     * 
     * wget 'http://cmts.iqiyi.com/bullet/70/00/583507000_300_1.z?rn=0.21184565359726548&business=danmu&is_iqiyi=true&is_video_page=true&tvid=583507000&albumid=205221401&categoryid=2&qypid=01010011010000000000' -U '-' -O - 2>/dev/null | pigz -d -z | less
     * 
     * @param page
     */
    private void content(Page page) {

        log.debug("begin capture Iqiyi danmu,url = {}",page.getUrl().get());

        Job old = ((DelayRequest) page.getRequest()).getJob();

        try {
            Html html = page.getHtml();

            if (!BARRAGE_SUCCESS_STATUS.equals(html.xpath("//danmu//code/text()").get())){
                log.debug("barrage return unknow status : {}", html.xpath("//danmu//code/text()").get());
                return;
            }

            List<Selectable> bulletInfos = html.xpath("//bulletInfo").nodes();

            bulletInfos.stream().forEach(bullet -> {

                try {
                    BarrageText btext= new BarrageText();

                    String barrageId =  bullet.xpath("///contentid/text()").get();//弹幕ID

                    if (StringUtils.isBlank(barrageId)) {
                        log.warn("iqiyi barrageId return is null by xml : {}, url : {}", bullet.get(), page.getUrl());
                        return;
                    }
                    btext.setBarrageId(barrageId);
                    btext.setContent(filter(bullet.xpath("///content/text()").get(),""));
                    btext.setNickName(filter(bullet.xpath("///userinfo/name/text()").get(),""));
                    btext.setUserId(filter(bullet.xpath("///userinfo/uid/text()").get(),""));
                    btext.setIsReplay(Boolean.valueOf(filter(bullet.xpath("///isreply/text()").get(), "false")));
                    btext.setShowTime(Long.parseLong(filter(bullet.xpath("///showtime/text()").get(), "0")));
                    btext.setCreatedTime(parseCreatedTime(barrageId));

                    putModel(
                            page,
                            DbEntityHelper.derive(old, btext)
                    );
                } catch (Exception e) {
                    throw e;
                }
            });

            //当前进度
            int current = Integer.valueOf(page.getUrl().regex("\\_300\\_(\\d*)",1).get());

            // 跳过Job生成: 时间超出一小时，且弹幕文本为空
            if (current >= MAX_SCOPE && CollectionUtils.isEmpty(bulletInfos)) {
                return;
            }

            //生成下一个任务
            createNextJob(page,old);

        } catch (Exception e) {
            log.error("iqiyi barrage content process fail by url {}", page.getUrl(), e);
        }
    }

    private String filter (String target, String defaultE) {
        try {
            if (StringUtils.isNotBlank(target)) {
                target = target.trim();//replaceAll
            }
            if (StringUtils.isBlank(target)) {
                target = defaultE;
            }
        } catch (Exception e) {
            target = defaultE;
            log.error("{} filter error :", target, e);
        }
        return target;
    }

    private final static int MAX_SCOPE = 10;
    
    /**
     * 生成下一个任务
     */
    private void createNextJob(Page page, Job job) {
            //当前进度
            int current = Integer.valueOf(page.getUrl().regex("\\_300\\_(\\d*)",1).get());

            List<Job> jobs = Lists.newArrayList();

            //计算下一个任务的进度
            final int next_scope = INCREASE_SCOPE + current;

            //创建递归任务
            final String nextUrl = page.getUrl().replace("\\_300\\_(\\d*)", String.format("\\_300\\_%s",next_scope)).get();
            Job newJob = DbEntityHelper.deriveNewJob(job,nextUrl);
            newJob.setFrequency(FrequencyConstant.BARRAGE_TEXT);
            jobs.add(newJob);

            Job newJob2 = DbEntityHelper.deriveNewJob(
                    job,
                    page.getUrl().replace("\\_300\\_(\\d*)", String.format("\\_300\\_%s", INCREASE_SCOPE + INCREASE_SCOPE + current)).get());
            newJob2.setFrequency(FrequencyConstant.BARRAGE_TEXT);

            jobs.add(newJob2);
            putModel(page, jobs);

    }

    /**
     * 从id中解析出弹幕文本创建日期
     * @param id
     * @return
     */
    private Timestamp parseCreatedTime(String id) {
        if (id == null || id.length() < 13) {
            return null;
        }
        try {
            String s = id.substring(0, 13);
            long time = Long.parseLong(s);
            return new Timestamp(time);
        } catch (NumberFormatException e) {
            // ignore
            log.warn(e.getMessage() + ":" + id);
        }
        return null;
    }

    @Override
    public PageRule getPageRule() {
        return rule;
    }

    /**
     * get the site settings
     *
     * @return site
     * @see Site
     */
    @Override
    public Site getSite() {
        return site;
    }
}
