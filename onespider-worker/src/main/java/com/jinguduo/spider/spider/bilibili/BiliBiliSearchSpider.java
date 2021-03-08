package com.jinguduo.spider.spider.bilibili;

import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.constant.FrequencyConstant;
import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.data.table.*;
import com.jinguduo.spider.webmagic.Page;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.ParseException;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @DATE 27/07/2017 10:09
 */
@Slf4j
@Worker
public class BiliBiliSearchSpider extends CrawlSpider {

    private Site site = SiteBuilder.builder().setDomain("search.bilibili.com").build();

    private PageRule rules = PageRule.build()
            .add("totalrank", page -> totalrank(page)) // 综合排序查询视频数
            .add("click$", page -> click(page)) // 最多点击排序
            .add("dm$", page -> dm(page)) // 最多弹幕排序
            .add("stow$", page -> stow(page)) // 最多收藏排序
            ;

    /* createPlayPageJob(page, url);*/
    private void totalrank(Page page) {
        Job job = ((DelayRequest) page.getRequest()).getJob();
        Document document = page.getHtml().getDocument();
        String total = document.getElementsByTag("body").get(0).attr("data-total");
        if (StringUtils.isBlank(total) || StringUtils.equals(total, "0")) {
            return;
        }
        Integer totalNum = Integer.valueOf(total.replace("+", ""));
        String code = job.getCode();
        String url = job.getUrl();
        Byte type = this.biliGetTypeByUrl(url);
        if (type == null) {
            log.error("search.bilibili.com error ,this url get cout is null -->{}", url);
            return;
        }
        BilibiliVideoCount bc = new BilibiliVideoCount();
        bc.setCode(code);
        bc.setType(type);
        bc.setCount(totalNum);
        putModel(page, bc);


        /**
         * 生成获取弹幕的任务
         * */
        Elements elements = document.getElementsByClass("ajax-render");
        if (null == elements || elements.size() == 0) {
            return;
        }
        Element ul = elements.get(0);
        Elements lis = ul.getElementsByTag("li");

        for (int i = 0; i < lis.size(); i++) {
            Element li = lis.get(i);
            Element a = li.getElementsByTag("a").get(0);
            String danmuUrl = a.attr("href");
            createPlayPageJob(page, danmuUrl);
        }

    }

    private void click(Page page) throws ParseException {
        Job job = ((DelayRequest) page.getRequest()).getJob();
        Document document = page.getHtml().getDocument();
        Elements elements = document.getElementsByClass("video-contain clearfix");



        if (null == elements || elements.size() == 0) {
            return;
        }
        Element ul = elements.get(0);
        Elements lis = ul.getElementsByTag("li");

        for (int i = 0; i < lis.size(); i++) {

            if (i >= 5) { // 只取前5个
                break;
            }

            Element li = lis.get(i);
            //bili dataId 唯一标识
            String dataId = li.getElementsByClass("headline clearfix").attr("type avid");

            Element a = li.getElementsByTag("a").get(0);
            String title = a.attr("title");
            String url = a.attr("href");
            Element tag = li.getElementsByClass("tags").get(0);
            Elements spans = tag.getElementsByTag("span");

            BilibiliVideoClick click = new BilibiliVideoClick(title, url);
            String playCount = spans.get(0).text();//播放量
            click.setStrPlayCount(playCount);

            click.setDataId(dataId);

            String danmakuCount = spans.get(1).text();//弹幕量
            click.setStrDanmakuCount(danmakuCount);

            String postDate = spans.get(2).text();//上传时间
            click.setPostDate(DateUtils.parseDate(postDate, "yyyy-MM-dd"));
            String postUser = spans.get(3).text();//up主
            click.setPostUser(postUser);

            click.setCode(job.getCode());
            putModel(page, click);
        }
    }


