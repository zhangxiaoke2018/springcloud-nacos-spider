package com.jinguduo.spider.spider.youku;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Sets;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.code.FetchCodeEnum;
import com.jinguduo.spider.common.constant.CommonEnum;
import com.jinguduo.spider.common.exception.PageBeChangedException;
import com.jinguduo.spider.common.util.*;
import com.jinguduo.spider.data.table.Category;
import com.jinguduo.spider.data.table.Show;
import com.jinguduo.spider.data.table.VipEpisode;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.selector.Html;
import com.jinguduo.spider.webmagic.selector.PlainText;
import com.jinguduo.spider.webmagic.selector.Selectable;
import com.jinguduo.spider.webmagic.utils.UrlUtils;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 优酷分类页爬虫
 */
@SuppressWarnings("all")
@Worker
@CommonsLog
public class YoukuListSpider extends CrawlSpider {

    private Site site = SiteBuilder.builder().setDomain("list.youku.com").build();

    private static final String G_EPI_JOB_URL = "http://list.youku.com/show/module?id=%s&tab=point&callback=jQuery";

    //用来获取分集的tab 分集的分页
    private static final String EPI_TAB_JOB_URL = "http://list.youku.com/show/module?id=%s&tab=showInfo&callback=jQuery";

    private static final String EPI_TAB_SHOW_JOB_URL = "http://list.youku.com/show/point?id=%s&stage=%s&callback=jQuery";

    private static final String EPI_TAB_ITEM_URL = "http://list.youku.com/show/episode?id=%s&stage=%s&callback=jQuery";

    private static final String VARIETY_EPI_SHOW_JOB_URL = "http://api.m.youku.com/api/showlist/getshowlist?vid=%s&showid=%s&cateid=85&pagesize=1&page=0";

    private static final String SOKU_SEARCH_PLAYCOUNT_URL = "http://www.soku.com/search_video/q_%s";

    private static final String DETAIL_URL = "https://list.youku.com/show/id_%s.html";

    private final static String URL_PREFIX = "http:";

    private final static String V_DETAIL_URL = "https://v.youku.com/v_show/id_%s.html";

    private final static String HOT_COUNT_URL = "https://acs.youku.com/h5/mtop.youku.haixing.play.h5.detail/1.0/?jsv=2.5.0&appKey=24679788&&v=1.0&type=originaljson&dataType=json&api=mtop.youku.haixing.play.h5.detail";
    private final static String data = "{\"device\":\"H5\",\"layout_ver\":\"100000\",\"system_info\":\"{\\\"device\\\":\\\"H5\\\",\\\"pid\\\":\\\"0d7c3ff41d42fcd9\\\",\\\"guid\\\":\\\"1547803393171S2M\\\",\\\"utdid\\\":\\\"1547803393171S2M\\\",\\\"ver\\\":\\\"1.0.0.0\\\",\\\"userAgent\\\":\\\"Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.98 Mobile Safari/537.36\\\"}\",\"video_id\":\"%s\"}";

    private final static String DANMU_DATA = "{\"pid\":0,\"ctype\":10004,\"sver\":\"3.1.0\",\"cver\":\"v1.0\",\"ctime\":%s,\"guid\":\"EtW3F0kASSgCAXLz3WlAIEkl\",\"vid\":\"%s\",\"mat\":3,\"mcount\":1,\"type\":1}";
    private final static String DANMU_URL="https://acs.youku.com/h5/mopen.youku.danmu.list/1.0/?jsv=2.5.1&appKey=24679788&api=mopen.youku.danmu.list&v=1.0&type=originaljson&dataType=jsonp&timeout=20000&jsonpIncPrefix=utility";
    //日漫自动发现翻页任务
    private final static String JP_KID_ANIME = "https://list.youku.com/category/page?c=100&s=6&d=1&a=日本&type=show&p=1";


