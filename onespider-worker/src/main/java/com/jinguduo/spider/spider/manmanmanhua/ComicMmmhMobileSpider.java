package com.jinguduo.spider.spider.manmanmanhua;

import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.data.table.Comic;
import com.jinguduo.spider.data.table.ComicAuthorRelation;
import com.jinguduo.spider.webmagic.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Created by lc on 2018/11/7
 */
@Worker
@Slf4j
public class ComicMmmhMobileSpider extends CrawlSpider {

    private Site site = SiteBuilder.builder().setDomain("m.manmanapp.com").build();

    private PageRule rules = PageRule.build()
            .add("/comic-", page -> getComicDetail(page));

    //https://m.manmanapp.com/comic-1404220.html
    private void getComicDetail(Page page) {
        Job job = ((DelayRequest) page.getRequest()).getJob();
        Document document = page.getHtml().getDocument();
        log.info("m.manmanapp.com -> start get detail");

        Element cover = document.getElementsByClass("cartoon_introduce").get(0);
        Elements pic = cover.getElementsByClass("pic");
        String imageUrl = pic.get(0).getElementsByTag("img").get(0).attr("src");
        String title = document.getElementsByClass("flex1 title").text();
       // String author = cover.getElementsByClass("author").text().replace("作者：", "");
        Element authorATag = cover.getElementsByClass("author").get(0).getElementsByTag("a").get(0);
        String author = authorATag.text();
        String authorUrl = authorATag.attr("href");
        String authorId = StringUtils.substring(authorUrl, StringUtils.indexOf(authorUrl, "author-") + 7, StringUtils.indexOf(authorUrl, ".html"));


        String initType = cover.getElementsByClass("type").text();
        String type = org.apache.commons.lang3.StringUtils.replace(initType, "类型：", "");
        String finalType = org.apache.commons.lang3.StringUtils.replace(type, ",", "/").trim();
        Elements strongs = cover.getElementsByTag("strong");
        Boolean exclusive = Boolean.FALSE;
        for (Element strong : strongs) {
            if (org.apache.commons.lang3.StringUtils.equals("独家", strong.text())) {
                exclusive = Boolean.TRUE;
                continue;
            }
        }

        String ellipsis = cover.getElementsByClass("introduce").text();
        String intro = org.apache.commons.lang3.StringUtils.replace(ellipsis, "作品简介：", "").trim();

        Comic comic = new Comic();
        comic.setCode(job.getCode());
        comic.setPlatformId(35);
        comic.setName(title);
        comic.setHeaderImg(imageUrl);
        comic.setTags(finalType);
        comic.setAuthor(author);
        comic.setIntro(intro);
        comic.setExclusive(exclusive);
        putModel(page, comic);

        ComicAuthorRelation car = new ComicAuthorRelation();
        car.setPlatformId(35);
        car.setAuthorId(authorId);
        car.setComicCode(job.getCode());
        car.setAuthorName(author);
        putModel(page, car);

        log.info("m.manmanapp.com -> this comic is ->{}", comic.toString());
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
