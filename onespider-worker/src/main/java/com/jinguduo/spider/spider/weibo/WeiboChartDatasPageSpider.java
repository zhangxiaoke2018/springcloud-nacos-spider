package com.jinguduo.spider.spider.weibo;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.exception.AntiSpiderException;
import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.data.table.ExponentLog;
import com.jinguduo.spider.data.table.WeiboIndexHourLog;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.selector.PlainText;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;

import java.sql.Date;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;

@Worker
@Slf4j
public class WeiboChartDatasPageSpider extends CrawlSpider {


    private Site site = SiteBuilder.builder()
            .setDomain("data.weibo.com")
            .addHeader("Referer","http://data.weibo.com/index/newindex?visit_type=search")
            .build();

    private PageRule rule = PageRule.build()
            .add("newindex", page -> newIndexWid(page))
            .add("1month", page -> newTendency(page))//热词趋势数据处理
            .add("1day", page -> newTendencyHour(page))
            ;




    private void newIndexWid(Page page) {
        Job job = ((DelayRequest) page.getRequest()).getJob();

        JSONObject jsonObject = JSONObject.parseObject(page.getRawText());
        Integer code = jsonObject.getInteger("code");

        if(code > 100){
//            throw new AntiSpiderException("微博未收录此关键词:"+page.getRequest().getUrl() + page.getRawText());
            return;
        } else {
            String html = jsonObject.getString("html");
            try {
                String wid = new PlainText(html).regex("wid=\"(.*?)\"", 1).get();

                Job newJob = new Job();
                newJob.setUrl("http://data.weibo.com/index/ajax/newindex/getchartdata?dateGroup=1month&wid="+wid);
                DbEntityHelper.derive(job, newJob);
                putModel(page, newJob);

                Job newJobHour = new Job();
                newJobHour.setUrl("http://data.weibo.com/index/ajax/newindex/getchartdata?dateGroup=1day&wid="+wid);
                DbEntityHelper.derive(job, newJobHour);
                putModel(page, newJobHour);

            } catch (Exception ex) {
                throw new AntiSpiderException("微博未收录此关键词:"+page.getRequest().getUrl());
            }
        }

    }



    private void newTendency(Page page) throws ParseException {

        if ("csrf".equals(page.getRawText().trim())) {
            log.error("the analysisChartProcess error, url : " + page.getUrl().get() + "; value: " + page.getRawText());
            return;
        }

        Job job = ((DelayRequest) page.getRequest()).getJob();

        String rawText = page.getRawText();
        JSONObject jsonObject = JSONObject.parseObject(rawText);

        Integer code = jsonObject.getInteger("code");

        if(code == null) {
            return;
        }

        if(code == 100) {

            JSONObject data = jsonObject.getJSONArray("data").getObject(0, JSONObject.class);
            JSONObject trend = data.getJSONObject("trend");
            String endDayStr = trend.getJSONObject("last").getString("daykey");
            java.util.Date endDay = DateUtils.parseDate(endDayStr, "yyyyMMdd");
            JSONArray dates = trend.getJSONArray("x");
            JSONArray nums = trend.getJSONArray("s");

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(endDay);
            calendar.add(Calendar.DATE, -dates.size());
            for (int i = 0; i < dates.size(); i++) {

                calendar.add(Calendar.DATE, 1);
                Long num = nums.getLong(i);
                ExponentLog exponentLog = new ExponentLog();
                exponentLog.setExponentNum(num);
                exponentLog.setExponentDate(new Date(calendar.getTime().getTime()));

                DbEntityHelper.derive(job, exponentLog);
                putModel(page, exponentLog);
            }
        }
    }

    private void newTendencyHour(Page page) throws ParseException {
        if ("csrf".equals(page.getRawText().trim())) {
            log.error("the analysisChartProcess error, url : " + page.getUrl().get() + "; value: " + page.getRawText());
            return;
        }

        Job job = ((DelayRequest) page.getRequest()).getJob();

        String rawText = page.getRawText();
        JSONObject jsonObject = JSONObject.parseObject(rawText);

        Integer code = jsonObject.getInteger("code");

        if(code == null) {
            return;
        }

        if(code == 100) {

            JSONObject data = jsonObject.getJSONArray("data").getObject(0, JSONObject.class);
            JSONObject trend = data.getJSONObject("trend");

            String endDayStr = trend.getJSONObject("last").getString("daykey");
            java.util.Date endDay = DateUtils.parseDate(endDayStr, "yyyyMMddHH");

            ArrayList dates = trend.getObject("x", ArrayList.class);
            ArrayList nums = trend.getObject("s", ArrayList.class);

            Calendar calendar = Calendar.getInstance();
            calendar.setTime(endDay);
            calendar.add(Calendar.HOUR, -dates.size());
            for (int i = 0; i < dates.size(); i++) {

                calendar.add(Calendar.HOUR, 1);
                Long num = Long.valueOf(nums.get(i).toString());
                WeiboIndexHourLog weiboIndexHourLog = new WeiboIndexHourLog();
                weiboIndexHourLog.setIndexCount(num);
                weiboIndexHourLog.setHour(calendar.getTime());
                weiboIndexHourLog.setCode(job.getCode());

                putModel(page, weiboIndexHourLog);
            }


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
