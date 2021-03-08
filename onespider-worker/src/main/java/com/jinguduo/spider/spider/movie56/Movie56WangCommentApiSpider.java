package com.jinguduo.spider.spider.movie56;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONException;
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

import lombok.extern.apachecommons.CommonsLog;

@Worker
@CommonsLog
public class Movie56WangCommentApiSpider extends CrawlSpider {

    private Site site = SiteBuilder.builder().setDomain("comment.56.com").build();

    private PageRule rule = PageRule.build()
            .add(".",page -> analysisCommentProcess(page));

    /**
     * 56网评论数解析
     * @param page
     */
    private void analysisCommentProcess(Page page) {
        Job job = ((DelayRequest) page.getRequest()).getJob();
        String rawText = page.getRawText();

        if(StringUtils.isBlank(rawText)){
            log.error("response body is null");
            return;
        }
        JSONObject jsonObject = JSONObject.parseObject( rawText );
        if ( null != jsonObject.getInteger("ctTotal") ){
            Integer commentCount = jsonObject.getInteger("ctTotal");
            
            CommentLog commentLog = new CommentLog(commentCount);
            DbEntityHelper.derive(job, commentLog);
            putModel(page,commentLog);
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
