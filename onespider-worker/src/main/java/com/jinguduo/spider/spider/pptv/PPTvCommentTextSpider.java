package com.jinguduo.spider.spider.pptv;

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
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.constant.FrequencyConstant;
import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.data.table.CommentLog;
import com.jinguduo.spider.data.text.CommentText;
import com.jinguduo.spider.webmagic.Page;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.List;

/**
 * Created by gsw on 2017/02/22.
 */
@Worker
@CommonsLog
public class PPTvCommentTextSpider extends CrawlSpider {

    private Logger logger = LoggerFactory.getLogger(PPTvCommentTextSpider.class);

    private Site site = SiteBuilder.builder().setDomain("apicdn.sc.pptv.com")
            .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.102 Safari/537.36 QIHU 360EE")
            .addHeader("Host", "apicdn.sc.pptv.com")
            .build();

    /**
     * 分页请求增长区间
     */
    private final static Integer INCREMENT_SCOPE = 1;

    private PageRule rule = PageRule.build()
            .add("list", page -> analysisCommentTextProcess(page))
            .add("/count", page -> analysisCommentCount(page));

    private void analysisCommentCount(Page page) {
        Job job = ((DelayRequest) page.getRequest()).getJob();
        //http://apicdn.sc.pptv.com/sc/v4/pplive/ref/vod_28158399/feed/count?appplt=web
        String rawText = page.getRawText();
        JSONObject jsonObject = JSON.parseObject(rawText);
        Integer err = jsonObject.getInteger("err");
        if (0 != err) {
            log.error("apicdn.sc.pptv.com get comment error ,this msg is -> " + jsonObject.getString("msg"));
            return;
        }

        Integer count = jsonObject.getInteger("data");
        if (null == count) {
            log.error("apicdn.sc.pptv.com get comment success , but count is null  ,this msg is -> " + jsonObject.getString("msg"));
            return;
        }

        CommentLog commentLog = new CommentLog(count);
        DbEntityHelper.derive(job, commentLog);
        putModel(page, commentLog);
    }

    /**
     * pptv 评论文本解析
     *
     * @param page
     */
    private void analysisCommentTextProcess(Page page) {
        Job job = ((DelayRequest) page.getRequest()).getJob();
        if (job == null) {
            log.error("job is null");
            return;
        }
        /** 自定义变量 */
        String rawText = null;
        JSONObject jsonObject = null;
        rawText = page.getRawText();
        if (StringUtils.isBlank(rawText)) {
            log.error("response body is null");
            return;
        }
        try {
            jsonObject = JSONObject.parseObject(rawText);
            JSONObject jsonData = jsonObject.getJSONObject("data");
            if (null != jsonData) {
                if (StringUtils.isEmpty(jsonData.getString("page_list"))
                        || "[]".equals(jsonData.getString("page_list"))) {
                    return;
                }
                List<JSONObject> jsonObjectList = (List) jsonData.getJSONArray("page_list");
                List<CommentText> commentTexts = Lists.newArrayListWithCapacity(jsonObjectList.size());
                jsonObjectList.stream().forEach(json -> analysis(json, page, job, commentTexts));

                if (CollectionUtils.isNotEmpty(commentTexts)) {
                    putModel(page, commentTexts);
                }
                /**生成下一任务*/
                createNextJob(page, job, jsonObjectList);
            } else {
                throw new JSONException("cannot analysis comment text by url :[" + page.getUrl() + "]");
            }
        } catch (JSONException e) {
            log.error(e.getMessage(), e);
            jsonObject = null;
            return;
        } catch (Exception e) {
            log.error("unknow exception! by comment text :[" + page.getUrl() + "]");
            log.error(e.getMessage(), e);
            return;
        }

    }

    private void createNextJob(Page page, Job job, List<JSONObject> jsonObjectList) {
        try {
            if (jsonObjectList == null || jsonObjectList.isEmpty()) {
                return;
            }
            //当前进度
            Integer current = Integer.valueOf(page.getUrl().regex("&pn=(\\d*)").get());
            //计算下一个任务的进度
            final Integer nextProgress = INCREMENT_SCOPE + current;
            //创建递归任务
            final String nextUrl = page.getUrl().replace("&pn=(\\d*)", String.format("&pn=%s", nextProgress)).get();
            Job newJob = DbEntityHelper.deriveNewJob(job, nextUrl);
            newJob.setFrequency(FrequencyConstant.BARRAGE_TEXT);
            putModel(page, newJob);
            log.debug("created pptv next job url successful!" + nextUrl);
        } catch (Exception e) {
            logger.error("create pptv comments content job failed," + page.getUrl(), e);
        }
    }

    /**
     * 解析评论文本
     *
     * @param json
     * @param page
     * @param job
     */
    private void analysis(JSONObject json, Page page, Job job, List<CommentText> cts) {
        try {
            String commentId = json.getString("id");//评论ID
            Integer up = json.getInteger("up_ct");//点赞量
            Integer replyCount = json.getInteger("reply_ct");//回复数
            String content = json.getString("content");//评论文本
            Long create_time = json.getLong("create_time");//文本创建时间
            JSONObject userJson = json.getJSONObject("user");
            String nickName = "";
            if (null != userJson) {
                nickName = userJson.getString("nick_name");//昵称
            }
            if (replyCount > 0) {
                //TODO 从这里拿到的回复评论只有5个，再多的话看以后再想办法弄上
                List<JSONObject> replys = (List) json.getJSONArray("replys");
                for (JSONObject r : replys) {
                    String subCommentId = r.getString("id");//评论ID
                    Integer subUp = r.getInteger("up_ct");//点赞量
                    Integer subReplyCount = r.getInteger("reply_ct");//回复数
                    String subContent = r.getString("content");//评论文本
                    Long sub_create_time = r.getLong("create_time");//文本创建时间
                    String pid = r.getString("pid");
                    JSONObject subUserJson = r.getJSONObject("user");
                    String subNickName = "";
                    if (null != subUserJson) {
                        subNickName = subUserJson.getString("nick_name");//昵称
                    }
                    CommentText subCommentText = new CommentText();
                    subCommentText.setCommentId(subCommentId);
                    subCommentText.setUp(subUp);
                    subCommentText.setReplyCount(subReplyCount);
                    subCommentText.setContent(subContent);
                    subCommentText.setCreatedTime(new Timestamp(sub_create_time));
                    subCommentText.setNickName(subNickName);
                    subCommentText.setReplyCommentId(pid);
                    DbEntityHelper.derive(job, subCommentText);
                    cts.add(subCommentText);
                }
            }
            CommentText commentText = new CommentText();
            commentText.setCommentId(commentId);
            commentText.setUp(up);
            commentText.setReplyCount(replyCount);
            commentText.setContent(content);
            commentText.setCreatedTime(new Timestamp(create_time));
            commentText.setNickName(nickName);
            DbEntityHelper.derive(job, commentText);
            cts.add(commentText);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
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
