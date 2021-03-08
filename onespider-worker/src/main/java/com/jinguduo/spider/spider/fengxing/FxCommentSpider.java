package com.jinguduo.spider.spider.fengxing;

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
import com.jinguduo.spider.common.util.RegexUtil;
import com.jinguduo.spider.data.table.CommentLog;
import com.jinguduo.spider.data.text.CommentText;
import com.jinguduo.spider.webmagic.Page;

@Worker
@CommonsLog
public class FxCommentSpider extends CrawlSpider {
    
    /**分页请求区间*/
    private final static Integer INCRMENT_SCOPE = 1;

    private Site site = SiteBuilder.builder().setDomain("api1.fun.tv").build();

    private PageRule rule = PageRule.build()
            .add("",page -> analysisCommentProcess(page));

    private void analysisCommentProcess(Page page) {
        Job oldJob = ((DelayRequest) page.getRequest()).getJob();

        String rawText = page.getRawText();
        JSONObject jsonObject = JSONObject.parseObject(rawText);
        if(jsonObject==null){
            return;
        }
        
        //总评论量
        Integer commentCount = jsonObject.getJSONObject("data").getInteger("total_num");
        if(commentCount!=null&&commentCount!=0){
            CommentLog commentLog = new CommentLog(commentCount);
            DbEntityHelper.derive(oldJob, commentLog);
            putModel(page,commentLog);
        }
    
        //评论文本分析
        analysisCommentText(page,oldJob,jsonObject);
    }
    
    //解析评论文本
    private void analysisCommentText(Page page ,Job job,JSONObject json) {
        JSONObject jsonData = json.getJSONObject("data");
        String commentStr = jsonData.getString("comment");
        if(StringUtils.isEmpty(commentStr)
                ||"{}".equals(commentStr) ||"[]".equals(commentStr)) {
            log.error("feng xing comments text is empty! job url is :"+job.getUrl());
            return;
        }
        List<JSONObject> jsonObjectList = (List)jsonData.getJSONArray("comment");
        List<CommentText> commentTexts = Lists.newArrayListWithCapacity(jsonObjectList.size());
        if(CollectionUtils.isNotEmpty(jsonObjectList)) {
            /**存放评论文本实体集合*/
            jsonObjectList.stream().forEach(jsonObject -> analysis(jsonObject,page,job,commentTexts));
        }

        if(CollectionUtils.isNotEmpty(commentTexts)){
            putModel(page, commentTexts);
        }
        
        /**生成下一个任务*/
        createNextJob(page,job,jsonObjectList);
    }
    
    private void analysis(JSONObject json,Page page,Job job,List<CommentText> cts) {
        String commentId = json.getString("commentId");//评论ID 风行的全是0，galleryid为电影id
        Integer up = json.getInteger("upCount");//点赞量
        String content = json.getString("content");//评论文本
        Long createTime = json.getLong("time");//文本创建时间
        String userId = json.getString("user_id");//用户ID
        String nickName = json.getString("nick_name");//昵称
        
        if(String.valueOf(createTime).length() <13) {
            createTime = createTime * 1000;
        }

        CommentText commentText = new CommentText();
        commentText.setCommentId(commentId);
        commentText.setUp(up);
        commentText.setReplyCount(0);//风行没有回复功能
        commentText.setContent(content);
        commentText.setCreatedTime(new Timestamp(createTime));
        commentText.setUserId(userId);
        commentText.setNickName(nickName);
        DbEntityHelper.derive(job, commentText);
        cts.add(commentText);
    }
    
    private void createNextJob(Page page,Job job,List<JSONObject> jsonObjectList) {
            //跳过Job生成：评论文本为空
            if(jsonObjectList==null ||jsonObjectList.isEmpty()) {
                return;
            }
            //当前进度
            Integer current = Integer.valueOf(RegexUtil.getDataByRegex(job.getUrl(), "\\?pg=(\\d*)", 1));
            //计算下一个任务的进度
            final Integer next_scope = INCRMENT_SCOPE + current;
            //创建递归任务
            final String nextUrl = job.getUrl().replaceAll("\\?pg=(\\d*)", String.format("?pg=%s",next_scope));
            Job newJob = DbEntityHelper.deriveNewJob(job,nextUrl);
            newJob.setFrequency(FrequencyConstant.COMMENT_TEXT);
            putModel(page,newJob);
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
