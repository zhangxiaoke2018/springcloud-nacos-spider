package com.jinguduo.spider.spider.fiction;

import com.alibaba.fastjson.JSONObject;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.constant.CommonEnum;
import com.jinguduo.spider.data.table.FictionPlatformClick;
import com.jinguduo.spider.webmagic.Page;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by lc on 2019/7/1
 */
@Worker
@Slf4j
@SuppressWarnings("all")
public class SQDetailSpider extends CrawlSpider {

    private Site site = new SiteBuilder()
            .setDomain("content.shuqireader.com")
            .build();


    private PageRule rule = PageRule.build()
            .add("/book/info", this::processDetail);

    private void processDetail(Page page) {
        JSONObject jsonObject = JSONObject.parseObject(page.getRawText());
        if (200 != jsonObject.getInteger("state").intValue()) return;

        JSONObject data = jsonObject.getJSONObject("data");
        String bookId = data.getString("bookId");

        Long click = data.getLong("numClick");
        FictionPlatformClick fictionClick = new FictionPlatformClick();
        fictionClick.setCode(bookId);
        fictionClick.setClickCount(click);
        fictionClick.setPlatformId(CommonEnum.Platform.SQ.getCode());
        putModel(page, fictionClick);

    }


    @Override
    public Site getSite() {
        // TODO Auto-generated method stub
        return site;
    }

    @Override
    public PageRule getPageRule() {
        // TODO Auto-generated method stub
        return rule;
    }

}
