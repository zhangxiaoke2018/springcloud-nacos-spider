package com.jinguduo.spider.spider.iqiyi;

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
import com.jinguduo.spider.common.exception.AntiSpiderException;
import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.data.table.CommentLog;
import com.jinguduo.spider.data.text.CommentText;
import com.jinguduo.spider.webmagic.Page;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.config.CookieSpecs;

import java.sql.Timestamp;
import java.util.List;

/**
 * 抓取iqiyi 分集评论数量
 */
@Worker
@Slf4j
public class IqiyiCommentSpider extends CrawlSpider {

    private Site site = SiteBuilder.builder()
            .setDomain("api.t.iqiyi.com")
            .setCookieSpecs(CookieSpecs.IGNORE_COOKIES)
            .addSpiderListener(new UserAgentSpiderListener())
            .build();
    
    /**分页请求增长区间*/
    private final static Integer INCREMENT_SCOPE = 1;
    
    //get_video_comments对应评论回复文本抓取
    private static final String REPLY_COMMENT_TEXT_URL = "http://api.t.iqiyi.com/qx_api/comment/get_comment_with_repies?contentid=%s&escape=true&need_reply=true&page=1&page_size=10&sort=hot";
    //get_feeds 对应评论回复文本抓取
    private static final String REPLY_COMMENT_TEXT_URL_TWO = "http://api.t.iqiyi.com/feed/get_comments?contentid=%s&page_size=5&page=1";
    private PageRule rules = PageRule.build()
            .add("/feed/get_feeds", page -> capturePopCommentContent(page))  // 泡泡评论文本, 无分集
            .add("/feed/get_comments", page -> capturePopCommentReply(page))  //泡泡评论的回复评论
            .add("/comment/get_video_comments",page -> captureCommentCount(page))  // 老评论，有评论总数
            .add("/comment/get_video_comments", page -> captureCommnetContentForOld(page))  //爱奇艺老评论文本接口，有分集
            .add("/comment/get_comment_with_repies", page -> captureCommentReplyForOld(page))  //iqiyi老评论的回复评论
            ;

    private void processAntiSpider(Page page) throws AntiSpiderException {
    	String rawText = page.getRawText();
    	if (rawText.contains("The URL you requested has been blocked")) {
			throw new AntiSpiderException();
		}
    }
    
    private static String NORMAL_RESCODE = "A00000";

    /***
     * @title 抓取爱奇艺评论数量
     * @param page
     * @throws AntiSpiderException 
     */
    private void captureCommentCount(Page page) throws AntiSpiderException {
        Job job = ((DelayRequest) page.getRequest()).getJob();
        String rawText = page.getRawText();
        if (StringUtils.isBlank(rawText)) {
        	throw new AntiSpiderException(job.getUrl());
        }
        processAntiSpider(page);
        JSONObject jsonObject = JSONObject.parseObject( rawText );

        if( NORMAL_RESCODE.equals( jsonObject.getString("code") ) ){
            if ( null != jsonObject.getJSONObject("data") ){
                Integer commentCount = jsonObject.getJSONObject("data").getInteger("count");
                CommentLog commentLog = new CommentLog(commentCount);
                DbEntityHelper.derive(job, commentLog);
                
                putModel(page,commentLog);
            }
        }
    }
    