    /***
     * "一夫多妻"==》欢乐戏剧人一期存在多个子期，
     * ep: 20170515 包含 1(正常),2(完整),3(超长)等期数
     * <strong>我们规定：</strong>
     * <ul>
     *     <li>正常->2017051501;
     *     <li>完整->2017051502;
     *     <li>超长->2017051503;
     */
    private final static Set<String> SPECIL_POLYGAMY = Sets.newHashSet("z727f4876d18d11e69c81");

    private PageRule rules = PageRule.build()
            .add("/show/id_", page -> processPC(page))//处理总播放量
            .add("/show/id_.*(?<!#movie_play_count)$", page -> createEpisodeJobs(page))//创建可生成剧集信息的job
            .add("tab=point", page -> processEpisodeShows(page))//生成分集show信息
            .add("tab=showInfo", page -> processGenEpiJobs(page))//生成所有的分集任务
            .add("stage=", page -> processEpisodeShows(page))//每个tab下的所有分集信息
            .add("list\\.youku\\.com/show/episode", page -> itemList(page))//专题页：Tab剧集列表，解析分集show(只有欢乐戏剧人会生成该任务，其他剧如需适配请自行测试)
            .add("/category/show/", page -> createAutoFindJob(page))//创建自动发现任务
            .add("/category/page", page -> processAutoFind(page))//处理自动发现

            ;


    /***
     * 获取总播放量
     * @param page
     */
    private void processPC(Page page) {
        Job oldJob = ((DelayRequest) page.getRequest()).getJob();
//        String playCountStr = page.getHtml().xpath("//div[@class=p-base]").regex("<li>总播放数\\：(.*?)<\\/li>", 1).get();
//        if (StringUtils.isBlank(playCountStr)) {//详情页无播放量一栏，通过搜索页获取
//            String title = page.getHtml().xpath("//div[@class=p-base]//li[@class='p-title']/text()").get();
//            if (StringUtils.isNotBlank(title)) {
//                title = title.replace("：", "").replace(" ", "%20");
//                Job job = new Job(String.format(SOKU_SEARCH_PLAYCOUNT_URL, title));
//                DbEntityHelper.derive(oldJob, job);
//                putModel(page, job);
//            }
//        } else {
//            Long playCount = NumberHelper.parseLong(playCountStr, -1);
//            ShowLog showLog = new ShowLog();
//            DbEntityHelper.derive(oldJob, showLog);
//            showLog.setPlayCount(playCount);
//            putModel(page, showLog);
//        }
        // 生成热度任务
        String playUrl = page.getHtml().xpath("//div[@class=p-play]/a/@href").get();
        String vid = "";
        if (playUrl.contains("=")) {
            vid = playUrl.substring(playUrl.indexOf("id_") + 3, playUrl.indexOf("="));
        } else {
            vid = playUrl.substring(playUrl.indexOf("id_") + 3, playUrl.indexOf(".html"));
        }
        try {
            String d = URLEncoder.encode(String.format(data, vid), "UTF-8");
            Job hotCountJob = new Job(HOT_COUNT_URL + "&data=" + d);
            hotCountJob.setCode(oldJob.getCode());
            putModel(page, hotCountJob);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }


    }

    /**
     * 通过最初 集信息的job
     *
     * @param page
     */
    private void createEpisodeJobs(Page page) {

        Job oldJob = ((DelayRequest) page.getRequest()).getJob();

        String showId = page.getHtml().regex("showid:\"(.*?)\"", 1).get();
        if (StringUtils.isBlank(showId)) {
            showId = page.getHtml().regex("showid_en:\"(.*?)\"", 1).get();
            if (StringUtils.isBlank(showId)) {
                //一般是404页会走进来
                throw new PageBeChangedException("get youku showId fail");
            }
        }

        if (!SPECIL_POLYGAMY.contains(oldJob.getCode())) {//欢乐戏剧人不需要这个任务
            Job job = new Job(String.format(G_EPI_JOB_URL, showId));
            DbEntityHelper.derive(oldJob, job);
            putModel(page, job);
        }

        Job job2 = new Job(String.format(EPI_TAB_JOB_URL, showId));
        DbEntityHelper.derive(oldJob, job2);
        putModel(page, job2);
    }

