package com.jinguduo.spider.spider.u17;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.util.DateUtil;
import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.data.table.Comic;
import com.jinguduo.spider.data.table.ComicAuthorRelation;
import com.jinguduo.spider.data.table.ComicCommentText;
import com.jinguduo.spider.data.table.ComicU17;
import com.jinguduo.spider.webmagic.Page;

import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @DATE 31/07/2017 11:03
 */
@Worker
@CommonsLog
public class U17ComicSpider extends CrawlSpider {

    // http://www.u17.com/more/salist_ts.html
    private Site site = SiteBuilder.builder().setDomain("www.u17.com").build();

    private PageRule rules = PageRule.build()
            .add("/comic/\\d+", page -> albumProcess(page))
            .add("/comment/ajax.php", page -> getCommentCount(page));

    private final String INIT_COMMENT_URL = "http://www.u17.com/comment/ajax.php?mod=thread&act=get_comment_php&thread_id=%s";


    private void albumProcess(Page page) {

        Job job = ((DelayRequest) page.getRequest()).getJob();

        Document document = page.getHtml().getDocument();

        Element info = document.getElementsByClass("info").get(0);

        String raw = info.html().replace(" ", "").replace("\n", "");

        ComicU17 comicU17 = new ComicU17();
        comicU17.setCode(job.getCode());
        Date day = DateUtil.getDayStartTime(new Date());
        comicU17.setDay(day);

        Matcher matcherTotalClick = Pattern.compile("总点击：(<i>|<span.*?>)(.*?)(</i>|</span>)").matcher(raw);
        if (matcherTotalClick.find()) {
            comicU17.setStrTotalClick(matcherTotalClick.group(2));
        }

        Matcher matcherMonthlyTicket = Pattern.compile("总月票：(<i>|<span.*?>)(.*?)(</i>|</span>)").matcher(raw);
        if (matcherMonthlyTicket.find()) {
            comicU17.setMonthlyTicket(Integer.valueOf(matcherMonthlyTicket.group(2)));
        }

        Matcher matcherTotalLike = Pattern.compile("加入收藏.*?<i>(.*?)</i>").matcher(raw);
        if (matcherTotalLike.find()) {
            comicU17.setTotalLike(Integer.valueOf(matcherTotalLike.group(1)));
        }

        Comic comic = new Comic();
        comic.setCode(job.getCode());
        comic.setPlatformId(28);
        String name = document.getElementsByClass("comic_info").get(0).getElementsByClass("fl").get(0).text();
        comic.setName(name);

        Element cover = null != document.getElementById("cover") ? document.getElementById("cover") : document.getElementById("cv");
        String headerImg = cover.getElementsByTag("a").get(0).getElementsByTag("img").get(0).attr("src");
        comic.setHeaderImg(headerImg);

        String author = document.getElementsByClass("author_info").get(0).getElementsByClass("name").get(0).text();
        String authorUrl = document.getElementsByClass("author_info").get(0).getElementsByClass("name").get(0).attr("href");
        int start = StringUtils.indexOf(authorUrl, "u17.com/") + 8;

        String authorIdInit = StringUtils.substring(authorUrl, start, authorUrl.length());
        String authorId = StringUtils.replace(authorIdInit, "/", "");

        ComicAuthorRelation car = new ComicAuthorRelation();
        car.setComicCode(job.getCode());
        car.setPlatformId(28);
        car.setAuthorName(author);
        car.setAuthorId(authorId);

        comic.setAuthor(author);

        Elements class_tag = document.getElementsByClass("class_tag");

        List tagList = new ArrayList();
        for (Element tagElemanet : class_tag) {
            tagList.add(tagElemanet.text());
        }

        String subject = StringUtils.join(tagList, "/");

        comic.setSubject(subject);

        Elements imgs = document.getElementsByClass("left_tag").get(0).getElementsByTag("img");
        for (Element img : imgs) {
            if (img.attr("title").contains("独家")) {
                comic.setExclusive(Boolean.TRUE);
            }
            if (img.attr("title").contains("签约")) {
                comic.setSigned(Boolean.TRUE);
            }
        }

        Matcher matcherFinish = Pattern.compile("状态：(<em>|<span.*?>)(.*?)(</em>|</span>)").matcher(raw);
        if (matcherFinish.find()) {
            comic.setFinished("连载中".equals(matcherFinish.group(2)) ? Boolean.FALSE : Boolean.TRUE);
        }

        Element words = info.getElementById("words");
        comic.setIntro(words.text());

        putModel(page, comicU17);
        putModel(page, comic);
        putModel(page, car);

        //评论任务生成
        try {
            Element aElement = document.getElementById("comment_total").nextElementSibling();
            String href = aElement.attr("href");
            String threadId = StringUtils.substringBefore(StringUtils.substringAfterLast(href, "/"), ".html");
            String commentUrl = String.format(INIT_COMMENT_URL, threadId);

            Job newJob = new Job(commentUrl);
            DbEntityHelper.derive(job, newJob);
            putModel(page, newJob);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            log.error("有妖气评论任务生成失败,url为：-->," + job.getUrl(), e);
        }

    }

    private void getCommentCount(Page page) {
        Job job = ((DelayRequest) page.getRequest()).getJob();
        String commentString = page.getHtml().toString();
        String jsonString = StringUtils.substring(commentString, StringUtils.indexOf(commentString, "{"), StringUtils.indexOf(commentString, "})") + 1);
        JSONObject jsonObject = JSON.parseObject(jsonString);
        Integer totalAll = jsonObject.getInteger("total_all");
        if (null == totalAll) {
            return;
        }
        ComicU17 u17 = new ComicU17();
        u17.setCode(job.getCode());
        Date day = DateUtil.getDayStartTime(new Date());
        u17.setDay(day);
        u17.setCommentCount(totalAll);
        putModel(page, u17);
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
