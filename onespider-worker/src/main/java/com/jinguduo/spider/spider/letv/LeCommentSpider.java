package com.jinguduo.spider.spider.letv;

import java.sql.Timestamp;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSON;
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
 * Created by gsw on 2017/2/20.
 */
@Worker
public class LeCommentSpider extends CrawlSpider {

    private Site sites = SiteBuilder.builder().setDomain("api.my.le.com").build();

    /**评论文本URL*/
    private final String COMMENT_CONTENT_URL = "http://api.my.le.com/vcm/api/list?rows=20&page=1&listType=1&xid=27647086&pid=10034722";

    private static Integer INCRMENT_SCOPE = 1;//请求增长区间

    private PageRule rules = PageRule.build()
            .add("vcm",page -> getCommentContent(page));

    /**
     * 获取评论文本
     * @param page
     */
    private void getCommentContent(Page page) {
        Job job = ((DelayRequest)page.getRequest()).getJob();
        String rawText = page.getRawText();
        JSONObject json = JSON.parseObject(rawText);
        if(StringUtils.isEmpty(json.getString("data"))
                ||"{}".equals(json.getString("data"))||"[]".equals(json.getString("data"))
                ||StringUtils.equals(json.getString("data"),"internal error")) {
            return ;
        }
            List<JSONObject> jsonObjectList = (List) json.getJSONArray("data");
            List<CommentText> commentTexts = Lists.newArrayListWithCapacity(jsonObjectList.size());
            if(CollectionUtils.isNotEmpty(jsonObjectList)) {
                jsonObjectList.stream().forEach(jsonObject ->analysis(page,job,commentTexts,jsonObject) );
            }
            
            if(CollectionUtils.isNotEmpty(commentTexts)){
                putModel(page, commentTexts);
            }
            /**生成下一个抓取文本任务*/
            createNextJob(page,job,jsonObjectList);
    }

    /**
     * 解析评论文本
     * @param page
     * @param job
     * @param cts
     * @param json
     */
    private void analysis(Page page ,Job job,List<CommentText> cts,JSONObject json) {
            String commmentId = json.getString("_id");//评论id
            String content = json.getString("content");//评论文本
            Long create_time = json.getLong("ctime");//创建时间
            Integer up = json.getInteger("like");//点赞量
            Integer replyCount = json.getInteger("replynum");//回复数

            //用户信息
            JSONObject userJson = json.getJSONObject("user");
            String userId ="";//用户ID
            String nickName = "";//昵称
            if(null!=userJson &&userJson.size()>0) {
                userId = userJson.getString("uid");
                nickName = userJson.getString("username");
            }

            CommentText commentText  = new CommentText();
            commentText.setCommentId(commmentId);
            commentText.setContent(content);
            commentText.setUp(up);
            commentText.setReplyCount(replyCount);
            if (create_time != null && String.valueOf(create_time).length() < 13){
                create_time = create_time*1000;
            }
            commentText.setCreatedTime(new Timestamp(create_time));
            commentText.setUserId(userId);
            commentText.setNickName(nickName);
            DbEntityHelper.derive(job,commentText);
            cts.add(commentText);
    }

    private void createNextJob(Page page,Job job,List<JSONObject> jsonObjectList) {
            //当前进度
            Integer current = Integer.valueOf(page.getUrl().regex("&page=(\\d*)").get());
            //跳过Job生成：评论文本为空
            if(jsonObjectList==null ||jsonObjectList.isEmpty()) {
                return;
            }
            //计算下一个任务的进度
            final Integer next_scope = INCRMENT_SCOPE + current;
            //创建递归任务
            final String nextUrl = page.getUrl().replace("&page=(\\d*)", String.format("&page=%s",next_scope)).get();
            Job newJob = DbEntityHelper.deriveNewJob(job,nextUrl);
            newJob.setFrequency(FrequencyConstant.COMMENT_TEXT);
            putModel(page,newJob);
    }
    
    @Override
    public PageRule getPageRule() {
        return rules;
    }

    @Override
    public Site getSite() {
        return sites;
    }
}