    //爱奇艺泡泡评论文本,无分集
    private void capturePopCommentContent(Page page) throws AntiSpiderException {
        String rawText = page.getRawText();
        if (StringUtils.isBlank(rawText)) {
        	throw new AntiSpiderException();
        }
        processAntiSpider(page);
        JSONObject jsonObject = JSONObject.parseObject( rawText );
        
        JSONObject dataJson = jsonObject.getJSONObject("data");
        if(dataJson==null||StringUtils.isEmpty(dataJson.getString("feeds"))
                || "[]".equals(dataJson.getString("feeds"))) {
            log.error("iqiyi_paopaocomments_error: not found data or feeds attribute,or feeds is [], "+page.getUrl());
            return;
        }
        List<JSONObject> jsonObjectList = (List)dataJson.getJSONArray("feeds");
        List<CommentText> commentTexts = Lists.newArrayListWithCapacity(jsonObjectList.size());
        List<Job> replyJobs = Lists.newArrayList();
        jsonObjectList.stream().forEach(json ->analysis(page, commentTexts,json,replyJobs) );
        
        /**生成下一个任务*/
        createNextJob(page, jsonObjectList);
        
        if(CollectionUtils.isNotEmpty(commentTexts)){
            putModel(page, commentTexts);
        }
        
        //生成评论回复的任务
        if(CollectionUtils.isNotEmpty(replyJobs)){
            putModel(page, replyJobs);
        }
    }

    private void analysis(Page page, List<CommentText> cts,JSONObject json,List<Job> replyJobs) {
    	Job job = ((DelayRequest) page.getRequest()).getJob();
        String commentId = json.getString("commentId");//评论ID
        String description = json.getString("description");//评论文本
        Integer up = json.getInteger("agreeCount");//点赞量
        Integer replyCount = json.getInteger("commentCount");//回复评论数
        if(replyCount>0){
            //生成回复评论爬取的任务
            Job replyCommentJob = new Job(String.format(REPLY_COMMENT_TEXT_URL_TWO, commentId));
            DbEntityHelper.derive(job, replyCommentJob);
            replyCommentJob.setFrequency(FrequencyConstant.COMMENT_TEXT);
            replyJobs.add(replyCommentJob);
        }
        String uid = json.getString("uid");//用户ID
        String nickName = json.getString("name");//昵称
        Long create_time = json.getLong("snsTime");//创建时间

        if(String.valueOf(create_time).length()<13){
            create_time = create_time * 1000;
        }
        CommentText commentText = new CommentText();
        commentText.setCommentId(commentId);
        commentText.setContent(description);
        commentText.setUp(up);
        commentText.setReplyCount(replyCount);
        commentText.setCreatedTime(new Timestamp(create_time));
        commentText.setUserId(uid);
        commentText.setNickName(nickName);
        DbEntityHelper.derive(job,commentText);
        cts.add(commentText);
    }

    /**
     * 生成抓取评论文本的下一个任务
     * @param page
     * @param job
     * @param jsonObjectList
     */
    private void createNextJob(Page page, List<JSONObject> jsonObjectList) {
    	Job job = ((DelayRequest) page.getRequest()).getJob();
        //跳过Job生成：时间超过一个小时，且评论文本为空
        if(jsonObjectList==null ||jsonObjectList.isEmpty()) {
            return;
        }
        JSONObject json = jsonObjectList.get(jsonObjectList.size()-1);
        //下一个任务进度
        String nextFeedId = json.getString("feedId");
        if(StringUtils.isEmpty(nextFeedId)) {
            nextFeedId = json.getString("fid");
        }
        //创建递归任务
        final String nextUrl = page.getUrl().replace("feedId=(\\d*)", String.format("feedId=%s",nextFeedId)).get();
        Job newJob = DbEntityHelper.deriveNewJob(job, nextUrl);
        newJob.setFrequency(FrequencyConstant.COMMENT_TEXT);
        putModel(page,newJob);
    }
    
