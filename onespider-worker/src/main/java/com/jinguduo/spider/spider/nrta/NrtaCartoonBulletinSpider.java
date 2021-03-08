package com.jinguduo.spider.spider.nrta;

import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.data.table.CartoonBulletin;
import com.jinguduo.spider.webmagic.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.ParseException;
import java.util.Date;

/**
 * Created by lc on 2020/4/9
 */
@Worker
@Slf4j
public class NrtaCartoonBulletinSpider extends CrawlSpider {


    private Site site = SiteBuilder.builder().setDomain("zw.nrta.gov.cn")
            .build();

    private PageRule rules = PageRule.build()
            .add(".", page -> processBulletin(page));

    private void processBulletin(Page page) {
        Document document = page.getHtml().getDocument();
        Element element = document.getElementById("489");
        String script = element.getElementsByTag("script").first().toString();
        script = StringUtils.substring(script, StringUtils.indexOf(script, "<recordset>") + 11, StringUtils.indexOf(script, "</recordset>"));
        script = StringUtils.replaceAll(script, "<!\\[CDATA\\[", "");
        script = StringUtils.replaceAll(script, "\\]", "");
        document = Jsoup.parse(script);
        Elements lis = document.getElementsByTag("li");

        String pre = "https://zw.nrta.gov.cn";
        for (Element li : lis) {
            Element a = li.getElementsByTag("a").first();
            String href = pre + a.attr("href");
            String title = a.text();
            String dateStr = li.getElementsByTag("span").first().text();
            Date date = new Date();

            try {
                date = DateUtils.parseDate(dateStr, "yyyy-MM-dd");
            } catch (ParseException e) {
                e.printStackTrace();
            }
            CartoonBulletin cb = new CartoonBulletin();
            cb.setUrl(href);
            cb.setTitle(title);
            cb.setDay(date);
            cb.setStatus(1);
            putModel(page, cb);
        }

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
