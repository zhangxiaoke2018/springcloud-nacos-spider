package com.jinguduo.spider.spider.kanmanhua;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.util.DateUtil;
import com.jinguduo.spider.data.table.ComicKanmanhua;
import com.jinguduo.spider.webmagic.Page;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;


@Slf4j
@Worker
public class KanmanhuaInfoSpider extends CrawlSpider {

    private Site site = SiteBuilder.builder()
            .setDomain("comic.321mh.com")
            .build();

    private PageRule rules = PageRule.build()
            .add("/getcomicinfo_influence/", page -> getInfo(page));


    public void getInfo(Page page){
        Job job = ((DelayRequest) page.getRequest()).getJob();
        JSONObject json = JSONObject.parseObject(page.getJson().get());
        JSONObject list = json.getJSONObject("data");
        JSONObject ceallData = list.getJSONObject("call_data");
        ComicKanmanhua kan = new ComicKanmanhua();
        String code = job.getCode();
        kan.setCode(code);
        kan.setDay(DateUtil.getDayStartTime(new Date()));
        kan.setGift(ceallData.getInteger("gift"));
        kan.setYuepiao(ceallData.getInteger("ticket"));
        kan.setPingfen(ceallData.getFloat("score"));
        kan.setTuijian(ceallData.getInteger("recommend"));
        kan.setDashang(ceallData.getInteger("reward"));
        kan.setShare(ceallData.getInteger("share"));
        putModel(page,kan);
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
