package com.jinguduo.spider.spider.kanmanhua;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.util.DateHelper;
import com.jinguduo.spider.common.util.DateUtil;
import com.jinguduo.spider.common.util.TextUtils;
import com.jinguduo.spider.data.table.ComicCommentText;
import com.jinguduo.spider.data.table.ComicKanmanhua;
import com.jinguduo.spider.webmagic.Page;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by lc on 2018/10/15
 */
@Slf4j
@Worker
public class KanmanhuaCommentSpider extends CrawlSpider {
    /*
     * http://community-hots.321mh.com/comment/count/?appId=1&commentType=2&ssid=25934&ssidType=0
     * {"status":1,"msg":"ok","data":858251.0,"servicetime":1539571697}
     * */
    private Site site = SiteBuilder.builder()
            .setDomain("community-hots.321mh.com")
            .build();

    private PageRule rules = PageRule.build()
            .add("/comment/count", page -> getKanmanhuaComment(page))
            .add("/comment/hotlist",page -> getKanmanhuaHotCommentText(page))
            .add("/comment/newgets",page ->getKanmanhuaNewsCommentText(page));

    private void getKanmanhuaHotCommentText(Page page){
        Job job = ((DelayRequest) page.getRequest()).getJob();
        String code = job.getCode();
        JSONObject jsonObject = JSONObject.parseObject(page.getRawText());
        JSONArray  data = jsonObject.getJSONArray("data");
        for(int k = 0;k<data.size();k++){
             JSONObject comment = data.getJSONObject(k);
            ComicCommentText c = new ComicCommentText();
            c.setCommentId(comment.getLong("id"));
            c.setUserName(TextUtils.removeEmoji(comment.getString("uname")));
            c.setUserId(comment.getString("uid"));
            c.setSupportCount(comment.getLong("supportcount"));
            c.setRevertCount(comment.getInteger("revertcount"));
            c.setPlatformId(36);
            Date date = new Date(comment.getLong("createtime"));
            c.setCommentCreateTime(date);
            String content = TextUtils.removeEmoji(comment.getString("content"));
            if(content.equals("")){continue;}
            c.setContent(content);
            c.setDay(DateUtil.getDayStartTime(new Date()));
            c.setCode(code);
            putModel(page,c);
        }
    }

    //新评论无uname uid
    private void getKanmanhuaNewsCommentText(Page page){
        Job job = ((DelayRequest) page.getRequest()).getJob();
        String code = job.getCode();
        JSONObject jsonObject = JSONObject.parseObject(page.getRawText());
        JSONArray  data = jsonObject.getJSONArray("data");
        for(int k = 0;k<data.size();k++){
            JSONObject comment = data.getJSONObject(k);
            ComicCommentText c = new ComicCommentText();
            c.setCommentId(comment.getLong("id"));
            c.setSupportCount(comment.getLong("supportcount"));
            c.setRevertCount(comment.getInteger("revertcount"));
            c.setPlatformId(36);
            Date date = new Date(comment.getLong("createtime"));
            c.setCommentCreateTime(date);
            String content = TextUtils.removeEmoji(comment.getString("content"));
            if(content.equals("")){continue;}
            c.setContent(content);
            c.setDay(DateUtil.getDayStartTime(new Date()));
            c.setCode(code);
            putModel(page,c);
        }

    }

    private void getKanmanhuaComment(Page page) {
        Job job = ((DelayRequest) page.getRequest()).getJob();
        String code = job.getCode();
        JSONObject jsonObject = JSONObject.parseObject(page.getRawText());
        if (!jsonObject.getInteger("status").equals(1)){
            return;
        }
        BigDecimal data = jsonObject.getBigDecimal("data");
        Integer commentCount = data.intValue();

        ComicKanmanhua kan = new ComicKanmanhua();
        kan.setCode(code);
        kan.setDay(DateUtil.getDayStartTime(new Date()));
        kan.setCommentCount(commentCount);

        //log.info("kanmanhua comment code ：" +code);

        putModel(page, kan);
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
