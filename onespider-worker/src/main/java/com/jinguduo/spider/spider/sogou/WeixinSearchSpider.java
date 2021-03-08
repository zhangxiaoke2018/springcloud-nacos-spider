package com.jinguduo.spider.spider.sogou;

import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.spider.listener.UserAgentSpiderListener;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.exception.AntiSpiderException;
import com.jinguduo.spider.common.util.DateUtil;
import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.common.util.Md5Util;
import com.jinguduo.spider.data.table.SougouWechatArticleText;
import com.jinguduo.spider.data.table.SougouWechatSearchText;
import com.jinguduo.spider.data.table.bookProject.WechatBookLogs;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.selector.Html;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * 1/只获取第一页
 * 2/需要顺序
 * 3/文章的标题，摘要，链接...
 */
@Slf4j
@Worker
public class WeixinSearchSpider extends CrawlSpider {

    private PageRule rule = PageRule.build()
            .add("com/weixin", page -> analyzeWeixinSearchCount(page))
            .add("share", page -> analyzeWechatArticle(page));


    private Site site = SiteBuilder.builder()
            .setDomain("weixin.sogou.com")
            .addSpiderListener(new WechatCookieDownloaderListener())
            .addSpiderListener(new UserAgentSpiderListener())
            // user-agent 动态化
            //.setCookieSpecs(CookieSpecs.IGNORE_COOKIES)
            .addHeader("Upgrade-Insecure-Requests", "1")
            .addHeader("Referer", "http://weixin.sogou.com/weixin?type=2&s_from=input&query=%E7%94%84%E5%AC%9B%E4%BC%A0")
            .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36")
//            .addDownloaderListener(new ResetCookieDownloaderListener()
//                    .addAbnormalStatusCode(403, 404, 500, 501, 502)
//                    .setQuota(new SequenceCounterQuota(2))
//                    .setProbability(0.01))
            .build();


    /**
     * 解析搜狗微信页
     *
     * @param page
     * @throws AntiSpiderException
     */
    public void analyzeWeixinSearchCount(Page page) throws AntiSpiderException {
        Job job = ((DelayRequest) page.getRequest()).getJob();
        //输出日志查看任务是否进入worker
        String oldUrl = job.getUrl();
        Html html = page.getHtml();
        Document document = html.getDocument();

        Elements news = document.getElementsByClass("news-list");

        if (null == news || news.isEmpty()) {
            throw new AntiSpiderException(document.title());
        }
        //查询的词
        String query = oldUrl.substring(oldUrl.indexOf("query") + 6, oldUrl.indexOf("&ie"));

        Elements lis = news.get(0).getElementsByTag("li");

        //童书处理逻辑
        if (oldUrl.contains("#children_book")) {

            for (Element article : lis) {
                WechatBookLogs wbs = new WechatBookLogs();
                Element titleBox = article.getElementsByClass("txt-box").get(0);
                Element titleHtml = titleBox.getElementsByTag("h3").get(0).getElementsByTag("a").get(0);
                wbs.setTitle(titleHtml.text());

                Element summaryeHtml = article.getElementsByClass("txt-info").get(0);
                wbs.setSummary(summaryeHtml.text());

                Element authorAndTimeHtml = titleBox.getElementsByClass("s-p").get(0);
                String articleTime = authorAndTimeHtml.attr("t");
                wbs.setArticleTime(new Date(Long.valueOf(articleTime) * 1000));

                Element accountHtml = authorAndTimeHtml.getElementsByClass("account").get(0);
                wbs.setAuthor(accountHtml.text());
                //唯一键 code 用 title+author 做md5
                String code = Md5Util.getMd5(wbs.getTitle() + wbs.getAuthor());
                wbs.setArticleCode(code);
                wbs.setBookCode(job.getCode());
                wbs.setPlatformId(job.getPlatformId());
                putModel(page, wbs);
            }
        } else {
            //剧的微信文章
            int i = 0;
            for (Element article : lis) {
                SougouWechatSearchText text = new SougouWechatSearchText();
                //整体
                text.setContent(article.toString());
                Element titleBox = article.getElementsByClass("txt-box").get(0);
                Element titleHtml = titleBox.getElementsByTag("h3").get(0).getElementsByTag("a").get(0);
                text.setTitle(titleHtml.text());
                String articleUrl = titleHtml.attr("href");
                int a = articleUrl.indexOf("url=");
                int b = (int) ((100 * Math.random()) + 1);
                String str = articleUrl.substring(a + 4 + 21 + b, a + 4 + 21 + b + 1);
                if (articleUrl.contains("http://mp.weixin.qq.com")) {
                    continue;
                }
                // 拼接code保存，用于创建微信文章爬取任务
                String old_url = "https://weixin.sogou.com" + articleUrl + "&k=" + b + "&h=" + str + "&code=" + job.getCode();
                text.setUrl(old_url);
                Element summaryeHtml = article.getElementsByClass("txt-info").get(0);
                text.setSummary(summaryeHtml.text());

                Element authorAndTimeHtml = titleBox.getElementsByClass("s-p").get(0);
                String articleTime = authorAndTimeHtml.attr("t");
                Date time = new Date(Long.valueOf(articleTime) * 1000);   // 文章发表时间
                Date yesDate = DateUtil.getYesterdayDate();
                if (time.compareTo(yesDate) < 0){
                    continue;
                }
                text.setArticleTime(time);

                Element accountHtml = authorAndTimeHtml.getElementsByClass("account").get(0);
                text.setAuthor(accountHtml.text());

                text.setCompositor(i);
                //排序+1
                i++;
                putModel(page, text);
            }
        }

        if (oldUrl.contains("&page=")) return;

        Integer pageNum = 5;
        if (oldUrl.contains("core")) {
            pageNum = 10;
        }
        // 生成10页临时任务
        for (int k = 0; k < pageNum; k++) {
            String url = String.format("https://weixin.sogou.com/weixin?type=2&query=%s&ie=utf8&s_from=input&_sug_=n&_sug_type_=1&page=%s", query, k + 1);
            if (oldUrl.contains("#children_book")) {
                url = url + "#children_book";
            }
            Job tmpNJob = new Job(url);
            DbEntityHelper.derive(job, tmpNJob);
            tmpNJob.setCode(job.getCode());
            putModel(page, tmpNJob);
        }
    }

    public void analyzeWechatArticle(Page page) {
        Job job = ((DelayRequest) page.getRequest()).getJob();
        log.warn("weixin.detail.com ,worker get job ,this url is ->{}", job.getUrl());

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
                .filter(p -> p.data().toString().contains("biz"))
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