    /**
     * html内容为"<ul"开头的才进行处理
     *
     * @param page
     */
    private void processGenEpiJobs(Page page) {
        Job oldJob = ((DelayRequest) page.getRequest()).getJob();
        String json = RegexUtil.getDataByRegex(page.getRawText(), "(?<=jQuery\\()(.+?)(?=\\);)");

        JSONObject jsonObject = JSON.parseObject(json);
        String html = jsonObject.getString("html");

        if (StringUtils.isBlank(html)) {
            return;
        }
        Document document = Jsoup.parse(html);

        /**
         * 处理vip数据
         * 情况1
         * */
        Elements episodes = document.getElementsByClass("p-drama-grid fix");
        Elements episodes2 = document.getElementsByClass("p-drama-full-row fix");
        /*****
         * anime
         */
        Elements animeEpisode = document.getElementsByClass("p-panel");
        //查询到的话
        if (null != episodes && episodes.size() != 0) {
            Element element = episodes.get(0);
            Elements lis = element.getElementsByTag("li");

            List<Element> vips = lis.stream().filter(p -> p.html().contains("p-icon p-icon-vip")).collect(Collectors.toList());

            for (int i = 0; i < vips.size(); i++) {
                Element p = vips.get(i);
                VipEpisode ve = new VipEpisode();
                String href = p.getElementsByTag("a").get(0).attr("href");
                String code = FetchCodeEnum.getCode(href);
                ve.setCode(code);
                ve.setPlatformId(oldJob.getPlatformId());
                putModel(page, ve);
            }
            /**
             * 处理vip数据
             * 情况2
             * */
        } else if (null != episodes2 && episodes2.size() != 0) {
            Element element = episodes2.get(0);
            Elements lis = element.getElementsByTag("li");
            List<Element> vips = lis.stream().filter(p -> p.html().contains("p-icon p-icon-vip")).collect(Collectors.toList());
            vips.stream().forEach(p -> {
                VipEpisode ve = new VipEpisode();
                String href = p.getElementsByTag("a").get(0).attr("href");
                String code = FetchCodeEnum.getCode(href);
                ve.setCode(code);
                ve.setPlatformId(oldJob.getPlatformId());
                putModel(page, ve);
            });
            Elements dts = element.getElementsByTag("dt");
            for (Element e : dts) {
                String dtAllText = e.text().trim().toString();
                String title = e.getElementsByTag("a").text();
                String epiString = dtAllText.replace(title, "");
                Integer ep = Integer.parseInt(epiString);
                String href = e.getElementsByTag("a").attr("href");
                String code = FetchCodeEnum.getCode(href);
                Show show = new Show();
                show.setPlatformId(oldJob.getPlatformId());
                show.setDepth(2);
                show.setName(title);
                show.setCode(code);
                show.setEpisode(ep);
                if (href != null && href.startsWith("//")) {
                    href = "https:" + href;
                }
                show.setUrl(href);
                //new PlainText(item.getElementsByAttribute("title").get(0))
                show.setParentId(oldJob.getShowId());
                show.setParentCode(oldJob.getCode());
                putModel(page, show);

            }
        } else if (null != animeEpisode && animeEpisode.size() != 0) {
            Element episode = animeEpisode.get(0);
            Elements divLists = episode.getElementsByClass("p-item");
            for (int i = 0; i < divLists.size(); i++) {
                Element e = divLists.get(i);
                Integer epiNum = Integer.valueOf(e.text().replaceAll("[^(0-9)]", ""));
                Show show = new Show();
                show.setEpisode(epiNum);
                String showName = e.text().replaceAll("[^(\\u4e00-\\u9fa5)]", "");
                show.setName(showName);
                Element a = e.getElementsByClass("c555").get(0);
                String href = a.attr("href");
                String code = href.substring(href.indexOf("/id_") + 4, href.indexOf(".html"));
                show.setCode(code);
                show.setDepth(2);
                show.setPlatformId(3);
                if (href != null && href.startsWith("//")) {
                    href = "https:" + href;
                }
                show.setUrl(href);
                show.setParentCode(oldJob.getCode());
//                log.info("youku分集，depth=2，code="+code);
                putModel(page, show);

                //depth=2的任务移到YoukudetailSpder
                if(i>=1) continue;
                Job detailJob = new Job(String.format(V_DETAIL_URL, code));
                DbEntityHelper.derive(oldJob, detailJob);
                detailJob.setCode(code);
                detailJob.setParentCode(oldJob.getCode());
                putModel(page, detailJob);

            }
        }

        //html为空，可能是url中object id 为0造成错误信息返回
        if (!html.startsWith("<ul")) {
            //这里是因为都是预告或者没有剧集信息的剧，也有可能是网大的详情页。不符合规则，需要过滤掉
            return;
        }
        Integer showId = jsonObject.getJSONObject("data").getInteger("id");


        Element ul = document.getElementsByTag("ul").get(0);
        Elements lis = ul.getElementsByTag("li");
        for (Element li : lis) {
            //只是欢乐戏剧人生成新任务，其他流程都正常就不动了
            String dataId = li.getElementsByAttribute("data-id").get(0).attr("data-id");
            Job job = new Job(String.format(EPI_TAB_ITEM_URL, showId, dataId));
            DbEntityHelper.derive(oldJob, job);
            putModel(page, job);

            Job job2 = new Job(String.format(EPI_TAB_SHOW_JOB_URL, showId, dataId));
            DbEntityHelper.derive(oldJob, job2);
            putModel(page, job2);
        }
    }


