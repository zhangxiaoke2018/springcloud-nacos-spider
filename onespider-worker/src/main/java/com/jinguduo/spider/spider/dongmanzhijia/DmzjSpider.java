package com.jinguduo.spider.spider.dongmanzhijia;

import com.alibaba.fastjson.JSONObject;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.util.DateHelper;
import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.data.table.Comic;
import com.jinguduo.spider.data.table.ComicAuthorRelation;
import com.jinguduo.spider.data.table.ComicDmzj;
import com.jinguduo.spider.data.table.ComicSex;
import com.jinguduo.spider.webmagic.Page;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Date;

/**
 * I'm a cute code dog
 * User: LingChen
 * Date:2017/11/1
 * Time:15:37
 */
@Slf4j
@Worker
public class DmzjSpider extends CrawlSpider {

    private static final String PAGE_URL = "http://www.dmzj.com/rank/week/1-%s.html";
    private static final String COMIC_DATA_URL = "https://www.dmzj.com/static/hits/%s.json";
    private static final String COMMENT_URL = "https://interface.dmzj.com/api/NewComment2/total?type=4&obj_id=%s";
    private static final int MAX_PAGE = 10;

    private Site site = SiteBuilder.builder()
            .setDomain("www.dmzj.com")
            .build();

    private PageRule rules = PageRule.build()
            .add("/rank$", page -> createPageTask(page))
            .add("/rank/week", page -> createComicNameTask(page))
            .add("/info/", page -> analyzeDetailAndCreateJsonTask(page))
            .add(".json$", page -> analyzeComicDataJson(page));

    private void analyzeComicDataJson(Page page) {
        Job job = ((DelayRequest) page.getRequest()).getJob();

        //返回数据为json格式转换为map
        JSONObject json = JSONObject.parseObject(page.getJson().get());
        if (null == json) return;
        ComicDmzj dmzj = new ComicDmzj();
        dmzj.setCode(job.getCode());
        dmzj.setDay(DateHelper.getTodayZero(Date.class));
        dmzj.setHotTotal(Integer.valueOf(json.getString("hot_total")));
        dmzj.setApphotTotal(Integer.valueOf(json.getString("apphot_total")));
        dmzj.setHitTotal(Integer.valueOf(json.getString("hit_total")));
        dmzj.setApphitTotal(Integer.valueOf(json.getString("apphit_total")));
        dmzj.setTotalAddNum(Integer.valueOf(json.getString("total_add_num")));
        dmzj.setCopyright(Integer.valueOf(json.getString("copyright")));
        dmzj.setHotHits(Integer.valueOf(StringUtils.replace(json.getString("hot_hits"), "℃", "")));
        dmzj.setHits(Integer.valueOf(json.getString("hits")));
        dmzj.setVoteAmount(Integer.valueOf(json.getString("vote_amount")));
        dmzj.setSubAmount(Integer.valueOf(json.getString("sub_amount")));
        putModel(page, dmzj);

        //漫画评论url
        Integer comicId = Integer.parseInt(dmzj.getCode().replaceAll("dmzj-","").trim());
        Job newJob2 = new Job(String.format(COMMENT_URL, comicId));
        DbEntityHelper.derive(job, newJob2);
        putModel(page, newJob2);
    }

    private void analyzeDetailAndCreateJsonTask(Page page) {
        Job job = ((DelayRequest) page.getRequest()).getJob();

        Document document = page.getHtml().getDocument();
        Comic comic = new Comic();
        Element nameDiv = document.getElementsByClass("comic_deCon").get(0);

        String name = nameDiv.getElementsByTag("h1").text();
        comic.setName(name);

        String headerImg = document.getElementsByClass("comic_i_img").get(0).getElementsByTag("a").get(0).getElementsByTag("img").get(0).attr("src");
        comic.setHeaderImg(headerImg);

        Element ul = nameDiv.getElementsByClass("comic_deCon_liO").get(0);
        Elements lis = ul.getElementsByTag("li");
        for (Element li : lis) {
            String liText = li.text();
            if (StringUtils.contains(liText, "作者：")) {
                String auth = StringUtils.substring(liText, StringUtils.indexOf(liText, "：") + 1, liText.length()).trim();

                comic.setAuthor(auth);
            } else if (StringUtils.contains(liText, "状态：")) {
                if (StringUtils.contains(liText, "连载中")) {
                    comic.setFinished(false);
                } else {
                    comic.setFinished(true);
                }
            } else if (StringUtils.contains(liText, "类别：")) {
                String subject = StringUtils.substring(liText, StringUtils.indexOf(liText, "：") + 1, liText.length()).trim();
                comic.setSubject(subject);
                if (StringUtils.contains(subject, "女")) {
                    comic.setSex(ComicSex.girl.getSqlEnum());
                } else {
                    comic.setSex(ComicSex.boy.getSqlEnum());
                }
            } else if (StringUtils.contains(liText, "类型：")) {
                comic.setTags(StringUtils.replace(StringUtils.substring(liText, StringUtils.indexOf(liText, "：") + 1, liText.length()).trim(), " | ", "/"));
            } else {
                continue;
            }
        }
        Element comicIds = document.getElementById("subscribe_id_mh");
        String click = comicIds.attr("onclick");
        String comicId = StringUtils.substring(click, StringUtils.indexOf(click, "(") + 1, StringUtils.indexOf(click, ","));

        String detailText = nameDiv.getElementsByClass("comic_deCon_d").text();
        comic.setIntro(detailText);

        comic.setPlatformId(job.getPlatformId());
        String code = "dmzj-" + comicId;
        comic.setCode(code);
        putModel(page, comic);

        Element authorSpan = document.getElementsByClass("intro_athor_de").get(0);
        Element authorATag = authorSpan.getElementsByTag("h3").get(0).getElementsByTag("a").get(0);
        String authorName = authorATag.text();
        String authorUrl = authorATag.attr("href");
        String authorId = StringUtils.substring(authorUrl, StringUtils.indexOf(authorUrl, "hisUid=") + 7, authorUrl.length());

        ComicAuthorRelation car = new ComicAuthorRelation();
        car.setPlatformId(job.getPlatformId());
        car.setComicCode(code);
        car.setAuthorName(authorName);
        car.setAuthorId(authorId);

        putModel(page,car);



        //漫画数据jsonurl
        Job newJob = new Job(String.format(COMIC_DATA_URL, comicId));
        DbEntityHelper.derive(job, newJob);
        newJob.setCode(code);
        putModel(page, newJob);

//        //漫画评论url
//        Job newJob2 = new Job(String.format(COMMENT_URL, comicId));
//        DbEntityHelper.derive(newJob, newJob2);
//        putModel(page, newJob2);

    }

    private void createComicNameTask(Page page) {
        Job job = ((DelayRequest) page.getRequest()).getJob();

        Document document = page.getHtml().getDocument();

        Elements divs = document.getElementsByClass("ph_r_con_li");
        Element div = (null == divs || divs.size() == 0) ? null : divs.get(0);
        if (null == div) return;
        Elements decImgs = div.getElementsByClass("dec_img");
        for (Element img : decImgs) {
            String href = img.attr("href");
            Job job2 = new Job(href);
            DbEntityHelper.derive(job, job2);
            putModel(page, job2);
        }
    }

    private void createPageTask(Page page) {
        Job job = ((DelayRequest) page.getRequest()).getJob();
        for (int i = 1; i <= 1; i++) {
            String newUrl = String.format(PAGE_URL, i);
            Job job2 = new Job(newUrl);
            DbEntityHelper.derive(job, job2);
            job2.setPlatformId(34);
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
