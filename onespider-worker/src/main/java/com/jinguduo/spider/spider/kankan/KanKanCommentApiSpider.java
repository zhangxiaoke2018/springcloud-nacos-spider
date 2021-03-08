package com.jinguduo.spider.spider.kankan;

import java.sql.Timestamp;
import java.util.List;

import lombok.extern.apachecommons.CommonsLog;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.constant.FrequencyConstant;
import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.data.table.CommentLog;
import com.jinguduo.spider.data.text.CommentText;
import com.jinguduo.spider.webmagic.Page;

@Worker
@CommonsLog
public class KanKanCommentApiSpider extends CrawlSpider {

    /**分页请求区间*/
    private final static Integer INCRMENT_SCOPE = 1;

    private Site site = SiteBuilder.builder().setDomain("api.t.kankan.com").build();

    private PageRule rule = PageRule.build()
            .add("hotscomment",page -> getCommentCount(page))//评论量
            .add("perpage=",page -> getCommentContent(page));//评论文本

    /***
     * 解析评论量
     * @param page
     */
    private void getCommentCount(Page page) {
        Job oldJob = ((DelayRequest) page.getRequest()).getJob();

        String rawText = page.getRawText();
        Integer commentCount = null;

        rawText = rawText.substring(rawText.indexOf("=")+1,rawText.lastIndexOf(";"));
        if(StringUtils.isBlank(rawText)){
            log.error("KanKanCommentApiSpider getCommentCount get empty response! url:"+oldJob.getUrl());
            return;
        }
            JSONObject json = JSONObject.parseObject( rawText );
            if(!StringUtils.equals(json.getString("status"), "200")){
                log.error("KanKanCommentApiSpider getCommentCount response fail! url:"+oldJob.getUrl()+" responseBody:["+rawText+"]");
                return;
            }
            
            if (json.getJSONObject("data")!=null){
                commentCount = json.getJSONObject("data").getJSONObject("misc").getInteger("count");
            }
            
            if ( null == commentCount ) {
                log.error("KanKanCommentApiSpider getCommentCount get comment count is null! url:"+oldJob.getUrl());
                return;
            }
            
            CommentLog commentLog = new CommentLog(commentCount);
            DbEntityHelper.derive(oldJob, commentLog);
            putModel(page,commentLog);
    }

    /**
     * 解析评论文本
     * @param page
     * @param job
     */
    private void getCommentContent(Page page) {
        Job oldJob = ((DelayRequest) page.getRequest()).getJob();
        
        String rawText = page.getRawText();

        rawText = rawText.substring(rawText.indexOf("=")+1,rawText.lastIndexOf(";"));
        if(StringUtils.isBlank(rawText)){
            log.error("KanKanCommentApiSpider getCommentContent get empty response! url:"+oldJob.getUrl());
            return;
        }
        
        JSONObject json = JSONObject.parseObject(rawText);
        
        if(!json.containsKey("data")||!json.getJSONObject("data").containsKey("weibo")){
            log.error("KanKanCommentApiSpider getCommentContent get comments text error! url:"+oldJob.getUrl());
            return;
        }
        JSONObject jsonData = json.getJSONObject("data");
        String commentStr = jsonData.getString("weibo");
        if(StringUtils.isEmpty(commentStr)||"{}".equals(commentStr) ||"[]".equals(commentStr)) {
            log.error("KanKanCommentApiSpider getCommentContent comments text is empty! url:"+oldJob.getUrl());
            return;
        }
        List<JSONObject> jsonObjectList = (List)jsonData.getJSONArray("weibo");
        /**存放评论文本实体集合*/
        List<CommentText> commentTexts = Lists.newArrayListWithCapacity(jsonObjectList.size());
        if(CollectionUtils.isNotEmpty(jsonObjectList)) {
            jsonObjectList.stream().forEach(jsonObject -> analysis(jsonObject,page,oldJob,commentTexts));
        }
        
        if(CollectionUtils.isNotEmpty(commentTexts)){
            putModel(page, commentTexts);
        }

        /**生成下一个任务*/
        createNextJob(page,oldJob,jsonObjectList);
    }

    private void analysis(JSONObject json,Page page,Job job,List<CommentText> cts) {
        try {
            String commentId = json.getString("_id");//评论ID
            Integer up = json.getInteger("useful_num");//点赞量
            Integer replyCount = json.getInteger("comment_count");//回复数
            String content = json.getString("content");//评论文本
            String createTime = json.getString("pub_time");//文本创建时间

            String userId = "";
            String nickName = "";

            //用户信息json
            JSONObject userJson = json.getJSONObject("userinfo");
            if(null !=userJson) {
                userId = userJson.getString("userid");//用户ID
                nickName = userJson.getString("nickname");//昵称
            }

            CommentText commentText = new CommentText();
            commentText.setCommentId(commentId);
            commentText.setUp(up);
            commentText.setReplyCount(replyCount);
            commentText.setContent(content);
            commentText.setCreatedTime(Timestamp.valueOf(createTime));
            commentText.setUserId(userId);
            commentText.setNickName(nickName);
            DbEntityHelper.derive(job, commentText);
            cts.add(commentText);
        } catch (Exception e) {
            log.debug(e.getMessage(), e);
        }
    }

    private void createNextJob(Page page,Job job,List<JSONObject> jsonObjectList) {
        try {
            //跳过Job生成：评论文本为空
            if(jsonObjectList==null ||jsonObjectList.isEmpty()) {
                return;
            }
            //当前进度
            Integer current = Integer.valueOf(page.getUrl().regex("&page=(\\d*)").get());
            //计算下一个任务的进度
            final Integer next_scope = INCRMENT_SCOPE + current;
            //创建递归任务
            final String nextUrl = page.getUrl().replace("&page=(\\d*)", String.format("&page=%s",next_scope)).get();
            Job newJob = DbEntityHelper.deriveNewJob(job,nextUrl);
            newJob.setFrequency(FrequencyConstant.COMMENT_TEXT);
            putModel(page,newJob);
        }catch (Exception e) {
            log.error(e.getMessage(),e);
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
