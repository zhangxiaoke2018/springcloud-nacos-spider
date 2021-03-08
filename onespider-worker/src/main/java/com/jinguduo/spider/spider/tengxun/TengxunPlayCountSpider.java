package com.jinguduo.spider.spider.tengxun;

import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.data.table.ShowLog;
import com.jinguduo.spider.webmagic.Page;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 16/6/14 下午2:49
 */
@Worker
public class TengxunPlayCountSpider extends CrawlSpider {

    private Site site = SiteBuilder.builder().setDomain("sns.video.qq.com").build();
    
    private PageRule rule = PageRule.build().add("", page -> processPage(page));

    public void processPage(Page page) {
        String rep = page.getJson().toString();

        JSONObject json = JSONObject.parseObject(rep.substring(rep.indexOf("=") + 1, rep.lastIndexOf(";")));

        List<JSONObject> playNums = (List<JSONObject>) json.get("node");

        List<ShowLog> showLogs = Lists.newArrayList();

        for (JSONObject playNum : playNums) {
            Long play = playNum.getLong("all");

            Job job = ((DelayRequest) page.getRequest()).getJob();
            if (job != null) {
                ShowLog showLog = new ShowLog();
                DbEntityHelper.derive(job, showLog);
                showLog.setPlayCount(play);
                showLog.setCode(job.getCode());
                showLogs.add(showLog);
            }
        }
        putModel(page,showLogs);
    }

    @Override
    public Site getSite() {
        return this.site;
    }

    @Override
    public PageRule getPageRule() {
        return this.rule;
    }
}
