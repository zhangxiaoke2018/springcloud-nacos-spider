package com.jinguduo.spider.spider.tudou;

import java.util.ArrayList;
import java.util.List;

import lombok.extern.apachecommons.CommonsLog;

import org.apache.commons.collections.CollectionUtils;

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
import com.jinguduo.spider.common.constant.FrequencyConstant;
import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.data.text.BarrageText;
import com.jinguduo.spider.webmagic.Page;


//已下架爬虫任务
//@Worker
@CommonsLog
@Deprecated
public class TudouBarrageSpider extends CrawlSpider {

    private final int INCREASE_SCOPE = 1;//请求增长页数
    private Site site = SiteBuilder.builder()
            .setDomain("service.danmu.tudou.com")
            .setCharset("UTF-8")
            .build();

    private static final String POTATO_URL = "http://service.danmu.tudou.com/list?ct=1001&uid=0&type=1&mat=11&iid=133049677&mcount=1";
    
    PageRule rule = PageRule.build().
            add("list",page -> dealBarrageContent(page));
    
    private void dealBarrageContent(Page page) {
        if (null == page)
            return;
        Job job = ((DelayRequest) page.getRequest()).getJob();
        JSONObject jsonObj = JSON.parseObject(page.getRawText());
        try {
            JSONArray jsonArray = jsonObj.getJSONArray("result");
            List<JSONObject> jsonList = null;
            if (null != jsonArray && jsonArray.size() > 0) {
                jsonList = new ArrayList<JSONObject>();
                for (Object obj : jsonArray) {
                    jsonList.add((JSONObject) obj);
                }
            }
            //将List中的弹幕JSON对象封装到弹幕实体DanmuLogs中
            if (CollectionUtils.isNotEmpty(jsonList)) {
                List<BarrageText> danmakuLogsList = Lists.newArrayListWithCapacity(jsonList.size());

                jsonList.stream().forEach(jsonObject -> analysis(page, danmakuLogsList, job, jsonObject));
                putModel(page, danmakuLogsList);
            }
            createNextJob(page, job, jsonList);
            
        } catch (Exception e) {
            log.error(job.getUrl(),e);
        }
    }

    /**
     * 解析弹幕数据并封装到弹幕实体中
     * playat 1000为1秒
     *
     */
    private void analysis(Page page, List<BarrageText> danmakuLogsList, Job job, JSONObject json) {
        try{
            BarrageText barrageText = new BarrageText();
            barrageText.setUserId(json.getString("uid"));//用户id
            barrageText.setContent(json.getString("content"));//弹幕内容
            barrageText.setBarrageId(json.getString("id"));//弹幕id
            barrageText.setShowTime(json.getLong("playat") / 1000L);//弹幕出现时间
            barrageText.setCreatedTime(json.getTimestamp("createtime"));
            // TODO: 2016/12/28  弹幕点赞量字段未确定，后续再补
            DbEntityHelper.derive(job, barrageText);
            danmakuLogsList.add(barrageText);
        }catch (Exception e) {
           log.error(e.getMessage(),e);
        }
    }
    
    private final static int MAX_SCOPE = 1 * 60 * 60;

    /**
     * 生成下一个任务
     *
     * @param page
     * @param oldJob
     */
    private void createNextJob(Page page, Job oldJob, List<JSONObject> list) {
        try {
            //当前进度
            int currentProgress = Integer.valueOf(page.getUrl().regex("\\&mat\\=(\\d*)\\&", 1).get());
            
            // 跳过Job生成: 时间超出一小时，且弹幕文本为空
            if (currentProgress >= MAX_SCOPE && (list == null || list.isEmpty())) {
                return;
            }
            List<Job> jobs = Lists.newArrayList();
            //下一个任务的进度
            final int nextJob_scope = INCREASE_SCOPE + currentProgress;
            //创建递归任务
            String nextUrl = page.getUrl().replace("mat\\=(\\d*)", String.format("mat=%S", nextJob_scope)).get();
            
            Job newJob = DbEntityHelper.deriveNewJob(oldJob, nextUrl);
            newJob.setFrequency(FrequencyConstant.BARRAGE_TEXT);
            jobs.add(newJob);

            Job newJob1 = DbEntityHelper.deriveNewJob(
                    oldJob,
                    page.getUrl().replace("mat\\=(\\d*)", String.format("mat=%S", INCREASE_SCOPE + nextJob_scope)).get()
            );
            newJob1.setFrequency(FrequencyConstant.BARRAGE_TEXT);
            jobs.add(newJob1);


            putModel(page,jobs);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
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
