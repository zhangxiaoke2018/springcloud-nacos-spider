package com.jinguduo.spider.spider.iqiyi;


import com.alibaba.fastjson.JSONObject;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.spider.listener.UserAgentSpiderListener;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.data.table.CommentLog;
import com.jinguduo.spider.webmagic.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.config.CookieSpecs;

/**
 * 2019-03-04 iqiyi 泡泡评论分集处理
 *  xk
 * */

@Worker
@Slf4j
public class IqiyiEpisodePaopaoCommentSpider extends CrawlSpider {

    private Site site = SiteBuilder.builder()
            .setDomain("sns-comment.iqiyi.com")
            .build();

    private PageRule rules = PageRule.build()
            .add("/v3/comment/get_comments",page -> getPaopaoComment(page))
            ;

    public void getPaopaoComment(Page page){
        Job job = ((DelayRequest) page.getRequest()).getJob();
        String rawText = page.getRawText();
        if(StringUtils.isBlank(rawText)){
            log.error("response body is null");
            return;
        }
        String jsonString = rawText.substring(rawText.indexOf("(",1)+1,rawText.indexOf(") }catch(e){};"));
        //System.out.print(jsonString);
        JSONObject jsonObject = JSONObject.parseObject(jsonString);
        JSONObject data = jsonObject.getJSONObject("data");
        Integer totalCommentCount = data.getInteger("totalCount");
        CommentLog paopaofenji = new CommentLog();
        paopaofenji.setCommentCount(totalCommentCount);
        DbEntityHelper.derive(job, paopaofenji);
        putModel(page,paopaofenji);

    }

    @Override
    public PageRule getPageRule() {
        return this.rules;
    }

    @Override
    public Site getSite() {
        return this.site;
    }
}
