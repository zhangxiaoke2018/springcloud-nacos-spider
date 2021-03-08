package com.jinguduo.spider.spider.pptv;

import java.net.URLDecoder;

import lombok.extern.apachecommons.CommonsLog;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.common.util.NumberHelper;
import com.jinguduo.spider.common.util.RegexUtil;
import com.jinguduo.spider.data.table.ShowLog;
import com.jinguduo.spider.webmagic.Page;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @DATE 16/7/19 下午3:09
 */
@Worker
@CommonsLog
public class PPTVSearchSpider extends CrawlSpider {

    private Site site = SiteBuilder.builder().setDomain("search.pptv.com").build();

    private PageRule rules = PageRule.build()
            .add("pptv",page -> processTotalPc(page));

    //获取搜索页总播放量
    private void processTotalPc(Page page){
        Job oldJob = ((DelayRequest) page.getRequest()).getJob();
        String searchTitle = "";
        try{
            searchTitle = URLDecoder.decode(RegexUtil.getDataByRegex(oldJob.getUrl(),"kw=(\\S+)",1), "utf-8");
        }catch(Exception e){
            log.error("PPTVSearchSpider processTotalPc decode title error! url:"+oldJob.getUrl());
        }
        Document document = page.getHtml().getDocument();
        Elements dls = document.getElementsByTag("dl");
        for (Element dl : dls) {
            Element a = dl.getElementsByTag("a").first();
            //a.attr("href").contains(oldJob.getCode()) 不用这个方式，因为录的链接不一定和这个页面标题跳转的链接相同，若一个一个链接比又太麻烦
            //因为已经有api获取总播放量较为稳定了，这个搜索爬虫只是备用，现在把匹配改为搜索关键词完全匹配的方式确定
            if(a != null && a != null && StringUtils.equalsIgnoreCase(a.attr("title"), searchTitle)){//剧名完全匹配则进行数据抓取
                Element parent = a.parent();
                Elements lis = parent.nextElementSibling().getElementsByTag("li");
                for (Element li : lis) {
                    String content = li.text().replace(" ","").replace(",","");
                    if(content.startsWith("播放：")){
                        String pc = content.substring(3,content.length());
                        ShowLog showLog = new ShowLog();
                        DbEntityHelper.derive(oldJob, showLog);
                        showLog.setPlayCount(NumberHelper.parseLong(pc,-1));
                        showLog.setCode(oldJob.getCode());
                        putModel(page,showLog);
                        break;
                    }
                }
            }
        }
    }
    
    @Override
    public PageRule getPageRule() {
        return this.rules;
    }

    @Override
    public Site getSite() {
        return this.site;
    }
}
