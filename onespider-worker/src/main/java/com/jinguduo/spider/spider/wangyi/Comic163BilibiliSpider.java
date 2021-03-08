package com.jinguduo.spider.spider.wangyi;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.common.util.TextUtils;
import com.jinguduo.spider.data.table.Comic;
import com.jinguduo.spider.data.table.Comic163;
import com.jinguduo.spider.data.table.ComicAuthorRelation;
import com.jinguduo.spider.webmagic.Page;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.Lists;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.List;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @deprecated  该爬虫已经过期  变更至 ComicBilibiliSpider
 *
 * @DATE  01/06/2020 11:40
 */
//@Worker
@Deprecated
public class Comic163BilibiliSpider extends CrawlSpider {


    // https://163.bilibili.com
    private Site site = SiteBuilder.builder().setDomain("163.bilibili.com").build();

    private PageRule rules = PageRule.build()
            .add("163.bilibili.com$", page -> processHome(page))
            .add("category", page -> processPager(page)) // https://163.bilibili.com/category/getData.json?sort=2&pageSize=72&page=1
            .add("author", page -> processHAuthor(page))
            .add("source", page -> processSource(page));

    private void processHAuthor(Page page) {
        Job job = ((DelayRequest) page.getRequest()).getJob();
        Document document = page.getHtml().getDocument();
        Element element = document.getElementsByClass("author-info-meta-warp").get(0);
        String authorName = element.getElementsByTag("h2").text();
        String url = job.getUrl();
        String authorId = StringUtils.substring(url, StringUtils.indexOf(url, "book/") + 5, url.length());
        String comicCode = job.getCode();

        ComicAuthorRelation car = new ComicAuthorRelation();
        car.setComicCode(comicCode);
        car.setPlatformId(29);
        car.setAuthorName(authorName);
        car.setAuthorId(authorId);
        putModel(page, car);
    }


    private final String PAGE_URL = "https://163.bilibili.com/category/getData.json?sort=2&dq=0&pageSize=72&page=%s";

    private final String B_URL = "https://163.bilibili.com/source/%s";

    private final String AUTHOR_URL = "https://163.bilibili.com/author/book/%s";

    private void processHome(Page page) {

        Job job = ((DelayRequest) page.getRequest()).getJob();

        // 生成全部分类的第一页任务
        Job cJob = new Job(String.format(PAGE_URL, 1));
        DbEntityHelper.derive(job, cJob);

        putModel(page, cJob);
    }

    private void processPager(Page page) {

        Job job = ((DelayRequest) page.getRequest()).getJob();


        // 解析列表内容
        JSONObject json = page.getJson().toObject(JSONObject.class);

        Integer nowPage = json.getJSONObject("params").getInteger("page");

        JSONArray books = json.getJSONArray("books");

        if (books != null && books.size() > 0) { // 生成下一页的任务
            Job cJob = new Job(String.format(PAGE_URL, nowPage + 1));
            DbEntityHelper.derive(job, cJob);
            putModel(page, cJob);
        }

        for (int i = 0; i < books.size(); i++) {
            JSONObject book = books.getJSONObject(i);

            Comic163 comic163 = new Comic163();

            comic163.setCode("163-" + book.getString("bookId").trim());
            comic163.setTotalPlayCount(book.getLong("clickCount"));
            comic163.setTucaoCount(book.getInteger("tucaoCount"));
            comic163.setCommentCount(book.getInteger("commentCount"));

            Comic comic = new Comic();
            String code = "163-" + book.getString("bookId").trim();
            String authorName = book.getString("author");
            String authorId = book.getString("authorUserId");
            comic.setName(TextUtils.removeEmoji(book.getString("title")));
            comic.setCode(code);
            comic.setPlatformId(29);
            comic.setAuthor(authorName);
            comic.setIntro(TextUtils.removeEmoji(book.getString("description")));
            comic.setFinished(book.getBoolean("finished"));
            comic.setHeaderImg(book.getString("cover"));


            //作者名称显示不准，生成单独任务进行抓取
            /*
            ComicAuthorRelation car = new ComicAuthorRelation();
            car.setComicCode(code);
            car.setPlatformId(29);
            car.setAuthorName(authorName);
            car.setAuthorId(authorId);
             putModel(page, car);
            */


            putModel(page, comic);
            putModel(page, comic163);


            // 生成专辑页任务去抓取题材 签约
            Job bJob = new Job(String.format(B_URL, book.getString("bookId")));
            DbEntityHelper.derive(job, bJob);
            bJob.setCode(code);
            putModel(page, bJob);


            Job authorJob = new Job(String.format(AUTHOR_URL, authorId));
            DbEntityHelper.derive(job, authorJob);
            authorJob.setCode(code);
            putModel(page, authorJob);

        }

    }

    private void processSource(Page page) {

        Job job = ((DelayRequest) page.getRequest()).getJob();

        Document document = page.getHtml().getDocument();

        Element detail = document.getElementsByClass("sr-detail").get(0);

        Elements signed = detail.getElementsByClass("u-label-signed");

        Comic comic = new Comic();
        comic.setCode(job.getCode());

        if (signed != null && signed.size() > 0) {
            comic.setSigned(Boolean.TRUE);
        } else {
            comic.setSigned(Boolean.FALSE);
        }

        Elements as = detail.getElementsByTag("a");

        List<String> types = Lists.newArrayList();
        for (Element a : as) {
            String href = a.attr("href");
            if (href.contains("?tc")) {
                types.add(a.text());
            }
        }
        comic.setSubject(StringUtils.join(types, "/"));
        putModel(page, comic);


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
