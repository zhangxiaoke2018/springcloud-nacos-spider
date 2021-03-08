package com.jinguduo.spider.spider.youku;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.constant.FrequencyConstant;
import com.jinguduo.spider.common.exception.AntiSpiderException;
import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.data.table.BarrageLog;
import com.jinguduo.spider.data.text.BarrageText;
import com.jinguduo.spider.webmagic.Page;

//@Worker
public class YoukuDanmuSpider extends CrawlSpider {

    private final int INCREASE_SCOPE = 1;//请求增长区间

    private Site site = SiteBuilder.builder()
            .setDomain("service.danmu.youku.com")
            .setCharset("UTF-8")
            .build();

      private PageRule  rules = PageRule.build()
            .add("/pool/",page -> proccessShowPlayCount(page) )//获取网页总播放量
            .add("\\/list",page -> getDanmu(page));//获取弹幕文本


    /**
     * http://service.danmu.youku.com/list?iid=471618915&ct=1001&cid=97&type=1&aid=307573&lid=0&mcount=1&uid=0&ouid=1066650389&mat=1
     * mat参数控制分页，以1为增量递增
     *
     * playat表示播放时间，1000为1秒
     */

    private void getDanmu(Page page) {
        Job oldJob = ((DelayRequest) page.getRequest()).getJob();
            JSONObject jsonStr = JSON.parseObject(page.getRawText());
            List<JSONObject> list = (List) jsonStr.getJSONArray("result");
            if (!CollectionUtils.isEmpty(list)) {
                List<BarrageText> danmakuLogsList = Lists.newArrayListWithCapacity(list.size());
                list.stream().forEach(j -> analysis(page, danmakuLogsList, oldJob, j));
                putModel(page, danmakuLogsList);
            }
            next(page, oldJob, list);
    }
    
    private final static int MAX_SCOPE = 1 * 60;

    private void next(Page page, Job oldJob, List<JSONObject> list) {
            //当前进度
            int current = Integer.valueOf(page.getUrl().regex("\\&mat\\=(\\d*)",1).get());
            
            // 跳过Job生成: 时间超出一小时，且弹幕文本为空
            // BadCase: http://service.danmu.youku.com/list?iid=466940818&ct=1001&type=1&mcount=5&mat=156
            if (current >= MAX_SCOPE && (list == null || list.isEmpty())) {
                return;
            }
            List<Job> jobs = Lists.newArrayList();
            //计算下一个任务的进度
            final int next_scope = INCREASE_SCOPE + current;
            //创建递归任务
            final String nextUrl = page.getUrl().replace("mat\\=(\\d*)", String.format("mat=%s",next_scope)).get();
            //Db
            Job newJob = DbEntityHelper.deriveNewJob(oldJob, nextUrl);
            newJob.setFrequency(FrequencyConstant.BARRAGE_TEXT);
            jobs.add(newJob);

            putModel(page,jobs);
    }

    private void analysis(Page page, List<BarrageText> danmakuLogsList, Job oldJob, JSONObject j) {
        long precision = 1000;
            BarrageText barrageText = new BarrageText(
                    j.getString("id"),
                    j.getString("uid"),
                    "",
                    j.getLong("playat") / precision,
                    j.getTimestamp("createtime"),
                    0,
                    j.getString("content")
            );
            DbEntityHelper.derive(oldJob,barrageText);
            danmakuLogsList.add(barrageText);
    }

    private void proccessShowPlayCount(Page page) {
        if(null == page)
            return ;
        Job oldjob = ((DelayRequest)page.getRequest()).getJob();
        JSONObject document = page.getJson().toObject(JSONObject.class);
        Integer playCount = document.getInteger("count");
        BarrageLog barrageLog = new BarrageLog(playCount);
        DbEntityHelper.derive(oldjob,barrageLog);
        putModel(page,barrageLog);
    }

    @Override
    public PageRule getPageRule() {
        return rules;
    }

    @Override
    public Site getSite() {
        return this.site;
    }
}
