package com.jinguduo.spider.spider.sohu;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import com.jinguduo.spider.common.util.DateHelper;
import com.jinguduo.spider.common.util.DateUtil;
import com.jinguduo.spider.common.util.NumberHelper;
import com.jinguduo.spider.data.table.ComicKanmanhua;
import com.jinguduo.spider.data.table.ComicZymk;
import com.jinguduo.spider.data.table.CommentLog;
import com.jinguduo.spider.data.text.CommentText;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;

import com.alibaba.fastjson.JSONObject;
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
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.selector.PlainText;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.config.CookieSpecs;

@Worker
@Slf4j
public class SohuCommentSpider extends CrawlSpider {

    private final static String COMMENT_CONTENT_URL = "http://changyan.sohu.com/api/2/topic/comments?client_id=cyqyBluaj&page_no=1&page_size=30&topic_id=%s";

    private static Site site = SiteBuilder.builder()
            .setDomain("changyan.sohu.com")
            .setCookieSpecs(CookieSpecs.IGNORE_COOKIES)
            .addSpiderListener(new UserAgentSpiderListener())
            .build();

    private PageRule rule = PageRule.build()
            .add("changyan\\.sohu\\.com/api/tvproxy/reply/cnts\\.do\\?videoId=.*?", page -> myMediaCount(page))//自媒体评论数
            .add("changyan\\.sohu\\.com/api/2/topic.*", page -> commentCount(page));

    private final static Integer INCRMENT_SCOPE = 1;//请求增长区间

    /**
     * 解析评论数量
     * 搜狐本身的评论已移除，更新至com.jinguduo.spider.spider.sohu.SohuBarrageTextSpider
     */
    private void commentCount(Page page) {

        Job job = ((DelayRequest) page.getRequest()).getJob();
        JSONObject jsonObject = JSONObject.parseObject(page.getRawText());

        /**
         * 知音漫客 漫画 的评论竟然用的搜狐api，放在这儿吧。
         * */
        if (StringUtils.contains(job.getUrl(), "client_id=cysLJ05yl")) {
            this.analyzeZhiyinmankeComment(page, jsonObject);
            return;
        }

        /**
         * 看漫画 的评论竟然用的搜狐api，放在这儿吧。
         * */
//        if (StringUtils.contains(job.getUrl(), "client_id=cysGR3Ozm")) {
//            this.analyzeKanmanhuaComment(page, jsonObject);
//            return;
//        }

        /**解析评论文本*/
        if (job.getUrl().contains("comments")) {
            captureCommentContent(page, jsonObject);
            return;
        }
        Integer commentCount = jsonObject.getInteger("cmt_sum");

        CommentLog commentLog = new CommentLog(commentCount);
        DbEntityHelper.derive(job, commentLog);

        putModel(page, commentLog);

        /**生成搜狐评论文本URL，搜狐需要在此处生成，其他地方获取不到参数*/
        //created by gsw 2017年2月22日10:25:05
        if (!job.getUrl().contains("comments")) {
            String topic_id = jsonObject.getString("topic_id");
            Job comment_content_job = new Job(String.format(COMMENT_CONTENT_URL, topic_id));
            DbEntityHelper.derive(job, comment_content_job);
            comment_content_job.setFrequency(FrequencyConstant.COMMENT_TEXT);
            putModel(page, comment_content_job);
        }
    }

    @Deprecated
    private void analyzeKanmanhuaComment(Page page, JSONObject jsonObject) {
        Job job = ((DelayRequest) page.getRequest()).getJob();
        String code = job.getCode();
        Integer commentCount = jsonObject.getInteger("cmt_sum");
        ComicKanmanhua kan = new ComicKanmanhua();
        kan.setCode(code);
        kan.setDay(DateUtil.getDayStartTime(new Date()));
        kan.setCommentCount(commentCount);
        putModel(page, kan);
    }

    /**
     * 知音漫客 评论 生成
     */
    private void analyzeZhiyinmankeComment(Page page, JSONObject jsonObject) {
        Job job = ((DelayRequest) page.getRequest()).getJob();
        Integer commentCount = null;
        for (String outKey : jsonObject.keySet()) {
            JSONObject result = jsonObject.getJSONObject(outKey);
            for (String inKey : result.keySet()) {
                JSONObject commentPojo = result.getJSONObject(inKey);
                commentCount = commentPojo.getInteger("comments");
            }
        }
        ComicZymk zymk = new ComicZymk();
        zymk.setCode(job.getCode());
        zymk.setDay(DateHelper.getTodayZero(Date.class));
        zymk.setCommentCount(commentCount);
        putModel(page, zymk);
    }

    /**
     * 获取评论文本
     * created by gsw
     * 2017年2月18日21:53:31
     */
    public void captureCommentContent(Page page, JSONObject jsonObject) {
        Job job = ((DelayRequest) page.getRequest()).getJob();

        List<JSONObject> jsonObjectList = (List) jsonObject.getJSONArray("comments");

        if (CollectionUtils.isNotEmpty(jsonObjectList)) {

            /**生成下一个任务*/
            createNextJob(page, job, jsonObjectList);

            jsonObjectList.stream()
                    .forEach(json ->
                            analysis(json, page, job)
                    );
        }

    }

    private void createNextJob(Page page, Job job, List<JSONObject> jsonObjectList) {
        try {
            //跳过Job生成：评论文本为空
            if (CollectionUtils.isEmpty(jsonObjectList)) {
                return;
            }
            //当前进度
            Integer current = Integer.valueOf(page.getUrl().regex("&page_no=(\\d*)").get());
            //计算下一个任务的进度
            final Integer next_scope = INCRMENT_SCOPE + current;
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
    private void analysis(JSONObject json, Page page, Job job) {
        String commentId = json.getString("comment_id");//评论ID
        Integer up = json.getInteger("support_count");//点赞量
        Integer replyCount = json.getInteger("reply_count");//回复数
        String content = json.getString("content");//评论文本
        Long create_time = json.getLong("create_time");//创建时间
        String reply_comment_id = json.getString("reply_id");//回复评论ID

        //获取用户信息
        JSONObject userJson = json.getJSONObject("passport");
        String userId = "";
        String nickName = "";
        if (null != userJson) {
            userId = userJson.getString("user_id");//用户ID
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
        commentText.setReplyCommentId(reply_comment_id);
        commentText.setUserId(userId);
        commentText.setNickName(nickName);
        DbEntityHelper.derive(job, commentText);

        putModel(page, commentText);
    }

    /***
     * 自媒体评论数抓取
     * @param page
     */
    private void myMediaCount(Page page) {

        Job job = ((DelayRequest) page.getRequest()).getJob();

        String raw = page.getRawText();

        Integer commentCount = NumberHelper.parseInt(new PlainText(raw).regex("\"count\":\"(.*?)\",", 1).get(), 1);

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
