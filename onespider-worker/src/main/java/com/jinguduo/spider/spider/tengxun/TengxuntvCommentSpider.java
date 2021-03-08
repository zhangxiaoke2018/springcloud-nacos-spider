package com.jinguduo.spider.spider.tengxun;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
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

import java.sql.Timestamp;
import java.util.List;

/**
 * @title 腾讯视频，评论数抓取
 */
@Worker
@Slf4j
public class TengxuntvCommentSpider extends CrawlSpider {

    private static String NORMAL_RESCODE = "0";//正常返回，code值

    //评论文本URL
    private static String COMMENT_TEXT_URL = "https://coral.qq.com/article/1744194068/comment?commentid=6238234434044773188&reqnum=20";

    private Site site = SiteBuilder.builder()
            .setDomain("coral.qq.com")
            .addSpiderListener(new UserAgentSpiderListener())
            .build();

    private PageRule rules = PageRule.build()
            .add("coral\\.qq\\.com/article/.*?/commentnum", page -> count(page))
            .add("coral\\.qq\\.com/article/.*?/comment\\?commentid=", page -> comments(page));

    private void count(Page page) {

        Job job = ((DelayRequest) page.getRequest()).getJob();

        if (page.getRequest().getUrl().contains("null")){
            return;
        }

        String rawText = page.getRawText();
        Integer commentCount = null;
        JSONObject jsonObject = null;

        if (StringUtils.isBlank(rawText)) {
            log.error("response body is null");
            return;
        }
        try {
            jsonObject = JSONObject.parseObject(rawText);
            if (NORMAL_RESCODE.equals(jsonObject.getString("errCode"))) {
                if (null != jsonObject.getJSONObject("data")) {
                    commentCount = jsonObject.getJSONObject("data").getInteger("commentnum");
                }
            } else {
                log.warn("tengxun return errCode :[" + jsonObject.getString("errCode") + "] by url :[" + page.getUrl() + "]");
            }
        } catch (JSONException e) {
            throw new AntiSpiderException("coral.qq.com , get commentnum error");
        }

        if (null == commentCount) {
            throw new AntiSpiderException("coral.qq.com , get commentnum error");
        }

        CommentLog commentLog = new CommentLog(commentCount);
        DbEntityHelper.derive(job, commentLog);

        putModel(page,commentLog);
    }

    /**
     * 获取评论文本
     * created by gsw
     * 2017年2月17日16:53:52
     */
    public void comments(Page page) {
        Job job = ((DelayRequest) page.getRequest()).getJob();
        String rawText = page.getRawText();
        JSONObject jsonObject = JSON.parseObject(rawText);
        JSONObject dataJson = jsonObject.getJSONObject("data");
        //错误状态时返回的不是json格式数据，
        if(dataJson==null){
            log.error("tengxun_commentnum_error: not found data attribute,"+page.getUrl());
            return;
        }
        List<JSONObject> jsonObjectList = (List) dataJson.getJSONArray("commentid");
        if (CollectionUtils.isNotEmpty(jsonObjectList)) {
            List<CommentText> cts = Lists.newArrayListWithCapacity(jsonObjectList.size());
            jsonObjectList.stream().forEach(json -> text(json, page, job, cts));
            putModel(page, cts);
        }
        /**生成腾讯评论文本下一个任务*/
        createNextJob(page,job,jsonObjectList);
    }

    private void createNextJob(Page page, Job job,List<JSONObject> jsonObjectList) {
        try {
            if(jsonObjectList ==null ||jsonObjectList.isEmpty()) {
                return;
            }
            JSONObject json = jsonObjectList.get(jsonObjectList.size()-1);
            /**
             * 腾讯评论文本的下一页的分割点正好是最后一个json文本中的ID值：
             * 即下一个任务进度为：jsonObjectList.get(jsonObjectList.size()-1).getString("id");
             */
            String finalCommentid = json.getString("id");
            //创建递归任务
            final String nextUrl = page.getUrl().replace("commentid=(\\d*)",String.format("commentid=%s",finalCommentid)).get();
            Job newJob = DbEntityHelper.deriveNewJob(job,nextUrl);
            newJob.setFrequency(FrequencyConstant.COMMENT_TEXT);
            putModel(page,newJob);
            log.debug("create tengxun next job url successful!"+nextUrl);
        }catch (Exception e) {
            throw new AntiSpiderException("coral.qq.com , get comment text error");
        }
    }
    /**
     * 解析评论文本
     *
     * @param json
     * @param page
     * @param job
     */
    private void text(JSONObject json, Page page, Job job, List<CommentText> cts) {
        try {
            String content = json.getString("content");//评论文本
            if (StringUtils.isBlank(content)) {
                content = json.getString("title");//可能是投票评论或者图片评论
                if (StringUtils.isBlank(content)) {
                    return;
                }
            }
            String commentId = json.getString("id");//评论ID
            Integer up = json.getInteger("up");//点赞量
            Integer replyCount = json.getInteger("rep");//回复数
            Long showTime = json.getLong("time");//文本出现时间

            JSONObject userJson = json.getJSONObject("userinfo");
            String userId = "";
            String nickName = "";
            if (null != userJson) {
                userId = userJson.getString("userid");//用户ID
                nickName = userJson.getString("nick");//昵称
            }
            // 转换单位为ms
            if(String.valueOf(showTime).length() <13) {
                showTime = showTime * 1000;
            }
            CommentText commentText = new CommentText();
            commentText.setCommentId(commentId);
            commentText.setUp(up);
            commentText.setReplyCount(replyCount);
            commentText.setContent(content);
            commentText.setCreatedTime(new Timestamp(showTime));
            commentText.setUserId(userId);
            commentText.setNickName(nickName);
            DbEntityHelper.derive(job, commentText);
            cts.add(commentText);
        } catch (Exception e) {
            throw new AntiSpiderException("coral.qq.com , get comment text error");
        }
    }

    @Override
    public PageRule getPageRule() {
        return rules;
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
