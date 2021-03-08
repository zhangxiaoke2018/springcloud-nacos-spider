package com.jinguduo.spider.spider.weibo;

import com.alibaba.fastjson.JSONObject;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.common.util.NumberHelper;
import com.jinguduo.spider.data.table.WeiboTagLog;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.selector.Html;
import com.jinguduo.spider.webmagic.selector.Selectable;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Deprecated
@Worker
@CommonsLog
public class WeiboTagSpider extends CrawlSpider {

    private Site site = SiteBuilder.builder()
            .setDomain("huati.weibo.com")
            .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.96 Safari/537.36")
            .addHeader("Cookie", "SUB=_2AkMp3iycdcPxrAZWm_oTzWvnZY5H-jyaC0VqAn7uJhMyAxh77g0gqSVutBF-XDGKOD2XBZbtny8HVYb7tmOLhqep;")
            .build();

    private PageRule rules = PageRule.build()
            .add("/k/", p -> processTopicData(p));


    private void processTopicData(Page page) throws UnsupportedEncodingException {
        Job job = ((DelayRequest) page.getRequest()).getJob();

        Html html = page.getHtml();
        long readCount = -1L;
        int feedCount = -1;
        int followCount = -1;

        String decode = URLDecoder.decode(job.getUrl(), "UTF-8");
        List<Selectable> scripts = html.$("script").nodes();
        String fansHtmlString = null;
        for (Selectable s : scripts) {
            if (s.get().contains("阅读") && s.get().contains("帖子") && s.get().contains("Pl_Core_T8CustomTriColumn__262")) {
                String replace = s.get().replace("\\t", "").replace(" \\n", "").replace("\\r", "");
                String substring = replace.substring(replace.indexOf("<script>FM.view(") + 16, replace.indexOf(")</script>"));
                fansHtmlString = JSONObject.parseObject(substring).get("html").toString();
            }
        }
        if (StringUtils.isBlank(fansHtmlString)) {
//            log.info("WeiboTagSpider html is null ! Script size is : " + scripts.size() + "  html is " + html.toString());
            return;
        }
        Document document = Jsoup.parse(fansHtmlString);

        Elements td = document.getElementsByTag("td");
        for (Element e : td) {
            if (e.toString().contains("阅读")) {
                String read = e.getElementsByTag("strong").text();
                readCount = numberFormat(read);
            } else if (e.toString().contains("帖子")) {
                String feed = e.getElementsByTag("strong").text();
                feedCount = numberFormat(feed);
            } else {  //此处 文本值不定，不能根据 "粉丝" 来判定
                String follow = e.getElementsByTag("strong").text();
                followCount = numberFormat(follow);

            }
        }
        WeiboTagLog wlog = new WeiboTagLog();
        String keyword = decode.substring(decode.lastIndexOf("/") + 1, decode.length());
        if (keyword.contains("&page")){
            keyword = keyword.substring(0,keyword.indexOf("&page"));
        }
        wlog.setKeyword(keyword);
        wlog.setParentCode(job.getParentCode());
        wlog.setCode(job.getCode());
        wlog.setReadCount(readCount);
        wlog.setFeedCount(feedCount);
        wlog.setFollowCount(followCount);
        putModel(page, wlog);
    }

//    }


    @Override
    public Site getSite() {
        return site;
    }

    @Override
    public PageRule getPageRule() {
        return rules;
    }

    public static Integer numberFormat(String text) {
        Integer count;
        if (text.contains("万")) {
            Double aDouble = Double.valueOf(text.replace("万", ""));
            count = (int) (aDouble * 10000);
            return count;
        } else if (text.contains("亿")) {
            Double aDouble = Double.valueOf(text.replace("亿", ""));
            count = (int) (aDouble * 100000000);
            return count;
        } else {
            count = Integer.valueOf(text);
            return count;
        }
    }
}
