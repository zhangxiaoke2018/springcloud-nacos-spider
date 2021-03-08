package com.jinguduo.spider.spider.bilibili;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.data.text.BarrageText;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.selector.Html;
import com.jinguduo.spider.webmagic.selector.Selectable;
import lombok.extern.apachecommons.CommonsLog;

/**
 * Created by jack on 2017/7/10.
 */

@Worker
@CommonsLog
public class BiliBiliDanmuSpider extends CrawlSpider{

    private Site site = SiteBuilder.builder().setDomain("comment.bilibili.com").build(); //https://comment.bilibili.com/658955.xml
    private PageRule rules = PageRule.build()
            .add(".",page -> getContent(page));

    private void getContent(Page page){
        Job oldJob = ((DelayRequest)page.getRequest()).getJob();
        Html html = page.getHtml();
        List<BarrageText> barrageTextList = new ArrayList<>();
        List<Selectable> selectableList =  html.xpath("//d").nodes();
        selectableList.forEach( selectable -> {
            String[] atts = selectable.xpath("///@p").toString().split(",");
            String nodeValue = selectable.xpath("///text()").toString();
            BarrageText barrageText = new BarrageText();
            barrageText.setShowTime(Double.valueOf(atts[0]).longValue());
            barrageText.setCreatedTime(new Timestamp(Long.valueOf(atts[4])*1000));
            barrageText.setUserId(atts[6]);
            barrageText.setBarrageId(atts[7]);
            barrageText.setContent(nodeValue);
            DbEntityHelper.derive(oldJob,barrageText);
            barrageTextList.add(barrageText);
        });
        //log.info("b站弹幕抓取，code :"+oldJob.getCode()+"。");
        putModel(page, barrageTextList);
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