    private void processEpisodeShows(Page page) {

        Job oldJob = ((DelayRequest) page.getRequest()).getJob();

        if (SPECIL_POLYGAMY.contains(oldJob.getCode())) {//欢乐戏剧人不走这个逻辑
            return;
        }

        String showId = RegexUtil.getDataByRegex(page.getRequest().getUrl(), "id=(\\d*)", 1);
        String json = RegexUtil.getDataByRegex(page.getRawText(), "(?<=jQuery\\()(.+?)(?=\\);)");

        JSONObject data = JSON.parseObject(json);

        String message = data.getString("message");

        if (!"success".equals(message)) {
            //这里报错是因为该分集段本身没有数据
            return;
        }

        Document document = Jsoup.parse(data.getString("html"));

        /**
         * 处理vip数据
         * 情况1
         * */
        Elements episodes = document.getElementsByClass("p-drama-grid fix");
        Elements episodes2 = document.getElementsByClass("p-drama-full-row fix");

        //查询到的话
        if (null != episodes && episodes.size() != 0) {
            Element element = episodes.get(0);
            Elements lis = element.getElementsByTag("li");

            List<Element> vips = lis.stream().filter(p -> p.html().contains("p-icon p-icon-vip")).collect(Collectors.toList());
            vips.stream().forEach(p -> {
                VipEpisode ve = new VipEpisode();
                String href = p.getElementsByTag("a").get(0).attr("href");
                String code = FetchCodeEnum.getCode(href);
                ve.setCode(code);
                ve.setPlatformId(oldJob.getPlatformId());
                putModel(page, ve);


            });
            /**
             * 处理vip数据
             * 情况2
             * */
        } else if (null != episodes2 && episodes2.size() != 0) {
            Element element = episodes2.get(0);
            Elements lis = element.getElementsByTag("li");
            List<Element> vips = lis.stream().filter(p -> p.html().contains("p-icon p-icon-vip")).collect(Collectors.toList());
            vips.stream().forEach(p -> {
                VipEpisode ve = new VipEpisode();
                String href = p.getElementsByTag("a").get(0).attr("href");
                String code = FetchCodeEnum.getCode(href);
                ve.setCode(code);
                ve.setPlatformId(oldJob.getPlatformId());
                putModel(page, ve);
            });
        }


        Elements items = document.getElementsByClass("item-title");
        for (int i = 0; i < items.size(); i++) {
            Element item = items.get(i);
            String url = "https:" + item.getElementsByTag("a").get(0).attr("href");
            Integer epi = 0;
            String code = FetchCodeEnum.getCode(url);

            //TODO 若能直接判断是综艺则可以直接走获取综艺 show的任务，现在无法判断要创建的这个show是否为综艺，所以还不能完全统一综艺的分集格式为yyyyMMdd
            String bh = new PlainText(item.getElementsByTag("a").get(0).attr("title")).regex("\\s(\\d*)$", 1).get();
            String title = item.getElementsByTag("a").get(0).attr("title");
            if (title.contains("预告") || title.contains("明日精彩") || title.contains("精彩看点") || title.matches(".*第(\\S*)版.*") || title.contains("会员版")) {
                continue;
            }
            try {
                epi = Integer.valueOf(title.replaceAll("\\D", ""));
            }catch (Exception e){
                log.info("Youku List Page Error Show Title :"+title+",code:"+code);
            }
            Show show = new Show();
            show.setPlatformId(oldJob.getPlatformId());
            show.setDepth(2);
            show.setName(title);
            show.setCode(code);
            show.setEpisode(epi);
            show.setUrl(url);
            //new PlainText(item.getElementsByAttribute("title").get(0))
            show.setParentId(oldJob.getShowId());
            show.setParentCode(oldJob.getCode());
            putModel(page, show);

            //depth=2的任务移到YoukudetailSpder
            if(i<1) {
                Job job = new Job(url);
                DbEntityHelper.derive(oldJob, job);
                job.setCode(code);
                job.setParentCode(oldJob.getCode());
                putModel(page, job);
            }



//            if (StringUtils.isBlank(bh)) {
//
//                String epiStr = new PlainText(title).regex("第(\\d+)(集|期)", 1).get();//综艺
//                if (StringUtils.isBlank(epiStr)) {
//                    String epiHan = new PlainText(title).regex("第([个十百千万一二三四五六七八九十零]+)(集|期)", 1).get();//网剧
//                    if (StringUtils.isNotBlank(epiHan)) {
//                        epiStr = NumberHelper.chineseNumber2Int(epiHan).toString();
//                    } else {
//                        //生成能获取日期分集的综艺show的任务
//                        Job job = new Job(String.format(VARIETY_EPI_SHOW_JOB_URL, code, showId));
//                        DbEntityHelper.derive(oldJob, job);
//                        putModel(page, job);
//
//                        Job job2 = new Job(url);
//                        DbEntityHelper.derive(oldJob, job2);
//                        job2.setCode(code);
//                        putModel(page, job2);
//
//                        continue;
//                    }
//                }
//                epi = EpisodeUtils.youkuEpisodeHandle(epiStr);
//                if (epi > 0 && StringUtils.contains(title, "合集")) {
//                    epi = 0;
//                }
//            } else {
//                epi = EpisodeUtils.youkuEpisodeHandle(bh);
//            }



        }
    }

