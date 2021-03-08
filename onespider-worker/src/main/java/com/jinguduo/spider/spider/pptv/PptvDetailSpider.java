package com.jinguduo.spider.spider.pptv;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.config.CookieSpecs;

import com.alibaba.fastjson.JSONObject;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.webmagic.Page;

import lombok.extern.apachecommons.CommonsLog;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 */
@Worker
@CommonsLog
public class PptvDetailSpider extends CrawlSpider {

    private Site site = SiteBuilder
            .builder()
            .setDomain("v.pptv.com")
            .setCookieSpecs(CookieSpecs.IGNORE_COOKIES)
            .setUserAgent("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/52.0.2725.0 Safari/537.36")
            .build();

    private final static String PREFIX = "var webcfg =";
    //人为添加参数subId(主要用于电影合集中对应code的电影定位)
    private final static String SHOW_LIST_URL = "http://epg.api.pptv.com/detail.api?cb=recDetailData&platform=android3&auth=%s&vid=%s&subId=%s";

    private PageRule rules = PageRule.build()
            .add("pptv", page -> createShowListJob(page));//根据vid及subId通过api获取json格式的剧集列表任务
    
    /**
     * 解析script块
     * 获取该剧的vid和subId生成剧集信息api爬虫任务
     * 
     * @param page
     * @param script
     */
    private void createShowListJob(Page page) {
        Job oldJob = ((DelayRequest) page.getRequest()).getJob();
        
        String text = page.getRawText();
        final int preIndexOf = text.indexOf(PREFIX);
        if(preIndexOf <= 0){
            log.error("pptvDetailSpider createShowListJob can't get script info! url:"+oldJob.getUrl());
            return;
        }
        //去除多余字符及末尾分号，仅保留json格式信息
        String s = text.substring(preIndexOf + PREFIX.length(), text.indexOf("\n", preIndexOf) - 1);
        
        JSONObject json = JSONObject.parseObject(s);
        String vid = json.getString("pid");
        String subId = json.getString("id");
        if (vid == null || "0".equals(vid)) {
            vid = subId;
        }
        if (StringUtils.isBlank(vid)) {
            log.error("pptvDetailSpider createShowListJob get empty vid! url:"+oldJob.getUrl());
            return;
        }
        
        //json格式剧集信息接口:http://epg.api.pptv.com/detail.api?cb=recDetailData&platform=android3&auth=pVozsFYSC0msKpI&vid=9042526&subId=25868709
        Job jsonShowJob = new Job(String.format(SHOW_LIST_URL, oldJob.getCode(), vid, subId));
        DbEntityHelper.derive(oldJob, jsonShowJob);
        jsonShowJob.setCode(oldJob.getCode());
        putModel(page, jsonShowJob);
    }
    
    @Override
    public Site getSite() {
        return this.site;
    }

    @Override
    public PageRule getPageRule() {
        return this.rules;
    }
}
