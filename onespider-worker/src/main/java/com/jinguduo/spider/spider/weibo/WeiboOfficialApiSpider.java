package com.jinguduo.spider.spider.weibo;

import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.jinguduo.spider.common.exception.AntiSpiderException;
import org.apache.commons.lang3.StringUtils;

import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.data.table.WeiboOfficialLog;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.selector.Html;
import com.jinguduo.spider.webmagic.selector.Selectable;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


@Slf4j
@Worker
public class WeiboOfficialApiSpider extends CrawlSpider {


    private Site site = SiteBuilder.builder()
            .setDomain("weibo.com")
            .setUserAgent("\"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.1 (KHTML, like Gecko) Chrome/22.0.1207.1 Safari/537.1\"")
            .addHeader("Connection","keep-alive")
            .addHeader("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3")
            .addHeader("Accept-Encoding","gzip, deflate, br")
            .addHeader("Accept-Language","zh-CN,zh;q=0.9")
            .addCookie("Cookie","SUBP=0033WrSXqPxfM72wWs9jqgMF55529P9D9W5GFdE6.C7laCRkjeTxrHe45JpVF02Reo27ehqXeK.0;SUB=_2AkMpfdlUdcPxrAVRmfwQyG_gbY5H-jyaqLCiAn7uJhMyAxh77m1TqSVutBF-XIn0-PK-VwTV_U7zrWgqi1Z9I8-S;")
            .build();

    private PageRule rules = PageRule.build()
            .add("", page -> analyzeWeiboMovieDataProcess(page));


    /***
     * @Title 解析微博数据
     * @param page
     */
    private void analyzeWeiboMovieDataProcess(Page page) {
        Job job = ((DelayRequest) page.getRequest()).getJob();
        if (job == null) {
            log.error("job is null");
            return;
        }
        Integer fansCount = -1;
        Integer followCount = -1;
        Integer postCount = -1;

        /** 新的抓取逻辑，page 适应于爬虫的 html */
        Html html = page.getHtml();

        /** 校验是否是有效页面 */
        String title = html.$("title").get();
        if (StringUtils.isBlank(title)){
            throw new AntiSpiderException("weibo official page is empty! code: " + job.getCode());
        }

        /** 关注、粉丝、微博 */
//        List<Selectable> selectables = html.$(".tb_counter").xpath("td").nodes();
        List<Selectable> script = html.$("script").nodes();
        String fansHtmlString=null;
        for(Selectable s: script){
            if(s.get().contains("关注") && s.get().contains("粉丝") && s.get().contains("微博") && s.get().contains("Pl_Core_T8CustomTriColumn__3")){
                String replace = s.get().replace("\\t", "").replace(" \\n", "").replace("\\r", "");
                String substring = replace.substring(replace.indexOf("<script>FM.view(") + 16, replace.indexOf(")</script>"));
                fansHtmlString = JSONObject.parseObject(substring).get("html").toString();
            }
        }
        if(StringUtils.isBlank(fansHtmlString)) {
            log.info("WeiboOfficialSpider html is null ! Script size is : " + script.size() +"    fansScript is "+script.toString() + "  html is " + html.toString());
            return;
        }

        Document document = Jsoup.parse(fansHtmlString);

        Elements td =  document.getElementsByTag("td");

        for(Element e : td){
            if(e.toString().contains("关注")){
                followCount= Integer.parseInt(e.getElementsByTag("strong").text());
            }else if (e.toString().contains("粉丝")) {
                fansCount = Integer.valueOf(e.getElementsByTag("strong").text());
            } else if (e.toString().contains("微博")) {
                postCount = Integer.valueOf(e.getElementsByTag("strong").text());
            }
        }

        /**获取官微的名称（username）lc*/
        String weiboName = "";
        weiboName = StringUtils.replace(StringUtils.replace(title, "<title>", ""), "的微博_微博</title>", "");
        if (StringUtils.isBlank(weiboName)) {
            weiboName = html.xpath("h1[@class='username']/text()").get();
        }
        if (StringUtils.isBlank(weiboName)) {
            weiboName = html.regex("CONFIG\\[\'onick\'\\]=\\'(.*?)\\'\\;",1).get();
        }
        if (StringUtils.isBlank(weiboName)) {
            weiboName = html.xpath("//h1[@class='username']/text()").get();
        }
        if (StringUtils.isBlank(weiboName)) {
            weiboName = html.xpath("//title/text()").regex("(.*?)的微博_微博",1).get();
        }
        if (StringUtils.isBlank(weiboName)) {
            log.error("xpath:\"//title/text()\"; title :{}, code:{}",html.toString(),job.getCode());
        }
        //save weibo data
        WeiboOfficialLog weiboOfficialLog = new WeiboOfficialLog();
        weiboOfficialLog.setCode(job.getCode());
        weiboOfficialLog.setFansCount(fansCount);
        weiboOfficialLog.setFollowCount(followCount);
        weiboOfficialLog.setPostCount(postCount);
        weiboOfficialLog.setWeiboName(weiboName);

        putModel(page, weiboOfficialLog);

        log.debug("=========analyze weibo process end=========");
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
