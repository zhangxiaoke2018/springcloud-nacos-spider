package com.jinguduo.spider.spider.douban;


import java.util.ArrayList;
import java.util.List;

import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.common.code.FetchCodeEnum;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.common.util.RegexUtil;
import com.jinguduo.spider.data.table.DouBanActor;
import com.jinguduo.spider.data.table.DouBanShow;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.selector.Html;


public class DoubanHelper {
    /**
     * get the raw info of the target douban show page
     *
     * @param page target page
     * @return the raw show info
     */
    public static DouBanShow getRawShowFromPage(Page page) {
        String url = page.getUrl().toString();

        Html html = page.getHtml();
        String info = html.xpath("//[@id='info'").get();

        String name = html.xpath("//*[@id=\"content\"]/h1/span[1]/text()").get();
        String code = getSubjectCode(url);
        String year = html.xpath("//*[@id=\"content\"]/h1/span[2]/text()").get();
        if (StringUtils.isNotBlank(year)) {
            year = year.replace("(", "").replace(")", "");
        }


        String screenWrite = getSubjectScreenWrites(html);

        String direct = html.xpath("//*[@id=\"info\"]/span[1]/span[2]/a/text()").get();
        String type = getSubjectType(html);
        String nickName = getSubjectNickName(html);
        String cover = html.xpath("//*[@id=\"mainpic\"]/a/img/@src").get();
        String episodes = RegexUtil.getDataByRegex(info, "<span class=\"pl\">集数:</span>(.*)\\n <br>");
        String duration = RegexUtil.getDataByRegex(info, "<span class=\"pl\">单集片长:</span>(.*)\\n <br>");
        if (StringUtils.isBlank(duration)) {
            duration = RegexUtil.getDataByRegex(info, "<span class=\"pl\">片长:</span>(.*)\\n");
        }
        if (StringUtils.isBlank(duration)) {
            duration = html.xpath("//*[@property=\"v:runtime\"]/text()").get();
        }


        String intro = html.xpath("//*[@id=\"link-report\"]/span/text()").get();
        if (StringUtils.isBlank(intro)) {
            intro = html.xpath("//*[@id=\"link-report\"]/span[2]/text()").get();
        }
        List<DouBanActor> actors = getSubjectActors(page);
        String actorNames = "";
        for (int i = 0; i < actors.size(); i++) {
            DouBanActor actor = actors.get(i);
            if (StringUtils.isNotBlank(actorNames)) {
                actorNames += "/";
            }
            actorNames += actor.getName();
        }
        DouBanShow show = new DouBanShow();
        show.setName(name);
        show.setUrl(url);
        show.setCode(code);
        show.setYear(year);
        show.setDirector(direct);
        show.setType(type);
        show.setNickName(nickName);
        show.setActorNames(actorNames);
        show.setCover(cover);
        show.setIntro(intro);
        show.setActors(actors);
        show.setDuration(duration);
        show.setScreenWrite(screenWrite);
        if (StringUtils.isNotBlank(episodes)) {
            Integer episode = Integer.valueOf(episodes);
            show.setEpisodes(episode);
        }
        return show;
    }

    private static String getSubjectScreenWrites(Html html) {
        List<String> screenWrites = html.xpath("//*[@id=\"info\"]/span[2]/span[2]/a/text()").all();
        String join = StringUtils.join(screenWrites, "/");
        return StringUtils.isBlank(join) ? null : join;
    }


    /**
     * 从page中获取原始的actor信息
     *
     * @param page 抓取的page
     * @return 从page中提取的信息
     */
    public static DouBanActor getRawActorFromPage(Page page) {
        Html html = page.getHtml();
        String url = page.getUrl().toString();
        String info = html.xpath("//div[@class='info'").get();
        String name = html.xpath("//*[@id=\"content\"]/h1/text()").get();
        String douBanPic = html.xpath("//a[@class='nbg']/img/@src").get();

        String sex = RegexUtil.getDataByRegex(info, "<span>性别</span>: (.*)<");
        String constellation = RegexUtil.getDataByRegex(info, "<span>星座</span>: (.*)<");
        String birthplace = RegexUtil.getDataByRegex(info, "<span>出生地</span>: (.*)<");
        String birthday = RegexUtil.getDataByRegex(info, "<span>出生日期</span>: (.*)<");
        String job = RegexUtil.getDataByRegex(info, "<span>工作</span>: (.*)<");
        String imdbNumber = RegexUtil.getDataByRegex(info, "<span>imdb编号</span>:.*<a .*?>([^<]*)</a>");

        String otherEnglishName = RegexUtil.getDataByRegex(info, "<span>更多外文名</span>: (.*)<");
        String otherChineseName = RegexUtil.getDataByRegex(info, "<span>更多中文名</span>: (.*)<");

        DouBanActor actor = new DouBanActor();
        actor.setName(name);
        actor.setUrl(url);
        actor.setCover(douBanPic);
        actor.setSex(sex);
        actor.setConstellation(constellation);
        actor.setBirthplace(birthplace);
        actor.setBirthday(birthday);
        actor.setJob(job);
        actor.setImdbNumber(imdbNumber);
        actor.setOtherEnglishName(otherEnglishName);
        actor.setOtherChineseName(otherChineseName);

        actor.setCode(FetchCodeEnum.getCode(actor.getUrl()));

        return actor;
    }

    private static String getSubjectCode(String url) {
        return url.split("/")[4];
    }

    // 获取剧的类别
    private static String getSubjectType(Html html) {
        List<String> typeList = html.xpath("//span[@property=\"v:genre\"]/text()").all();
        if (typeList == null || typeList.size() == 0) {
            return null;
        }

        String typeStr = "";
        for (int i = 0; i < typeList.size(); i++) {
            if (StringUtils.isNotBlank(typeStr)) {
                typeStr += "/";
            }
            typeStr += typeList.get(i);
        }
        return typeStr;
    }


    // 获取剧的别名
    private static String getSubjectNickName(Html html) {
        String info = html.xpath("//[@id='info'").get();
        return RegexUtil.getDataByRegex(info, "<span class=\"pl\">又名:</span>(.*)\\n <br>");
    }

    private static List<DouBanActor> getSubjectActors(Page page) {
        Html html = page.getHtml();
        List<String> list = html.xpath("//span[@class='actor']/*[@class=\"attrs\"]/a").all();
        List<DouBanActor> actors = new ArrayList<>();

        for (String s : list) {
            Document document = Jsoup.parse(s);
            Element node = document.select("a").first();
            String url = node.attr("href");
            String name = node.text();
            DouBanActor actor = new DouBanActor();
            actor.setName(name);
            if (url.contains("celebrity")) {
                actor.setUrl(url);
                actor.setCode(FetchCodeEnum.getCode(actor.getUrl()));
                actors.add(actor);
            }

        }
        return actors;
    }

    public static List<Job> getJobsFromActors(List<DouBanActor> actors, Job oldJob) {
        List<Job> jobs = new ArrayList<>();
        actors.forEach(actor -> {
            //适配doubanactor表中url错误数据问题，更改需小心！！！
            if (StringUtils.isNotBlank(actor.getUrl())) {
                String url = actor.getUrl().contains("https://") ? actor.getUrl() : ("https://movie.douban.com" + actor.getUrl());
                Job job = DbEntityHelper.derive(oldJob, new Job(url));
                jobs.add(job);
            }
        });
        if (jobs.size() > 0) {
            return jobs;
        }
        return null;
    }
}