    //爱奇艺老评论文本接口，有分集
    private void captureCommnetContentForOld(Page page) throws AntiSpiderException {
        String rawText = page.getRawText();
        if (StringUtils.isBlank(rawText)) {
        	throw new AntiSpiderException();
        }
        processAntiSpider(page);
        JSONObject jsonObject = JSONObject.parseObject( rawText );
        
        JSONObject dataJson = jsonObject.getJSONObject("data");
        if(dataJson==null||StringUtils.isEmpty(dataJson.getString("comments"))
                || "[]".equals(dataJson.getString("comments"))) {
            log.debug("iqiyi_getcomments_error: not found data or comments attribute,or comments is [],"+page.getUrl());
            return;
        }
        List<JSONObject> jsonObjectList = (List)dataJson.getJSONArray("comments");
        List<CommentText> commentTexts = Lists.newArrayListWithCapacity(jsonObjectList.size());
        List<Job> replyJobs = Lists.newArrayList();
        jsonObjectList.stream().forEach(json ->analysis2(page, commentTexts,json,replyJobs) );
        
        if(CollectionUtils.isNotEmpty(commentTexts)){
            putModel(page, commentTexts);
        }
        if(CollectionUtils.isNotEmpty(replyJobs)){
            //生成评论回复的任务
            putModel(page,replyJobs);
        }

        /**生成下一个任务*/
        createNextJob2(page, jsonObjectList);
    }

    private void analysis2(Page page, List<CommentText> cts,JSONObject json,List<Job> replyJobs) {
            if (json.getJSONObject("sourceInfo") != null && json.getJSONObject("sourceInfo").getString("link").contains("weibo.com")) {
                return;
            }
            Job job = ((DelayRequest) page.getRequest()).getJob();
            String commentId = json.getString("contentId");//评论ID
            String description = json.getString("content");//评论文本
            // 有心无力, 心有余而力不足
            if (StringUtils.isBlank(description)) {
                description = json.getString("voteTitle"); // 投票标题
            }
            Long create_time = json.getLong("addTime");//创建时间
            
            JSONObject counterList = json.getJSONObject("counterList");
            Integer up = counterList.getInteger("likes");//点赞量
            Integer replyCount = counterList.getInteger("replies");//回复评论数
            if(replyCount>0){
                //生成回复评论爬取的任务
                Job replyCommentJob = new Job(String.format(REPLY_COMMENT_TEXT_URL, commentId));
                DbEntityHelper.derive(job, replyCommentJob);
                replyCommentJob.setFrequency(FrequencyConstant.COMMENT_TEXT);
                replyJobs.add(replyCommentJob);
            }
            JSONObject user = json.getJSONObject("userInfo");
            String uid = user.getString("uid");//用户ID
            String nickName = user.getString("uname");//昵称
            if(String.valueOf(create_time).length()<13){
                create_time = create_time * 1000;
            }

            CommentText commentText = new CommentText();
            commentText.setCommentId(commentId);
            commentText.setContent(description);
            commentText.setUp(up);
            commentText.setReplyCount(replyCount);
            commentText.setCreatedTime(new Timestamp(create_time));
            commentText.setUserId(uid);
            commentText.setNickName(nickName);
            DbEntityHelper.derive(job,commentText);
            cts.add(commentText);
    }

    /**
     * 生成抓取评论文本的下一个任务
     * @param page
     * @param job
     * @param jsonObjectList
     */
    private void createNextJob2(Page page, List<JSONObject> jsonObjectList) {
            if(jsonObjectList ==null ||jsonObjectList.isEmpty()) {
                return;
            }
            Job job = ((DelayRequest) page.getRequest()).getJob();
            //当前进度
            Integer current = Integer.valueOf(page.getUrl().regex("&page=(\\d*)").get());
            //计算下一个任务的进度
            final Integer nextProgress = INCREMENT_SCOPE + current;
            //创建递归任务
            final String nextUrl = page.getUrl().replace("&page=(\\d*)",String.format("&page=%s",nextProgress)).get();
            Job newJob = DbEntityHelper.deriveNewJob(job,nextUrl);
            newJob.setFrequency(FrequencyConstant.COMMENT_TEXT);
            putModel(page,newJob);
    }
    
    
    
