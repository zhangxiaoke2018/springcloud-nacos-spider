package com.jinguduo.spider.spider.jingdong;

import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.util.DateUtil;
import com.jinguduo.spider.data.table.JdGoods;
import com.jinguduo.spider.webmagic.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Date;

/**
 * Created by lc on 2019/10/31
 */
@Worker
@Slf4j
@SuppressWarnings("all")
public class JdFilterSpider extends CrawlSpider {

    private String filterPageUrl = "https://book.jd.com/booktop/1-0-0.html?category=3263-1-0-0-9-10009-%s";

    private String commentUrl = "https://club.jd.com/comment/skuProductPageComments.action?callback=&productId=%s&score=0&sortType=5&page=0";

    private Integer JD_PLATFORM_ID = 58;

    private Site site = SiteBuilder.builder().setDomain("book.jd.com")
            .addHeader("referer", "https://book.jd.com/")
            .build();
    private PageRule rules = PageRule.build()
            .add(".", page -> getGoodsId(page));

    private void getGoodsId(Page page) {
        String oldUrl = page.getUrl().get();
        String pageNum = StringUtils.substringAfterLast(oldUrl, "-");

        if (StringUtils.equals("1", pageNum)) {
            createPageJob(page);
        }

        Document document = page.getHtml().getDocument();
        Elements elements = document.getElementsByClass("p-detail");
        for (Element element : elements) {
            element = element.getElementsByTag("a").first();
            String href = element.attr("href");
            String title = element.attr("title");
            String code = StringUtils.substringBeforeLast(StringUtils.substringAfterLast(href, "/"), ".");

            //保存商品
            JdGoods jd = new JdGoods();
            jd.setGoodsId(code);
            jd.setTitle(title);
            jd.setDay(DateUtil.getDayStartTime(new Date()));
            putModel(page, jd);
            //创建任务
            this.createCommentJob(page, code);

        }


    }

    //翻页任务
    private void createPageJob(Page page) {
        for (int i = 2; i <= 5; i++) {
            String nextPageUrl = String.format(filterPageUrl, i);
            Job job = new Job(nextPageUrl);
            job.setPlatformId(JD_PLATFORM_ID);
            job.setCode(DigestUtils.md5Hex(job.getUrl()));
            putModel(page, job);

        }

    }

    //创建评论任务
    private void createCommentJob(Page page, String goodsId) {
        String commentJobUrl = String.format(commentUrl, goodsId);
        Job job = new Job(commentJobUrl);
        job.setPlatformId(JD_PLATFORM_ID);
        job.setCode(goodsId);
        putModel(page, job);
    }

    @Override
    public Site getSite() {
        return site;
    }

    @Override
    public PageRule getPageRule() {
        return rules;
    }
}