    private void itemList(Page page) {
        Job oldJob = ((DelayRequest) page.getRequest()).getJob();
        String json = RegexUtil.getDataByRegex(page.getRawText(), "(?<=jQuery\\()(.+?)(?=\\);)");
        JSONObject data = JSON.parseObject(json);
        String message = data.getString("message");
        if (!"success".equals(message)) {
            log.error("the youku url is error : " + page.getRequest().getUrl());
            return;
        }
        Html html = new Html(data.getString("html"));

        html.xpath("//div[@class='p-panel']")
                .nodes()
                .stream()
                .forEach(s -> {
                    String reloadKey = s.xpath("///@id").regex("_(\\d{4})", 1).get();
                    for (Selectable item : s.xpath("///ul/li").nodes()) {
                        String showName = item.xpath("///dl/dt/a//@title").get();
                        if (StringUtils.isBlank(showName)) {
                            showName = item.xpath("///a/text()").get();
                        }
                        if (showName.contains("预告") || showName.contains("明日精彩") || showName.contains("精彩看点") || showName.matches(".*第(\\S*)版.*") || showName.contains("会员版")) {
                            continue;
                        }
                        String url = item.xpath("///dl/dt/a//@href").get();
                        if (StringUtils.isBlank(url)) {
                            url = item.xpath("///a//@href").get();
                            if (url.startsWith("//")) {
                                url = "https:" + url;
                            }
                        }
                        String epiStr = item.xpath("///dl/dt/text()").replace("[^0-9]", "").regex("(\\d+)").get();
                        if (StringUtils.isBlank(epiStr)) {
                            epiStr = item.xpath("///div/text()").get();
                        }
                        String code = FetchCodeEnum.getCode(url);

                        Show show = new Show();
                        show.setPlatformId(oldJob.getPlatformId());
                        show.setDepth(2);
                        show.setName(showName);
                        show.setCode(code);
                        show.setParentId(oldJob.getShowId());
                        show.setParentCode(oldJob.getCode());
                        Integer epi = 0;
                        if (SPECIL_POLYGAMY.contains(oldJob.getCode())) {//必须遵守下面的规定，保证和优酷一致，最终实现分组合并数据
                            if (showName.contains("完整版")) {//yyyyMMdd02
                                epi = Integer.valueOf(StringUtils.join(reloadKey, epiStr, "02"));
                            } else if (showName.contains("超长版")) {//yyyyMMdd03
                                epi = Integer.valueOf(StringUtils.join(reloadKey, epiStr, "03"));
                            } else if (epiStr.length() >= 8) { // yyyyMMdd01
                                epi = Integer.valueOf(StringUtils.join(epiStr, "01"));
                            } else {//yyyyMMdd01
                                epi = Integer.valueOf(StringUtils.join(reloadKey, epiStr, "01"));
                            }
                        } else {
                            epi = Integer.valueOf(epiStr);
                        }
                        show.setUrl(url);
                        show.setEpisode(epi);
                        putModel(page, show);

                        Job job = null;
                        job = new Job(url);
                        DbEntityHelper.derive(oldJob, job);
                        job.setCode(code);
                        job.setParentCode(oldJob.getParentCode());
                        putModel(page, job);
                    }
                });
    }