    /**
     * 抓取评论的回复评论逻辑
     * @param page
     * @throws AntiSpiderException 
     */
    private void captureCommentReplyForOld(Page page) throws AntiSpiderException{
        Job job = ((DelayRequest) page.getRequest()).getJob();
        String rawText = page.getRawText();
        if (StringUtils.isBlank(rawText)) {
			throw new AntiSpiderException(job.getUrl());
        }
        processAntiSpider(page);
        JSONObject jsonObject = JSON.parseObject(rawText);
        JSONObject dataJson = jsonObject.getJSONObject("data");
        if(dataJson==null||StringUtils.isEmpty(dataJson.getString("replyList"))
                || "[]".equals(dataJson.getString("replyList"))) {
            //log.error("iqiyi_replyCommentProcess:"+page.getRawText());
            log.error("iqiyi_replycommentprocess_error: not found data or replyList attribute,or replyList is [],"+page.getUrl());
            return;
        }
        List<JSONObject> jsonObjectList = (List)dataJson.getJSONArray("replyList");
        List<CommentText> commentTexts = Lists.newArrayListWithCapacity(jsonObjectList.size());
        jsonObjectList.stream().forEach(json ->analysisReply(page,job,commentTexts,json));

        if(CollectionUtils.isNotEmpty(commentTexts)){
            putModel(page, commentTexts);
        }
        
        /**生成下一个任务*/
        createNextJob2(page, jsonObjectList);
    }
    
    
    private void analysisReply(Page page,Job job,List<CommentText> cts,JSONObject json) {
            String commentId = json.getString("id");//评论ID
            String description = json.getString("content");//评论文本
            Long create_time = json.getLong("addTime");//创建时间
            
            Integer up = json.getInteger("likes");//点赞量
            Integer replyCount = json.getInteger("replyCount");//回复评论数
            JSONObject user = json.getJSONObject("userInfo");
            String uid = user.getString("uid");//用户ID
            String nickName = user.getString("uname");//昵称
            String replyCommentId = json.getString("mainContentId");
            if(String.valueOf(create_time).length()<13){
                create_time = create_time * 1000;
            }

            CommentText commentText = new CommentText();
            commentText.setCommentId(commentId);
            commentText.setContent(description);
            commentText.setUp(up);
            commentText.setReplyCount(replyCount);
            commentText.setCreatedTime(new Timestamp(create_time));
            commentText.setUserId(uid);
            commentText.setNickName(nickName);
            commentText.setReplyCommentId(replyCommentId);
            DbEntityHelper.derive(job,commentText);
            cts.add(commentText);
    }
    
    
    //规则为get_feeds 评论文本的  回复评论文本抓取
    private void capturePopCommentReply(Page page) throws AntiSpiderException{
        Job job = ((DelayRequest) page.getRequest()).getJob();
        String rawText = page.getRawText();
        if (StringUtils.isBlank(rawText)) {
			throw new AntiSpiderException(job.getUrl());
        }
        processAntiSpider(page);
        JSONObject jsonObject = JSON.parseObject(rawText);
        JSONObject dataJson = jsonObject.getJSONObject("data");
        if(dataJson==null||StringUtils.isEmpty(dataJson.getString("replies"))
                || "[]".equals(dataJson.getString("replies"))) {
            log.error("iqiyi_replycommentprocessTwo_error: not found data or replies attribute, "+page.getUrl());
            return;
        }
        List<JSONObject> jsonObjectList = (List)dataJson.getJSONArray("replies");
        List<CommentText> commentTexts = Lists.newArrayListWithCapacity(jsonObjectList.size());
        jsonObjectList.stream().forEach(json ->analysisReply(page,job,commentTexts,json));

        if(CollectionUtils.isNotEmpty(commentTexts)){
            putModel(page, commentTexts);
        }
        
        /**生成下一个任务*/
        createNextJob2(page, jsonObjectList);
    }
    
    
    @Override
    public PageRule getPageRule() {
        return rules;
    }

    @Override
    public Site getSite() {
        return site;
    }
}
