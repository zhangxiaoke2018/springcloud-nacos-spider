package com.jinguduo.spider.spider.kankan;

import java.util.List;
import java.util.Map;

import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.constant.FrequencyConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.data.text.BarrageText;
import com.jinguduo.spider.webmagic.Page;

/**
 * 响巢看看弹幕内容获取
 * Created by gsw on 2016/12/28.
 */
@Worker
public class KankanBarrageContentSpider extends CrawlSpider {

    private static Logger log = LoggerFactory.getLogger(KankanBarrageContentSpider.class);
    private final int INCREASE_SCOPE = 1000;//请求增长区间

    private Site site = SiteBuilder.builder().setDomain("point.api.t.kankan.com").build();

    PageRule rule = PageRule.build()
            .add("danmu",page -> getBarrageContent(page));

    /**
     * 获取弹幕的内容
     */
    private void getBarrageContent(Page page) {
        Job job = ((DelayRequest) page.getRequest()).getJob();
        //1.得到整个Json对象
        String json_Str = page.getRawText();
            json_Str = json_Str.substring(json_Str.indexOf("=") + 1, json_Str.lastIndexOf(";"));
            JSONObject rawObject = JSON.parseObject(json_Str);
            //2.得到关键词为data的Json对象
            JSONObject dataObject = rawObject.getJSONObject("data");
            //3.存放所有的JSONObject
            Map<String, List> jsonObjectMap = Maps.newHashMap();

            //分页起始位置
            int startIndex = Integer.valueOf(page.getUrl().regex("\\&start\\=(\\d*)\\&", 1).get());
            //分页结束位置
            int endIndex = Integer.valueOf(page.getUrl().regex("\\&end\\=(\\d*)\\&", 1).get());
            for (int index = startIndex; index < endIndex; index++) {
                //3.1得到每一页对应的JSONArray
                JSONArray jsonArray = dataObject.getJSONArray(String.valueOf(index));
                //3.2遍历JSONArray，把每个JSONObject存入jsonObjectList中
                if (null != jsonArray && jsonArray.size() > 0) {
                    jsonObjectMap.put(String.valueOf(index), jsonArray);
                }
            }
            //将List中的JSONObject(弹幕)对象封装到弹幕实体DanmuLogs中
            if (jsonObjectMap != null && !jsonObjectMap.isEmpty()) {
                List<BarrageText> danmakuLogsList = Lists.newArrayList();

                for (String str : jsonObjectMap.keySet()) {
                    List values = jsonObjectMap.get(str);
                    for (int i = 0; i < values.size(); i++) {
                        JSONObject json = (JSONObject) JSONObject.toJSON(values.get(i));
                        BarrageText barrageText = new BarrageText();
                        barrageText.setUserId(json.getString("userid"));//用户id
                        barrageText.setNickName(json.getString("nickname"));//用户昵称
                        barrageText.setContent(json.getString("content"));//弹幕内容
                        barrageText.setBarrageId(json.getString("_id"));//弹幕id
                        barrageText.setUp(json.getIntValue("up_num"));//点赞量
                        barrageText.setReplyCount(json.getIntValue("down_num"));//回复数
                        barrageText.setShowTime(Long.parseLong(str));//弹幕开始时间
                        DbEntityHelper.derive(job, barrageText);
                        danmakuLogsList.add(barrageText);
                    }
                }
                putModel(page,danmakuLogsList);
                createNextJob(page, job);

            }

    }

    /**
     * 生成下一个任务
     *
     * @param page
     * @param oldJob
     */
    private void createNextJob(Page page, Job oldJob) {
            List<Job> jobs = Lists.newArrayList();

            //当前进度
            int currentProgress = Integer.valueOf(page.getUrl().regex("\\&end\\=(\\d*)\\&", 1).get());
            //下一个任务的进度
            //下一个任务分页开始
            int nextJobPageStart = currentProgress + 1;
            //下一个任务分页结束
            final int nextJob_scope = INCREASE_SCOPE + nextJobPageStart;
            //创建递归任务
            String nextUrl = page.getUrl().replace("end\\=(\\d*)", String.format("end=%S", nextJob_scope))
                    .replace("start\\=(\\d*)",String.format("start=%S",nextJobPageStart)).get();

            Job job = DbEntityHelper.deriveNewJob(oldJob, nextUrl);
            job.setFrequency(FrequencyConstant.BARRAGE_TEXT);
            jobs.add(job);

            Job job2 = DbEntityHelper.deriveNewJob(oldJob,
                    page.getUrl().replace("end\\=(\\d*)", String.format("end=%S", INCREASE_SCOPE + nextJob_scope))
                            .replace("start\\=(\\d*)", String.format("start=%S", nextJobPageStart + 1)).get()
            );
            job2.setFrequency(FrequencyConstant.BARRAGE_TEXT);

            jobs.add(job2);

            putModel(page,jobs);
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