    private void createAutoFindJob(Page page) {
        Job oldJob = ((DelayRequest) page.getRequest()).getJob();

        Job newJob = new Job(JP_KID_ANIME);

        DbEntityHelper.derive(oldJob, newJob);
        String code = Md5Util.getMd5(JP_KID_ANIME);
        newJob.setCode(code);
        putModel(page, newJob);
    }

    public void processAutoFind(Page page) {
        Job oldJob = ((DelayRequest) page.getRequest()).getJob();
        String url = oldJob.getUrl();

        JSONObject jsonObject = JSONObject.parseObject(page.getRawText());

        Boolean isOk = jsonObject.getBoolean("success");
        if (!isOk) return;
        JSONArray data = jsonObject.getJSONArray("data");
        if (null == data || data.size() == 0) return;

        List<Job> jobs = new ArrayList<>();//todo 暂时没用

        List<Show> shows = new ArrayList<>();
        String type = UrlUtils.getParam(url, "c");

        for (int i = 0; i < data.size(); i++) {
            JSONObject innerData = data.getJSONObject(i);
            this.saveAutoFindJobAndShow(innerData, oldJob, jobs, shows, Integer.parseInt(type));
        }

        putModel(page, shows);

        String p = UrlUtils.getParam(url, "p");
        int pageNum = Integer.parseInt(p);
        if (pageNum < 6) {
            String pageUrl = StringUtils.replace(url, ("&p=" + p), ("&p=" + (pageNum + 1)));
            Job pageJob = new Job(pageUrl);
            DbEntityHelper.derive(oldJob, pageJob);
            String code = Md5Util.getMd5(pageUrl);
            pageJob.setCode(code);
            putModel(page, pageJob);
        }


    }

