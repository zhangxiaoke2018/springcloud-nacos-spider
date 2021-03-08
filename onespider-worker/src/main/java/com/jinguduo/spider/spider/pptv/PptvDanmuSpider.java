package com.jinguduo.spider.spider.pptv;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.spider.listener.UserAgentSpiderListener;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.constant.FrequencyConstant;
import com.jinguduo.spider.common.exception.AntiSpiderException;
import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.data.text.BarrageText;
import com.jinguduo.spider.webmagic.Page;

@Worker
public class PptvDanmuSpider extends CrawlSpider {

    private static Logger log = LoggerFactory.getLogger(PptvDanmuSpider.class);

    private final static int INCREASE_SCOPE = 1000;//请求增长区间

    private final static String URL = "http://apicdn.danmu.pptv.com/danmu/v2/pplive/ref/vod_25417273/danmu?pos=0";

    private Site site = SiteBuilder.builder()
            .setDomain("apicdn.danmu.pptv.com")
            .setCharset("UTF-8")
            .addSpiderListener(new UserAgentSpiderListener())
            .build();

    PageRule rule = PageRule.build()
            .add(".", page -> contents(page));

    /***
     * 获取弹幕文本
     * play_point参数代表时间，play_point=10代表一秒
     * @param page
     * @throws AntiSpiderException 
     */
    private void contents(Page page) throws AntiSpiderException {
        Job old = ((DelayRequest) page.getRequest()).getJob();
        try {
            //过滤返回结果为无效用户
            if(page.getRawText().contains("Unauthorized")||page.getRawText().contains("Authentication")){
                return;
            }
            JSONObject json = page.getJson().toObject(JSONObject.class);
            
            JSONObject data = json.getJSONObject("data");
            
            List<JSONObject> list = (List) data.getJSONArray("infos");
            
            if (CollectionUtils.isNotEmpty(list)){
                List<BarrageText> bs = Lists.newArrayListWithCapacity(list.size());
                list.stream().forEach(j -> analysis(page,bs,old,j));
                //insert
                putModel(page, bs);
            }
            next(page, old, list);
            
        } catch (JSONException e) {
            // 被服务端拒绝访问后，返回html页面，导致json解析异常
            throw new AntiSpiderException(old.getUrl());
        }
        
    }

    private void analysis(Page page, List<BarrageText> bs, Job old, JSONObject j) {
        long precision = 10L;
        BarrageText barrageText = new BarrageText(
                j.getString("id"),
                j.getString("user_name"),
                j.getString("nick_name"),
                j.getLong("play_point") / precision,//秒数  play_point/10 = second
                j.getTimestamp("createtime"),
                j.getIntValue("upCount"),
                j.getString("content")
        );
        DbEntityHelper.derive(old,barrageText);
        bs.add(barrageText);
    }

    private final static int MAX_SCOPE = 1 * 60 * 60;
    
    private void next(Page page, Job old, List<JSONObject> list) {
        try {
            //当前进度
            int current = Integer.valueOf(page.getUrl().regex("\\?pos\\=(\\d*)",1).get());
            
            // 跳过Job生成: 时间超出一小时，且弹幕文本为空
            // BadCase: http://apicdn.danmu.pptv.com/danmu/v2/pplive/ref/vod_16738283/danmu?pos=184000
            if (current >= MAX_SCOPE && (list == null || list.isEmpty())) {
                return;
            }

            List<Job> jobs = Lists.newArrayList();
            //计算下一个任务的进度
            final int next_scope = INCREASE_SCOPE + current;
            //创建递归任务
            final String nextUrl = page.getUrl().replace("pos\\=(\\d*)", String.format("pos=%s",next_scope)).get();
            //Db
            Job newJob = DbEntityHelper.deriveNewJob(old, nextUrl);
            newJob.setFrequency(FrequencyConstant.BARRAGE_TEXT);
            jobs.add(newJob);

            Job newJob2 = DbEntityHelper.deriveNewJob(
                    old,
                    page.getUrl().replace("pos\\=(\\d*)", String.format("pos=%s", INCREASE_SCOPE + next_scope)).get()
            );
            newJob2.setFrequency(FrequencyConstant.BARRAGE_TEXT);
            jobs.add(newJob2);

            putModel(page,jobs);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
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
