package com.jinguduo.spider.spider.sohu;

import org.apache.http.client.config.CookieSpecs;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.spider.listener.UserAgentSpiderListener;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.data.table.ShowLog;
import com.jinguduo.spider.webmagic.Page;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 13/06/2017 13:37
 */
@Worker
public class SohuApiSpider extends CrawlSpider {


    private Site site = SiteBuilder.builder()
            .setDomain("api.tv.sohu.com")
            .setCookieSpecs(CookieSpecs.IGNORE_COOKIES)
            .addSpiderListener(new UserAgentSpiderListener())
            .build();


    private PageRule rules = PageRule.build()
            .add(".", page -> processSelf(page));

    private void processSelf(Page page) {

        Job job = ((DelayRequest) page.getRequest()).getJob();

        JSONObject jsonObject = page.getJson().toObject(JSONObject.class);
        JSONArray columns = jsonObject.getObject("data", JSONObject.class).getJSONArray("columns");

        Long playCount = 0L;

        for (int i = 0; i < columns.size(); i++) {
            JSONObject column = columns.getJSONObject(i);
            Integer id = column.getInteger("id");
            if(job.getCode().equals(String.valueOf(id))){
                playCount = column.getLong("playCount");
                break;
            }
        }

        ShowLog showLog = new ShowLog();
        DbEntityHelper.derive(job, showLog);
        showLog.setPlayCount(playCount);
        putModel(page, showLog);

    }

    @Override
    public Site getSite() {
        return this.site;
    }

    @Override
    public PageRule getPageRule() {
        return this.rules;
    }
}