    public void saveAutoFindJobAndShow(JSONObject jsonObject, Job old, List<Job> jobs, List<Show> shows, Integer type) {

        String title = jsonObject.getString("title");
        String url = fixUrl(jsonObject.getString("videoLink"));
        String code = "";

        if (title.contains("DVD版") || title.contains("网络版") || title.endsWith("CUT") || title.contains("TV版")) {
            return;
        }

        Show show = new Show();
        if (type == 96) {
            if (old.getUrl().contains("动画")) {
                if (old.getUrl().contains("中国") && !old.getUrl().contains("中国香港") && !old.getUrl().contains("中国台湾")) {
                    show.setCategory(Category.KID_ANIME_MOVIE.name());
                    code = FetchCodeEnum.getCode(url);
                } else {
                    show.setCategory(Category.FOREIGN_KID_ANIME_MOVIE.name());
                    code = FetchCodeEnum.getCode(url);
                }
            } else {
                show.setCategory(Category.NETWORK_MOVIE.name());
                code = FetchCodeEnum.getCode(url);
            }
        } else if (type == 97) {
            show.setCategory(Category.TV_DRAMA.name());
            String tmpCode = FetchCodeEnum.getCode(url + "?source=autoFind");
            if (StringUtils.isNotBlank(tmpCode)) {
                code = "z" + tmpCode;
            }
            url = String.format(DETAIL_URL, code);
        } else if (type == 85) {
            show.setCategory(Category.TV_VARIETY.name());
            String tmpCode = FetchCodeEnum.getCode(url + "?source=autoFind");
            if (StringUtils.isNotBlank(tmpCode)) {
                code = "z" + tmpCode;
            }
            url = String.format(DETAIL_URL, code);
        } else if (type == 177) {
            String category = "";
            if (old.getUrl().contains("中国") && !old.getUrl().contains("中国香港") && !old.getUrl().contains("中国台湾")) {
                category = Category.KID_ANIME.name();
                show.setCategory(category);
            } else {
                category = Category.FOREIGN_KID_ANIME.name();
                show.setCategory(category);
            }
            String vid = "";
            if (url.contains("=")) {
                vid = url.substring(url.indexOf("id_") + 3, url.indexOf("=") + 2);
            } else {
                vid = url.substring(url.indexOf("id_") + 3, url.indexOf(".html"));
            }
            code = vid;
            Job job = new Job(String.format(V_DETAIL_URL, code));
            job.setCode(category + "_autoMark" + vid);
            jobs.add(job);
            return;
        } else if (type == 100) {//动漫
            if ("日本".equals(UrlUtils.getParam(old.getUrl(), "a"))) {
                show.setCategory(Category.JAPAN_ANIME.name());
                code = jsonObject.getString("videoId");
            }

        }

        if (StringUtils.isBlank(code)) {
            throw new PageBeChangedException("The code is null");
        }

        show.setName(title);
        show.setCode(code);
        show.setPlatformId(CommonEnum.Platform.YOU_KU.getCode());
        show.setParentId(0);
        show.setUrl(url);
        show.setSource(3);//3-代表自动发现的剧
        shows.add(show);

        Job newJob = DbEntityHelper.deriveNewJob(old, url);
        newJob.setCode(code);
        jobs.add(newJob);
    }

    private String fixUrl(String href) {
        if (StringUtils.isNotBlank(href) && !href.startsWith("http:")) {
            href = "http:" + href;
        }
        return href;
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