package com.jinguduo.spider.spider.mgtv;


import com.alibaba.fastjson.JSONObject;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.exception.AntiSpiderException;
import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.data.table.ShowLog;
import com.jinguduo.spider.webmagic.Page;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.lang3.StringUtils;

import org.apache.http.client.config.CookieSpecs;


/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @DATE 16/6/24 下午6:15
 */
@Worker
@CommonsLog
public class MgtvPlayCountSpider extends CrawlSpider {

    private Site site = SiteBuilder.builder()
    		.setDomain("videocenter-2039197532.cn-north-1.elb.amazonaws.com.cn")
    		.setCookieSpecs(CookieSpecs.IGNORE_COOKIES)
    		.build();
    
    private PageRule rule = PageRule.build().add("", page -> processPage(page));

    public void processPage(Page page) {

        String text = page.getRawText();

        if(StringUtils.isBlank(text)){
            throw new AntiSpiderException("芒果播放量抓取异常 -> " + page.getRequest().getUrl());
        }

        JSONObject data = JSONObject.parseObject(text.substring(text.indexOf("(") + 1, text.length() - 1)).getJSONObject("data");

        Long playCount = data.getLong("allVV");
        ShowLog showLog = new ShowLog();
        Job job = ((DelayRequest) page.getRequest()).getJob();
        if (job != null) {
            DbEntityHelper.derive(job, showLog);
            showLog.setPlayCount(playCount);
            showLog.setCode(job.getCode());
            //log.info("芒果播放量 code ------->"+job.getCode()+"-----播放量-------->"+playCount+"。");
        }
        putModel(page,showLog);

    }

    @Override
    public Site getSite() {
        return this.site;
    }

    @Override
    public PageRule getPageRule() {
        return this.rule;
    }
}
