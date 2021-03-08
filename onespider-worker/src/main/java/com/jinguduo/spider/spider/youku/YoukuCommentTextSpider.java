package com.jinguduo.spider.spider.youku;

import java.sql.Timestamp;
import java.util.List;

import lombok.extern.apachecommons.CommonsLog;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
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
import com.jinguduo.spider.data.text.CommentText;
import com.jinguduo.spider.webmagic.Page;

/**
 * Created by gsw on 2017/2/23.
 */
@Worker
@CommonsLog
public class YoukuCommentTextSpider extends CrawlSpider{

    private static Integer INCRMENT_SCOPE = 1;//请求增长区间

    private static Site site = SiteBuilder.builder().setDomain("api.mobile.youku.com").build();

    private PageRule rule = PageRule.build()
            .add("/comment/list/new",page -> analysisCommentContent(page));


    private void analysisCommentContent(Page page) {
        Job job = ((DelayRequest) page.getRequest()).getJob();
        String rawText = page.getRawText();
        JSONObject jsonObject = JSON.parseObject(rawText);
        String resultStr = jsonObject.getString("results");
        if(StringUtils.isEmpty(resultStr)||"{}".equals(resultStr) ||"[]".equals(resultStr)) {
            return;
        }
        List<JSONObject> jsonObjectList = (List) jsonObject.getJSONArray("results");
        if (CollectionUtils.isNotEmpty(jsonObjectList)) {
            List<CommentText> cts = Lists.newArrayListWithCapacity(jsonObjectList.size());
            jsonObjectList.stream().forEach(json -> analysis(json, page, job, cts));
            putModel(page, cts);
        }
        /**生成下一个任务*/
        createNextJob(page,job,jsonObjectList);
    }

    private void createNextJob(Page page,Job job,List<JSONObject> jsonObjectList) {
            //跳过Job生成：评论文本为空
            if(jsonObjectList==null ||jsonObjectList.isEmpty()) {
                return;
            }
            //当前进度
            Integer current = Integer.valueOf(page.getUrl().regex("&pg=(\\d*)").get());
            //计算下一个任务的进度
            final Integer next_scope = INCRMENT_SCOPE + current;
            //创建递归任务
            final String nextUrl = page.getUrl().replace("&pg=(\\d*)", String.format("&pg=%s",next_scope)).get();
            Job newJob = DbEntityHelper.deriveNewJob(job,nextUrl);
            newJob.setFrequency(FrequencyConstant.COMMENT_TEXT);
            putModel(page,newJob);
            log.debug("create youku next job url successful!"+nextUrl);
    }

    /**
     * 解析评论文本
     *
     * @param json
     * @param page
     * @param job
     */
    private void analysis(JSONObject json, Page page, Job job, List<CommentText> cts) {
            JSONArray jsonArray = json.getJSONArray("reply_content");//回复评论  只能拿到2个
            if(jsonArray!=null&&jsonArray.size()>0){
                for (Object reply : jsonArray) {
                    JSONObject subJson = (JSONObject) reply;
                    CommentText subText = parseToCommentText(subJson,job);
                    cts.add(subText);
                }
            }
            CommentText text = parseToCommentText(json,job);
            cts.add(text);
    }
    
    private CommentText parseToCommentText(JSONObject json,Job job){
        String commentId = json.getString("id");//评论ID
        Integer up = json.getInteger("total_up");//点赞量
        Integer replyCount = json.getInteger("reply_total");//回复数
        String reply_id = json.getString("reply_id");//回复评论ID
        String content = json.getString("content");//评论文本
        Long createTime = json.getLong("create_at");//文本创建时间
        String userId = json.getString("userid");//用户ID
        String nickName = json.getString("username");//昵称

        CommentText commentText = new CommentText();
        commentText.setCommentId(commentId);
        commentText.setUp(up);
        commentText.setReplyCount(replyCount);
        commentText.setReplyCommentId(reply_id);
        commentText.setContent(content);
        if(String.valueOf(createTime).length() <13) {
            createTime = createTime * 1000;
        }
        commentText.setCreatedTime(new Timestamp(createTime));
        commentText.setUserId(userId);
        commentText.setNickName(nickName);
        DbEntityHelper.derive(job, commentText);
        return commentText;
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
