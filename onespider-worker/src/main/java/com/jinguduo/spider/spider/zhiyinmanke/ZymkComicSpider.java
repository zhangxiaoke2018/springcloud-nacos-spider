package com.jinguduo.spider.spider.zhiyinmanke;

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
import com.jinguduo.spider.data.table.ComicSex;
import com.jinguduo.spider.data.table.ComicZymk;
import com.jinguduo.spider.webmagic.Page;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * I'm a cute code dog
 * User: LingChen
 * Date:2017/10/20
 * Time:10:25
 */
@Slf4j
@Worker
public class ZymkComicSpider extends CrawlSpider {

    private static final String DETAIL_INIT_URL = "http://www.zymk.cn%s";
    private static final String COMMENT_URL = "http://changyan.sohu.com/api/2/topic/count?topic_source_id=comic%s&client_id=cysLJ05yl";

    /**
     * 评论的爬虫放在
     * com.jinguduo.spider.spider.sohu.SohuCommentSpider
     */

    private Site site = SiteBuilder.builder()
            .setDomain("www.zymk.cn")
            .build();

    private PageRule rules = PageRule.build()
            .add("/top/comic_click.html$", page -> analyze(page))
            .add("/\\d+/$", page -> analyzeDetail(page));


    //分发任务
    private void analyze(Page page) {
        Job job = ((DelayRequest) page.getRequest()).getJob();

        Document document = page.getHtml().getDocument();
        Elements mainDiv = document.getElementsByClass("tabs-item clearfix");
        if (null == mainDiv || mainDiv.size() == 0) return;
        Elements items = mainDiv.get(0).getElementsByClass("item");
        for (Element item : items) {
            String href = item.getElementsByTag("a").attr("href");
            String newUrl = String.format(DETAIL_INIT_URL, href);
            Job job2 = new Job(newUrl);
            DbEntityHelper.derive(job, job2);
            putModel(page, job2);
        }
    }

    //解析详情页
    private void analyzeDetail(Page page) {
        Job job = ((DelayRequest) page.getRequest()).getJob();

        Document document = page.getHtml().getDocument();
        Comic comic = new Comic();
        comic.setPlatformId(33);
        List<Element> metas = document.getElementsByTag("meta").stream().filter(p -> p.hasAttr("property")).collect(Collectors.toList());

        for (Element meta : metas) {
            String content = meta.attr("content");
            if (StringUtils.equals(meta.attr("property"), "og:novel:book_name")) {
                comic.setName(content);
            } else if (StringUtils.equals(meta.attr("property"), "og:novel:category")) {
                comic.setTags(content.trim().replace(" ", "/"));
            } else if (StringUtils.equals(meta.attr("property"), "og:novel:author")) {
                comic.setAuthor(content);
            } else if (StringUtils.equals(meta.attr("property"), "og:novel:status")) {
                comic.setFinished(StringUtils.equals(content, "连载") ? Boolean.FALSE : Boolean.TRUE);
            }
        }

        String headerImg = document.getElementsByClass("comic-cover").get(0).getElementsByTag("img").get(0).attr("data-src");
        comic.setHeaderImg(headerImg);

        Elements title = document.getElementsByTag("h1");
        String idStr = title.attr("data-comic_id");
        if (StringUtils.isBlank(idStr)) return;
        Integer id = Integer.valueOf(idStr.trim());

        String code = "zymk-" + id;
        comic.setCode(code);

        Elements crumbs = document.getElementsByClass("crumb");
        Byte sex = ComicSex.all.getSqlEnum();
        if (crumbs.size() == 1) {
            String text = crumbs.get(0).text();
            if (StringUtils.contains(text, "少男")) {
                sex = ComicSex.boy.getSqlEnum();
            } else if (StringUtils.contains(text, "少女")) {
                sex = ComicSex.girl.getSqlEnum();
            } else {
                sex = ComicSex.unKnowSex.getSqlEnum();
            }
        }
        comic.setSex(sex);

        String desc = document.getElementsByClass("desc-con").text();
        comic.setIntro(desc);


        ComicZymk zymk = new ComicZymk();
        String read = document.getElementById("read").getElementsByClass("type-show").text();
        String collect = document.getElementById("collect").getElementsByTag("strong").text();
        String reward = document.getElementById("reward").getElementsByTag("strong").text();
        String monthticket = document.getElementById("monthticket").getElementsByTag("strong").text();
        String recommend = document.getElementById("recommend").getElementsByTag("strong").text();
        zymk.setCode(code);
        zymk.setDay(DateHelper.getTodayZero(Date.class));
        zymk.setReadCount(str2Long(read.trim()));
        zymk.setCollectCount(str2Long(collect.trim()).intValue());
        zymk.setRewardCount(str2Long(reward.trim()).intValue());
        zymk.setMonthlyTicket(str2Long(monthticket.trim()).intValue());
        zymk.setRecommendCount(str2Long(recommend.trim()).intValue());

        putModel(page, zymk);
        putModel(page, comic);

        Job nexJob = new Job(String.format(COMMENT_URL, id));
        DbEntityHelper.derive(job, nexJob);
        nexJob.setCode(code);
        putModel(page, nexJob);

    }

    /**
     * 反格式化数字
     */
    public static Long str2Long(String num) {
        try {
            int index = num.indexOf("亿");
            if (index != -1) {
                String numFormat = num.substring(0, index);
                Double aDouble = Double.valueOf(numFormat);
                long number = (long) (aDouble * (double) 100000000);
                return number;
            }
            index = num.indexOf("万");
            if (index != -1) {
                String numFormat = num.substring(0, index);
                long number = (long) (Double.valueOf(numFormat) * (double) 10000);
                return number;
            }
            if (StringUtils.isBlank(num)) {
                return 0L;
            }
            return Long.valueOf(num);
        } catch (Exception e) {
            log.error("ac.qq.com error  数字格式化错误-->{}", num);
            return 0L;
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
