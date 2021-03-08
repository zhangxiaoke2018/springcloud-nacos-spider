package com.jinguduo.spider.spider.sogou;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.jinguduo.spider.cluster.downloader.listener.ResetCookieDownloaderListener;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.spider.listener.UserAgentSpiderListener;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.type.SequenceCounterQuota;
import com.jinguduo.spider.common.util.Md5Util;
import com.jinguduo.spider.data.table.SougouWechatArticleText;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.selector.Html;

import lombok.extern.slf4j.Slf4j;

/**
 * I'm a cute code dog
 * User: LingChen
 * Date:2017/6/27
 * Time:13:23
 */
@Worker
@Slf4j
public class WechatArticleSpider extends CrawlSpider {

    private PageRule rule = PageRule.build()
            .add("", page -> analyzeWechatArticle(page));

    private Site site = SiteBuilder.builder()
            .setDomain("mp.weixin.qq.com")
            // Cookie动态化
            .addDownloaderListener(new ResetCookieDownloaderListener()
                    .addAbnormalStatusCode(403, 404, 500, 501, 502)
                    .setQuota(new SequenceCounterQuota(2))
                    .setProbability(0.01))
            // user-agent 动态化
            .addSpiderListener(new UserAgentSpiderListener())
            .build();

    public void analyzeWechatArticle(Page page) {
        Job job = ((DelayRequest) page.getRequest()).getJob();

        Html html = page.getHtml();
        Document document = html.getDocument();
        SougouWechatArticleText sougouText = new SougouWechatArticleText();
        sougouText.setUrl(job.getUrl());
        sougouText.setContent(document.toString());
        Element title = document.getElementById("activity-name");
        sougouText.setTitle(title.text());
        
        Elements scripts = document.getElementsByTag("script");
        if (null == scripts || scripts.size() == 0) {
            log.warn("mp.weixin.qq.com warn ,this biz and mid is null ，this url is -->{},this script is -->{}", job.getUrl(), scripts);
            return;
        }
        Map<String, String> bizAndMid = this.getBizAndMid(scripts);
        if (null == bizAndMid || bizAndMid.size() < 2) {
            Elements tips = document.getElementsByClass("tips");
            if (null != tips && tips.size() > 0) {
                String tip = tips.get(0).text();
                log.warn("mp.weixin.qq.com warn ,this biz and mid is null because the article is deleted(作者删除，内容违法),this url is -->{},the tips is -->{}", job.getUrl(), tip);
            } else {
                log.warn("mp.weixin.qq.com warn , this biz and mid is null beacuse the article url time out ,this url is -->{}", job.getUrl());
            }
            return;
        }
        String id = Md5Util.getMd5(bizAndMid.get("biz") + bizAndMid.get("mid"));
        sougouText.setId(id);
        putModel(page, sougouText);
    }

    private Map<String, String> getBizAndMid(Elements scripts) {
        Element script = scripts.stream()
                .filter(p -> p.data().toString().contains("biz") && p.data().toString().contains("mid"))
                .findFirst()
                .orElse(null);
        if (script == null) {
            log.error("TAG: BadNull " + scripts.toString());
            return null;
        }
        /*biz和mid*/
        Map<String, String> map = new HashMap<String, String>();
        //log.debug("WechatArticleSpider-103 null scripts : {}", scripts);
        String[] data = script.data().toString().split("var");
        for (String variable : data) {
            /*取到满足条件的JS变量*/
            if (variable.contains("biz") || variable.contains("mid")) {
                String[] kvp = variable.split("=");
                /*取得JS变量存入map*/
                if (!map.containsKey(kvp[0].trim())) {
                    String val = kvp[1].replace(" ", "")
                            .replace("\"", "")
                            .replace("|", "")
                            .replace(";", "")
                            .trim();
                    if (StringUtils.isBlank(val)) {
                        continue;
                    }
                    map.put(kvp[0].trim(), val);
                }
            }
        }
        return map;
    }

    @Override
    public PageRule getPageRule() {
        return this.rule;
    }

    @Override
    public Site getSite() {
        return this.site;
    }
}
