package com.jinguduo.spider.spider.toutiao;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.code.FetchCodeEnum;
import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.data.table.Show;
import com.jinguduo.spider.data.table.ShowLog;
import com.jinguduo.spider.data.table.ToutiaoNewLogs;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.selector.Html;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Date;


/**
 * Created by lc on 2017/5/15.
 */
@Worker
@Slf4j
@SuppressWarnings("all")
public class ToutiaoNewsSpider extends CrawlSpider {

    /**
     * https://www.toutiao.com/api/search/content/?aid=24&offset=0&format=json&keyword=人民的名义&autoload=true&count=20&cur_tab=1
     */


    /**
     * 头条老郭一个奇怪的视频
     * https://www.toutiao.com/pgc/column/article_list/?column_no=6507463263532354573&media_id=1583395523989517
     * <p>
     * 以上链接的数据来源于以下链接
     * https://www.toutiao.com/pgc/column/article_list/?column_no=6507463263532354573&media_id=1583395523989517&format=json
     */

    private Site site = SiteBuilder.builder()
            .setDomain("www.toutiao.com")
            .setCharset("UTF-8")
            // .addSpiderListener(new UserAgentSpiderListener())
            .addSpiderListener(new ToutiaoDownloaderListener())
            .build();

    private PageRule rules = PageRule.build()
            .add("/search/content", page -> analyze(page))
            .add("/pgc/column/article_list/", page -> analyzeVideoList(page));


    private void createVideoTask(Page page) {
        Job job = ((DelayRequest) page.getRequest()).getJob();
        String url = job.getUrl();
        String newUrl = url + "&count=100&format=json";
        Job newJob = new Job(newUrl);
        DbEntityHelper.derive(job, newJob);
        putModel(page, newJob);
    }


    private void analyzeVideoList(Page page) {
        Job job = ((DelayRequest) page.getRequest()).getJob();
        if (!job.getUrl().contains("format=json")) {
            this.createVideoTask(page);
            return;
        }
        JSONObject jsonObject = JSON.parseObject(page.getRawText());
        String html = jsonObject.getString("html");
        Document document = Html.create(html).getDocument();
        Elements sections = document.getElementsByTag("section");
        Long allPlayCount = 0L;
        //每一个section相当于一集
        for (Element section : sections) {
            Element aTag = section.getElementsByTag("a").get(0);
            String href = aTag.attr("href");

            Element wrap = section.getElementsByClass("text-wrap").get(0);
            Element h3 = wrap.getElementsByTag("h3").get(0);
            String name = h3.text();

            Element info = wrap.getElementsByClass("info").get(0);
            Elements spanList = info.getElementsByTag("span");


            Long count = null;
            Long comment = null;
            Date time = null;
            String episode = null;

            try {
                for (Element span : spanList) {
                    String spanClass = span.attr("class");
                    switch (spanClass) {
                        case "label-count":
                            String countStr = StringUtils.replace(span.text().trim(), "次播放", "");
                            count = StringUtils.contains(countStr, "万") ? Long.valueOf(StringUtils.replace(countStr, "万", "")) * 10000L : Long.valueOf(countStr);
                            allPlayCount += count;
                            break;
                        case "label-comment":
                            String commentStr = StringUtils.replace(span.text().trim(), "评论", "");
                            comment = Long.valueOf(commentStr);
                            break;
                        case "time":
                            String timeStr = span.attr("title");
                            time = DateUtils.parseDate(timeStr, "yyyy-MM-dd HH:mm");
                            episode = DateFormatUtils.format(time, "yyyyMMdd");
                            break;
                    }
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
            String code = FetchCodeEnum.getCode(href);

            Show show = new Show(name, code, 44, job.getShowId());
            show.setDepth(2);
            show.setEpisode(Integer.valueOf(episode));
            show.setParentCode(job.getCode());
            show.setUrl(href);
            putModel(page, show);

            ShowLog showLog = new ShowLog();
            showLog.setPlayCount(count);
            showLog.setCode(code);
            showLog.setPlatformId(44);
            putModel(page, showLog);
        }
        ShowLog allLogs = new ShowLog();
        allLogs.setPlayCount(allPlayCount);
        allLogs.setCode(job.getCode());
        allLogs.setPlatformId(44);
        putModel(page, allLogs);
    }

    private void analyze(Page page) {
        Job job = ((DelayRequest) page.getRequest()).getJob();
        //返回数据为json格式转换为map
        JSONObject json = JSONObject.parseObject(page.getRawText());
        JSONArray data = json.getJSONArray("data");
        //数据已抓取完毕
        if (null == data || data.size() == 0) {
//            log.error("www.toutiao.com error,request exception, url-->{},result-->{}", job.getUrl(), page);
            return;
        }
        for (int i = 0; i < data.size(); i++) {
            //获取到的新闻对象
            JSONObject o = data.getJSONObject(i);

            //头条唯一标识
            String toutiaoId = String.valueOf(o.get("id"));
            //标题
            String title = o.getString("title");
            if (StringUtils.isEmpty(title)) {
                continue;
            }
            //作者
            String source = o.getString("source");
            //来源
            String sourceUrl = o.getString("share_url");
            //评论数
            Integer commentsCount = o.getInteger("comments_count");
            if (StringUtils.isBlank(title) || StringUtils.isBlank(source) || StringUtils.isBlank(sourceUrl)) {
                continue;
            }
            Long newsLong = o.getLong("create_time");
            Date newsDate = new Date(newsLong * 1000);

            ToutiaoNewLogs logs = new ToutiaoNewLogs();
            logs.setToutiaoId(toutiaoId);
            logs.setCode(job.getCode());
            logs.setTitle(title);
            logs.setAuthor(source);
            logs.setSourceUrl(sourceUrl);
            logs.setCommentsCount(commentsCount);
            logs.setNewsDate(newsDate);
            putModel(page, logs);
        }
        if (data.size() > 15) {
            //获取到url.模拟翻页
            String oldUrl = job.getUrl();

            StringBuffer buffer = new StringBuffer(oldUrl);
            int i = buffer.indexOf("offset=");
            int j = buffer.indexOf("&format=");
            String num = buffer.substring(i + 7, j);
            Integer oldNum = Integer.valueOf(num);
            //防止翻页过多
            if (oldNum >= 100) {
                return;
            }
            StringBuffer newUrlBuf = buffer.replace(i + 7, j, String.valueOf(oldNum + 20));
            String newUrl = newUrlBuf.toString();
            Job job2 = new Job(newUrl);
            DbEntityHelper.derive(job, job2);
            putModel(page, job2);
        }
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