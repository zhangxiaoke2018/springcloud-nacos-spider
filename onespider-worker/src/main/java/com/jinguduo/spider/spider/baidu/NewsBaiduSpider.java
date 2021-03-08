package com.jinguduo.spider.spider.baidu;

import static com.jinguduo.spider.common.util.DateHelper.lDToUdate;


import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.assertj.core.util.Lists;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.extern.slf4j.Slf4j;

import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.common.util.RegexUtil;
import com.jinguduo.spider.data.table.BaiduNewsLog;
import com.jinguduo.spider.data.table.NewsArticleLog;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.selector.Html;
import com.jinguduo.spider.webmagic.selector.PlainText;

@Worker
@Slf4j
public class NewsBaiduSpider extends CrawlSpider {

    private Site site = SiteBuilder.builder()
            .setDomain("news.baidu.com")
            .setUserAgent("Mozilla/5.0 (iPhone; CPU iPhone OS 9_1 like Mac OS X) AppleWebKit/601.1.46 (KHTML, like Gecko) Version/9.0 Mobile/13B143 Safari/601.1")
            .build();

    private PageRule rules = PageRule.build()
            .add(".", page -> analyze(page))
            .add(".", page -> getNewsList(page));//新闻标题提及量


    private void analyze(Page page) {
        Job job = ((DelayRequest) page.getRequest()).getJob();
        String url = page.getUrl().get();
        //如果不包含pn或者pn=0，则为第一页, 只记录一次数
        if (!StringUtils.contains(url, "pn=") || StringUtils.contains(url, "pn=0")) {
            //提及量
            Integer involveCount = Integer.valueOf(page.getHtml().xpath("//*[@id=\"header_top_bar\"]/span/text()").replace(",", "").regex("([0-9]+)", 1).get());
            //save media data
            BaiduNewsLog baiduNewsLog = new BaiduNewsLog();
            baiduNewsLog.setCount(involveCount);
            baiduNewsLog.setCode(job.getCode());
            putModel(page,baiduNewsLog);
        }
    }

    /**
     * 生成新闻列表和后续页面抓取连接
     *
     * @param page
     */
    private void getNewsList(Page page) {
        Job oldJob = ((DelayRequest) page.getRequest()).getJob();
        Html html = page.getHtml();
        String url = page.getUrl().get();
        List<NewsArticleLog> newsArticles = getNewsArticles(page);
        putModel(page, newsArticles);

        //如果不包含pn或者pn=0，则为第一页，如果还有后续页码则生成后两页job
        if (!StringUtils.contains(url, "pn=") || StringUtils.contains(url, "pn=0")) {
            List<String> urlss = html.xpath("//[@id='page']/a/@href").all();
            List<String> urls = Lists.newArrayList();
            for (String u : urlss) {
                if(u.startsWith("/ns")){
                    urls.add("http://news.baidu.com" + u);
                }else {
                    urls.add(u);
                }
            }
            List<Job> jobs = Lists.newArrayList();
            //最后一个是下一页按钮，所以不需要生成
            for (int i = 0; i < urls.size() - 1; i++) {
                //再取三页
                if(i>2){
                    break;
                }
                Job job = new Job(urls.get(i));
                job = DbEntityHelper.derive(oldJob, job);
                jobs.add(job);
            }
            if(jobs.size()>0){
                putModel(page, jobs);
            }
        }
    }

    private List<NewsArticleLog> getNewsArticles(Page page) {

        List<NewsArticleLog> logs = Lists.newArrayList();
        Html html = page.getHtml();
        Job oldJob = ((DelayRequest) page.getRequest()).getJob();
        List<String> list = html.xpath("//div[contains(@class, 'result title')]").all();
        list.forEach(str -> {

            try {
                Html html1 = new Html(str);
                String newsurl = html1.xpath("//h3/a/@href").get();

                Document document = Jsoup.parse(str);
                String title = document.select("h3.c-title").get(0).text();
                String authorAndTime = document.select("div.c-title-author").get(0).text();

                //String name = RegexUtil.getDataByRegex(authorAndTime, "(.+?)\\d", 1).replace("\t", "");//不适配:河南100度  2016年11月28日 14:59
                //String time = RegexUtil.getDataByRegex(authorAndTime, "\\d.*\\d\\d:\\d\\d", 0);

                String name = RegexUtil.getDataByRegex(authorAndTime, "(.*)[  |\\s*](\\d*[年|小|分|秒])", 1);

                String time = "";
                if (StringUtils.isNotBlank(name)) {
                    time = authorAndTime.replaceAll(name, "");
                    name = name.replaceAll(" ","");
                } else {
                    // 小网站，可能没有标明文章来源
                    log.debug("getDataByRegex name fail by authorAndTime " + authorAndTime + " url "+page.getUrl());
                    return;
                }
                PlainText timeText = new PlainText(time);
                Date date;
                if (StringUtils.contains(time, "小时")) {
                    int hour = Integer.valueOf(timeText.regex("(\\d+)小").get());
                    LocalDateTime dateTime = LocalDateTime.now();
                    LocalDateTime res = dateTime.minusHours(hour);
                    date = lDToUdate(res);
                }else if(StringUtils.contains(time, "分钟前")){
                    int min = Integer.valueOf(timeText.regex("(\\d+)分").get());
                    LocalDateTime dateTime = LocalDateTime.now();
                    LocalDateTime res = dateTime.minusMinutes(min);
                    date = lDToUdate(res);
                } else {
                    time = new PlainText(time).regex("(\\d+年\\d+月\\d+日\\s*\\d+:\\d+)",1).get();
                    date = DateUtils.parseDate(time, "yyyy年MM月dd日 HH:mm");
                }
                NewsArticleLog newsArticleLog = new NewsArticleLog();
                newsArticleLog.setShowId(oldJob.getShowId());
                newsArticleLog.setCode(oldJob.getCode());
                newsArticleLog.setAuthor(name);
                newsArticleLog.setDate(date);
                newsArticleLog.setTitle(title);
                newsArticleLog.setUrl(newsurl);
                logs.add(newsArticleLog);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        });
        return logs;
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
