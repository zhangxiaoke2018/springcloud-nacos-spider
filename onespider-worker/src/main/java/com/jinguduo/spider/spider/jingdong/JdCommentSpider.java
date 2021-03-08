package com.jinguduo.spider.spider.jingdong;

import com.alibaba.fastjson.JSONObject;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.util.DateUtil;
import com.jinguduo.spider.data.table.JdGoods;
import com.jinguduo.spider.webmagic.Page;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

/**
 * Created by lc on 2019/10/31
 * host = club.jd.com
 */
@Worker
@Slf4j
public class JdCommentSpider extends CrawlSpider {

    private Integer JD_PLATFORM_ID = 58;

    private Site site = SiteBuilder.builder().setDomain("club.jd.com")
            .addHeader("referer", "https://book.jd.com/")
            .build();
    private PageRule rules = PageRule.build()
            .add("comment", page -> getComments(page));


    private void getComments(Page page) {
        Job job = ((DelayRequest) page.getRequest()).getJob();

        JSONObject jsonObject = JSONObject.parseObject(page.getRawText());
        JSONObject product = jsonObject.getJSONObject("productCommentSummary");
        Integer commentCount = product.getInteger("commentCount");
        Integer afterCount = product.getInteger("afterCount");
        Integer goodCount = product.getInteger("goodCount");
        Integer defaultGoodCount = product.getInteger("defaultGoodCount");
        Integer generalCount = product.getInteger("generalCount");
        Integer poorCount = product.getInteger("poorCount");
        String goodsId = job.getCode();

        JdGoods jd = new JdGoods();
        jd.setDay(DateUtil.getDayStartTime(new Date()));
        jd.setGoodsId(goodsId);
        jd.setCommentCount(commentCount);
        jd.setAfterCount(afterCount);
        jd.setGoodCount(goodCount);
        jd.setDefaultGoodCount(defaultGoodCount);
        jd.setGeneralCount(generalCount);
        jd.setPoorCount(poorCount);

        putModel(page,jd);

    }


    @Override
    public Site getSite() {
        return site;
    }

    @Override
    public PageRule getPageRule() {
        return rules;
    }

}
