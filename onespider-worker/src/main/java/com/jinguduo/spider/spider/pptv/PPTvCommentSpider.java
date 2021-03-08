package com.jinguduo.spider.spider.pptv;

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
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.config.CookieSpecs;

@Deprecated
@Worker
@CommonsLog
public class PPTvCommentSpider extends CrawlSpider {


    private Site site = SiteBuilder.builder().setDomain("comment.pptv.com").setCookieSpecs(CookieSpecs.IGNORE_COOKIES).build();

    private PageRule rule = PageRule.build()
            .add("", page -> analysisCommentProcess(page));

    /**
     * pptv 评论数解析
     *
     * @param page
     */
    private void analysisCommentProcess(Page page) {

        Job job = ((DelayRequest) page.getRequest()).getJob();
        if (job == null) {
            log.error("job is null");
            return;
        }

        /** 自定义变量 */
        String code = null;
        String rawText = null;
        Integer commentCount = null;
        JSONObject jsonObject = null;
        /** 解析播放量 */
        //http://comment.pptv.com/api/v1/show.json/?ids=video_24167185&pg=1&ps=20&tm=0&type=1
        code = page.getUrl().regex("video_(\\d*)\\&", 1).get();

        rawText = page.getRawText();

        if (StringUtils.isBlank(code)) {
            log.error("pptv get comment count by code fail , analysis code error by url :[" + page.getUrl() + "]");
            return;
        }

        if (StringUtils.isBlank(rawText)) {
            log.error("response body is null");
            return;
        }
        try {
            jsonObject = JSONObject.parseObject(rawText);
            if (null != jsonObject.getJSONObject("data")) {
                commentCount = jsonObject.getJSONObject("data").getJSONObject("video_" + code).getIntValue("count");
            } else {
                throw new JSONException("cannot analysis comment count by url :[" + page.getUrl() + "]");
            }
        } catch (JSONException e) {
            log.error(e.getMessage(), e);
            jsonObject = null;
            return;
        } catch (Exception e) {
            log.error("unknow exception! by comment :[" + page.getUrl() + "]");
            log.error(e.getMessage(), e);
            return;
        }

        if (null == commentCount) {
            log.error("commentCount is null ,maybe code has problem , code :[" + code + "],url :[" + page.getUrl() + "]");
            return;
        }

        CommentLog commentLog = new CommentLog(commentCount);
        DbEntityHelper.derive(job, commentLog);

        putModel(page, commentLog);
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
