package com.jinguduo.spider.spider.mgtv;


import com.alibaba.fastjson.JSONObject;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.data.table.ShowLog;
import com.jinguduo.spider.webmagic.Page;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 16/6/24 下午6:15
 */
@Worker
public class MgtvAnimePlayCountSpider extends CrawlSpider {

    private Site site = SiteBuilder.builder().setDomain("vc.mgtv.com").build();
    
    private PageRule rule = PageRule.build().add("", page -> processPage(page));

    public void processPage(Page page) {

        String text = page.getRawText();

        JSONObject data = JSONObject.parseObject(text).getJSONObject("data");

        Long playCount = data.getLong("all");
        ShowLog showLog = new ShowLog();
        Job job = ((DelayRequest) page.getRequest()).getJob();
        if (job != null) {
            DbEntityHelper.derive(job, showLog);
            showLog.setPlayCount(playCount);
            putModel(page,showLog);
        }
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
