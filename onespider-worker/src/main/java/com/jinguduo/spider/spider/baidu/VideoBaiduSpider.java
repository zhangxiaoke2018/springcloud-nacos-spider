package com.jinguduo.spider.spider.baidu;

import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.worker.Worker;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.common.util.RegexUtil;
import com.jinguduo.spider.data.table.BaiduVideoLog;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.selector.Html;

import lombok.extern.apachecommons.CommonsLog;

/**
 * Created by csonezp on 2016/8/25.
 */
//@Worker
@CommonsLog
public class VideoBaiduSpider extends CrawlSpider {

    private Site site = SiteBuilder.builder().setDomain("v.baidu.com").build();
    private PageRule rule = PageRule.build()
            .add(".", page -> getSubList(page))
            ;

    /**
     * 获取该剧的分集列表，添加JOB
     *
     * @param page
     * @throws JSONException 
     */
    private void getSubList(Page page) throws JSONException {
        Html html = page.getHtml();
        String url = page.getUrl().toString();
        Job oldJob = ((DelayRequest) page.getRequest()).getJob();
        String cacheData = RegexUtil.getDataByRegex(html.toString(), "var cacheData = (\\{.*\\})\\|");

        if ( StringUtils.isBlank(cacheData) ) {
            log.error("Has no macth param，page no result by url :["+page.getUrl()+"]");
            return;
        }

        JSONObject allData = new JSONObject(cacheData);
        Integer playCount = allData.optInt("dispNum");
        JSONArray jsonArray = allData.getJSONObject("filters").getJSONArray("cond_site");
        int length = jsonArray.length();
        for (int i = 0; i < length; i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            String title = jsonObject.getString("title");
            String surfix = jsonObject.getString("surfix");
            Integer status = jsonObject.getInt("status");
            title.trim();
            //status为1时即为当前的平台
            if (status == 1) {
                BaiduVideoLog baiduVideoLog = new BaiduVideoLog();
                baiduVideoLog.setPlatformName(title);
                baiduVideoLog.setCode(oldJob.getCode());
                baiduVideoLog.setCount(playCount);
                putModel(page,baiduVideoLog);
            }
            //如果该平台title不含全部，且此时的url是总平台的url，也就是不加surfix的，则将子平台的url加到job里
            if (!StringUtils.contains(title, "全部") && !StringUtils.contains(url, "sc=") && status!=2) {
                Job job = new Job(url + "&" + surfix);
                DbEntityHelper.derive(oldJob, job);
                putModel(page, job);
            }
        }
    }

    @Override
    public PageRule getPageRule() {
        return rule;
    }

    @Override
    public Site getSite() {
        return site;
    }
}