    private void
    dm(Page page) {
        Job job = ((DelayRequest) page.getRequest()).getJob();
        Document document = page.getHtml().getDocument();
        Elements elements = document.getElementsByClass("video-contain clearfix");
        if (null == elements || elements.size() == 0) {
            return;
        }
        Element ul = elements.get(0);
        Elements lis = ul.getElementsByTag("li");

        for (int i = 0; i < lis.size(); i++) {

            if (i >= 5) { // 只取前三个
                break;
            }

            Element li = lis.get(i);

            //bili dataId 唯一标识
            String dataId = li.getElementsByClass("headline clearfix").attr("type avid");

            Element a = li.getElementsByTag("a").get(0);
            String title = a.attr("title");
            String url = a.attr("href");
            Element tag = li.getElementsByClass("tags").get(0);
            Elements spans = tag.getElementsByTag("span");

            BilibiliVideoDm dm = new BilibiliVideoDm(title, url);
            dm.setDataId(dataId);
            String playCount = spans.get(0).text();//播放量
            dm.setStrPlayCount(playCount);

            String danmakuCount = spans.get(1).text();//弹幕量
            dm.setStrDanmakuCount(danmakuCount);

            String postDate = spans.get(2).text();//上传时间
            try {
                dm.setPostDate(DateUtils.parseDate(postDate, "yyyy-MM-dd"));
            } catch (ParseException e) {
                log.error(e.getMessage(), e);
            }
            String postUser = spans.get(3).text();//up主
            dm.setPostUser(postUser);

            dm.setCode(job.getCode());
            putModel(page, dm);
        }

    }

    private void createPlayPageJob(Page page, String url) {
        Job oldJob = ((DelayRequest) page.getRequest()).getJob();
        if (!url.startsWith("http:")) {
            url = "https:" + url;
        }
        Job job = new Job(url);
        DbEntityHelper.derive(oldJob, job);
        job.setFrequency(FrequencyConstant.COMMENT_TEXT);
        job.setPlatformId(31);
        putModel(page, job);
    }

    private void stow(Page page) throws ParseException {
        Job job = ((DelayRequest) page.getRequest()).getJob();
        Document document = page.getHtml().getDocument();
        Element ul = document.getElementsByClass("ajax-render").get(0);
        Elements lis = ul.getElementsByTag("li");

        for (int i = 0; i < lis.size(); i++) {

            if (i >= 5) { // 只取前三个
                break;
            }

            Element li = lis.get(i);

            //bili dataId 唯一标识
            String dataId = li.getElementsByClass("search-watch-later icon-later-off").attr("data-aid");

            Element a = li.getElementsByTag("a").get(0);
            String title = a.attr("title");
            String url = a.attr("href");
            Element tag = li.getElementsByClass("tags").get(0);
            Elements spans = tag.getElementsByTag("span");

            BilibiliVideoStow stow = new BilibiliVideoStow(title, url);
            stow.setDataId(dataId);
            String playCount = spans.get(0).text();//播放量
            stow.setStrPlayCount(playCount);

            String danmakuCount = spans.get(1).text();//弹幕量
            stow.setDanmakuCount(Integer.valueOf(danmakuCount));

            String postDate = spans.get(2).text();//上传时间
            stow.setPostDate(DateUtils.parseDate(postDate, "yyyy-MM-dd"));
            String postUser = spans.get(3).text();//up主
            stow.setPostUser(postUser);

            stow.setCode(job.getCode());
            putModel(page, stow);
        }
    }


    private Byte biliGetTypeByUrl(String url) {
        String totalrank = url.substring(url.indexOf("totalrank") + 9);
        Byte type = 0;
        switch (totalrank) {
            case "":
                type = 0;
                break;
            case "&tids_1=3":
                type = 1;
                break;
            case "&tids_1=129":
                type = 2;
                break;
            case "&tids_1=160":
                type = 3;
                break;
            case "&tids_1=119":
                type = 4;
                break;
            case "&tids_1=5":
                type = 5;
                break;
            case "&tids_1=23":
                type = 6;
                break;
            case "&tids_1=11":
                type = 7;
                break;
            case "&tids_1=1":
                type = 8;
                break;
            case "&tids_1=13":
                type = 9;
                break;
            case "&tids_1=167":
                type = 10;
                break;
            case "&tids_1=4":
                type = 11;
                break;
            case "&tids_1=36":
                type = 12;
                break;
            case "&tids_1=155":
                type = 13;
                break;
            case "&tids_1=165":
                type = 14;
                break;
            default:
                type = null;
                break;
        }
        return type;
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
