package com.jinguduo.spider.spider.douban;

import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.spider.listener.UserAgentSpiderListener;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.type.SequenceCounterQuota;
import com.jinguduo.spider.data.table.bookProject.DoubanBook;
import com.jinguduo.spider.webmagic.Page;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by lc on 2020/1/10
 */
@Worker
public class DoubanBookSpider extends CrawlSpider {


    private Site site = SiteBuilder.builder()
            .setDomain("book.douban.com")
            .addDownloaderListener(new DoubanRandomCookieDownloaderListener()
                    .addAbnormalStatusCode(403, 404, 500, 501, 503)
                    .setQuota(new SequenceCounterQuota(5))
                    .setProbability(0.2))
            // user-agent 动态化
            .addSpiderListener(new UserAgentSpiderListener()).build();

    private PageRule rules = PageRule.build()
            .add("/subject/\\d+/", page -> detailProcess(page));

    private void detailProcess(Page page) {
        Job oldJob = ((DelayRequest) page.getRequest()).getJob();
        String oldUrl = oldJob.getUrl();

        String code = oldJob.getCode();
        Document document = page.getHtml().getDocument();

        Element info = document.getElementById("info");
        if (null == info) return;
        //获取标题
        Elements title = document.getElementsByTag("title");

        String bookTitle = title.text().replace("(豆瓣)", "").trim();


        String infoText = info.text();

        String ISBN = null;
        if (infoText.contains("ISBN: ")) {
            ISBN = StringUtils.substringAfterLast(infoText, "ISBN: ");
        }

        Element scoreDiv = document.getElementById("interest_sectl");
        String scoreStr = scoreDiv.getElementsByAttributeValue("property", "v:average").text();
        String scorePersonStr = scoreDiv.getElementsByAttributeValue("property", "v:votes").text();

        DoubanBook dbl = new DoubanBook();
        dbl.setCode(code);
        dbl.setPlatformId(oldJob.getPlatformId());
        dbl.setIsbn(ISBN);
        dbl.setUrl(oldUrl);
        dbl.setBookName(bookTitle);
        //如果无评分则直接跳出
        if (StringUtils.isEmpty(scoreStr) || StringUtils.isEmpty(scorePersonStr)) {
            dbl.setScore(0F);
            putModel(page, dbl);
            return;
        }


        dbl.setScore(Float.valueOf(scoreStr));
        dbl.setScorePerson(Integer.valueOf(scorePersonStr));
        //5-1分评论人数百分比
        Elements ratings = scoreDiv.getElementsByClass("rating_per");
        if (null != ratings && ratings.size() == 5) {
            String star5 = ratings.get(0).text().replace("%", "");
            String star4 = ratings.get(1).text().replace("%", "");
            String star3 = ratings.get(2).text().replace("%", "");
            String star2 = ratings.get(3).text().replace("%", "");
            String star1 = ratings.get(4).text().replace("%", "");

            dbl.setScore5Proportion(Float.valueOf(star5));
            dbl.setScore4Proportion(Float.valueOf(star4));
            dbl.setScore3Proportion(Float.valueOf(star3));
            dbl.setScore2Proportion(Float.valueOf(star2));
            dbl.setScore1Proportion(Float.valueOf(star1));
        }

        Element shortCommentDiv = document.getElementsByClass("mod-hd").first();
        String plStr = shortCommentDiv.getElementsByClass("pl").text();
        //短评数
        if (!StringUtils.isEmpty(plStr)) {
            plStr = this.subIntStrByStr(plStr);
            dbl.setComment(Integer.valueOf(plStr));
        }
        putModel(page, dbl);
    }

    private String subIntStrByStr(String s) {
        if (StringUtils.isEmpty(s)) return null;

        String regEx = "[^0-9]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(s);

        return m.replaceAll("");
    }

    @Override
    public PageRule getPageRule() {
        return rules;
    }

    @Override
    public Site getSite() {
        return this.site;
    }
}
