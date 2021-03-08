package com.jinguduo.spider.spider.sohu;

import com.alibaba.fastjson.JSON;
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
import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.data.table.CommentLog;
import com.jinguduo.spider.data.text.BarrageText;
import com.jinguduo.spider.data.text.CommentText;
import com.jinguduo.spider.webmagic.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.config.CookieSpecs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

/**
 * Created by gsw on 2016/12/26.
 */
@Worker
@Slf4j
public class SohuCommentNewSpider extends CrawlSpider {

    private static Logger logger = LoggerFactory.getLogger(SohuCommentNewSpider.class);

    private final static String COMMENT_CONTENT_URL = "https://api.my.tv.sohu.com/comment/api/v1/load?topic_id=%s&page_size=50&page_no=1";

    private Site site = SiteBuilder.builder()
            .setDomain("api.my.tv.sohu.com")
            .setCharset("UTF-8")
            .setCookieSpecs(CookieSpecs.IGNORE_COOKIES)
            .addSpiderListener(new UserAgentSpiderListener())
            .build();

    private PageRule rule = PageRule.build()
            .add("comment/api/v1/count", page -> getCommentCount(page))
            .add("comment/api/v1/load", page -> getCommentText(page));

    //https://api.my.tv.sohu.com/comment/api/v1/count?vrs_vids=4966860
    private void getCommentCount(Page page) {
        Job job = ((DelayRequest) page.getRequest()).getJob();

        JSONObject jsonObject = JSON.parseObject(page.getRawText());
        JSONObject data = Optional.of(jsonObject.getJSONObject("data")).orElse(new JSONObject());
        Object o = null != data.values() && data.values().size() > 0 ? data.values().stream().findFirst().orElse(null) : null;
        if (null != o) {
            JSONObject inData = (JSONObject) o;
            Integer commentCount = inData.getInteger("commentCount");
            CommentLog commentLog = new CommentLog(commentCount);
            DbEntityHelper.derive(job, commentLog);
            putModel(page, commentLog);

            //https://api.my.tv.sohu.com/comment/api/v1/load?topic_id=4966860&page_size=50&page_no=1
            String topic_id = inData.getString("topicId");
            Job comment_content_job = new Job(String.format(COMMENT_CONTENT_URL, topic_id));
            DbEntityHelper.derive(job, comment_content_job);
            comment_content_job.setFrequency(FrequencyConstant.COMMENT_TEXT);
            putModel(page, comment_content_job);
        }
    }

    private void getCommentText(Page page) {
        Job job = ((DelayRequest) page.getRequest()).getJob();
        JSONObject jsonObject = JSON.parseObject(page.getRawText());
        jsonObject = jsonObject.getJSONObject("data");
        List<JSONObject> jsonObjectList = (List) jsonObject.getJSONArray("comments");

        if (CollectionUtils.isNotEmpty(jsonObjectList)) {
            /**生成下一个任务*/
            createNextCommentJob(page, job, jsonObjectList);

            jsonObjectList.stream()
                    .forEach(json ->
                            analysisCommentText(json, page, job)
                    );
        }

    }

    private void createNextCommentJob(Page page, Job job, List<JSONObject> jsonObjectList) {
        try {
            //跳过Job生成：评论文本为空
            if (CollectionUtils.isEmpty(jsonObjectList)) {
                return;
            }
            //当前进度
            Integer current = Integer.valueOf(page.getUrl().regex("&page_no=(\\d*)").get());
            //计算下一个任务的进度
            final Integer next_scope = 1 + current;
            //创建递归任务
            final String nextUrl = page.getUrl().replace("&page_no=(\\d*)", String.format("&page_no=%s", next_scope)).get();
            Job newJob = DbEntityHelper.deriveNewJob(job, nextUrl);
            newJob.setFrequency(FrequencyConstant.COMMENT_TEXT);
            putModel(page, newJob);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 解析评论文本
     * 2017年2月18日21:53:46
     */
    private void analysisCommentText(JSONObject json, Page page, Job job) {
        String commentId = json.getString("comment_id");//评论ID
        Integer up = json.getInteger("like_count");//点赞量
        Integer replyCount = json.getInteger("reply_count");//回复数
        String content = json.getString("content");//评论文本
        Long create_time = json.getLong("createtime");//创建时间

        //获取用户信息
        JSONObject userJson = json.getJSONObject("user");
        String userId = "";
        String nickName = "";
        if (null != userJson) {
            userId = userJson.getString("uid");//用户ID
            nickName = userJson.getString("nickname");//昵称
        }
        CommentText commentText = new CommentText();
        commentText.setCommentId(commentId);
        commentText.setUp(up);
        commentText.setReplyCount(replyCount);
        commentText.setContent(content);
        if (create_time != null && String.valueOf(create_time).length() < 13) {
            create_time = create_time * 1000;
        }
        commentText.setCreatedTime(new Timestamp(create_time));
        commentText.setUserId(userId);
        commentText.setNickName(nickName);
        DbEntityHelper.derive(job, commentText);

        putModel(page, commentText);
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
