package com.jinguduo.spider.spider.tengxun;

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

import lombok.extern.apachecommons.CommonsLog;

import org.apache.http.client.config.CookieSpecs;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 16/7/11 下午5:22
 */
@Worker
@CommonsLog
public class TengxunTotalPlayCountSpider extends CrawlSpider {

    private Site site = SiteBuilder.builder()
            .setDomain("data.video.qq.com")
            .addSpiderListener(new UserAgentSpiderListener())
            .setCookieSpecs(CookieSpecs.IGNORE_COOKIES)
            .build();

    private static final String VARIETY_JOB = "http://v.qq.com/variety/column/column_%s.html";

    private PageRule rule = PageRule.build()
            .add(".", page -> getCount(page));

    public void getCount(Page page) {

        Job oldJob = ((DelayRequest) page.getRequest()).getJob();

        JSONObject json = JSONObject.parseObject(
                page.getRawText().substring(
                        page.getRawText().indexOf("(") + 1, page.getRawText().lastIndexOf(")")
                )
        );

        JSONArray results = json.getJSONArray("results");
        if (results == null) {
            log.warn("get results is null from url [" + page.getRequest().getUrl() + "]");
            return;
        }

        JSONObject fields = json.getJSONArray("results")
                .getObject(0, JSONObject.class)
                .getJSONObject("fields");

        //column不存在时fields中只有view_all_count一个属性，
        // 因此在处理前判断是否有column存在
        JSONObject column = null;
        if(fields.containsKey("column")){
            column = fields.getJSONObject("column");
        }

        Long playCount = null;
        if (column == null) {
            // 网剧==网大
            playCount = fields.getLong("allnumc");
            if (playCount == null) {
                playCount = fields.getLong("view_all_count");
            }
        } else {
            // 网综的code均为不为0的数字
            //可直接从该字段获取到总播，若获取不到则走下面的页面,这样就避免了特殊综艺的问题
            JSONObject ccv = column.getJSONObject("c_column_view");
            if (ccv != null) {
                playCount = ccv.getLong("c_allnumc");
                if(playCount == null){
                    Integer columnId = ccv.getInteger("c_column_id");
                    Job job = new Job(String.format(VARIETY_JOB, columnId));
                    DbEntityHelper.derive(oldJob, job);
                    putModel(page,job);
                }
            }
        }

        if (playCount != null) {
            ShowLog showLog = new ShowLog();
            DbEntityHelper.derive(oldJob, showLog);
            showLog.setPlayCount(playCount);
            putModel(page,showLog);
        }
    }

    @Override
    public Site getSite() {
        return this.site;
    }

    @Override
    public PageRule getPageRule() {
        return rule;
    }
}
