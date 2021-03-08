package com.jinguduo.spider.spider.youku;

import lombok.extern.apachecommons.CommonsLog;

import com.alibaba.fastjson.JSONObject;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.data.table.CommentLog;
import com.jinguduo.spider.webmagic.Page;

@Worker
@CommonsLog
public class YouKuCommentCountSpider extends CrawlSpider {

    private static Site site = SiteBuilder.builder().setDomain("p.comments.youku.com").build();

    private PageRule rule = PageRule.build()
            .add("/ycp/",page -> getCommentCount(page));

    /** 评论数量解析 */
    private void getCommentCount(Page page) {
        Job job = ((DelayRequest) page.getRequest()).getJob();
        
        JSONObject jsonObject = page.getJson().toObject(JSONObject.class);
        JSONObject data = jsonObject.getObject("data",JSONObject.class);
        if(data==null){
            log.error("YouKuCommentCountSpider getCommentCount can't get data! url:"+job.getUrl());
            return;
        }
        Integer commentCount = data.getInteger("totalSize");
        CommentLog commentLog = new CommentLog(commentCount);
        DbEntityHelper.derive(job, commentLog);
        putModel(page,commentLog);
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
