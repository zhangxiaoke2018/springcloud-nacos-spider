package com.jinguduo.spider.spider.tengxun;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.spider.listener.UserAgentSpiderListener;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.code.FetchCodeEnum;
import com.jinguduo.spider.common.constant.CommonEnum;
import com.jinguduo.spider.common.constant.CommonEnum.BannerType;
import com.jinguduo.spider.common.constant.CommonEnum.Platform;
import com.jinguduo.spider.common.constant.FrequencyConstant;
import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.common.util.Md5Util;
import com.jinguduo.spider.common.util.NumberHelper;
import com.jinguduo.spider.common.util.RegexUtil;
import com.jinguduo.spider.data.table.AutoFindLogs;
import com.jinguduo.spider.data.table.BannerRecommendation;
import com.jinguduo.spider.data.table.Category;
import com.jinguduo.spider.data.table.Show;
import com.jinguduo.spider.data.table.ShowLog;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.selector.Html;
import com.jinguduo.spider.webmagic.selector.Selectable;
import com.jinguduo.spider.webmagic.utils.UrlUtils;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.config.CookieSpecs;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @DATE 16/6/24 上午11:21
 */
@Worker
@CommonsLog
public class TengxunUrlSpider extends CrawlSpider {

    private Site site = SiteBuilder.builder()
            .setDomain("v.qq.com")
            .setCookieSpecs(CookieSpecs.IGNORE_COOKIES)
            // user-agent 动态化
            .addSpiderListener(new UserAgentSpiderListener())
            .build();

    /**
     * 剧以及综艺规范入口链接
     */
    private final static String DETAIL_URL_PRE = "http://v.qq.com/detail/%s/%s.html";

    // 总播放量
    private final static String TOTAL_PLAY_COUNT = "http://data.video.qq.com/fcgi-bin/data?tid=70&&appid=10001007&appkey=e075742beb866145&callback=jQuery19109213305850191142_1468217242170&low_login=1&idlist=%s&otype=json&_=1468217242171";

//    private final static String ONE_EPISODE = "http://sns.video.qq.com/tvideo/fcgi-bin/batchgetplaymount?id=%s&otype=json";

    // 获取评论Id api
    private final static String COMMENT_CID_URL = "http://ncgi.video.qq.com/fcgi-bin/video_comment_id?otype=json&op=%s&cid=%s";

    // 获取评论Id api
    private final static String COMMENT_VID_URL = "http://ncgi.video.qq.com/fcgi-bin/video_comment_id?otype=json&op=%s&vid=%s";

    // 弹幕targetid
    private final static String DANMU_TARGETID_URL = "http://bullet.video.qq.com/fcgi-bin/target/regist?vid=%s&cid=%s";

    private final static String SHOW_LIST_URL = "http://s.video.qq.com/loadplaylist?callback=jQuery19102759763043414536_1465884369999&low_login=1&type=6&id=%s&plname=qq&vtype=3&video_type=10&inorder=1&otype=json&_=1567758140000";

    private final static String PLAYCOUNT_URL_ONE = "https://data.video.qq.com/fcgi-bin/data?tid=376&&appid=20001212&appkey=b4789ed0ec69d23a&otype=json&&callback=jQuery19106671459599489511_1484404891342&idlist=%s";

    private final static String EA_KID_ANIME = "https://v.qq.com/x/list/child?iarea=1&offset=%s";

    private final static String JK_KID_ANIME = "https://v.qq.com/x/list/child?iarea=2&offset=%s";

    private final static String CN_KID_ANIME = "https://v.qq.com/x/list/child?iarea=3&offset=%s";
    
    private final static String WEB_CHANNEL_BANNER_DRAMA = "https://v.qq.com/channel/tv#WEB_CHANNEL_BANNER";
    
    private final static String MOBILE_HOME_BANNER = "https://m.v.qq.com/index.html#MOBILE_HOME_BANNER";
    
    private final static String MOBILE_CHANNEL_BANNER = "https://m.v.qq.com/x/m/channel/figure/tv#MOBILE_CHANNEL_BANNER";
    /**
     * 自定义变量
     */
    private final int op = 3;// 不知道是什么值，可能是登陆校验，取值，1,2,3均可

    // 特殊剧，暂时特殊处理，规律后再封装逻辑
    // 网络剧:恶魔少爷别吻我2 code:bg8lt16giaj9vyu
    private static final Set<String> SPECIFIC = Sets.newHashSet("bg8lt16giaj9vyu");

    PageRule rule = PageRule.build()
            .add("/x/list/", page -> find(page))// 自动发现剧
            .add("/tv/", page -> findByBanner(page))// 自动发现banner
            .add("/detail/", page -> specialPageProcess(page))// 详情页处理
            .add("/x/cover/", page -> processNetMovie(page))
            .add("/x/bu/pagesheet/list", page -> cartoonAutoFind(page))//日漫自动发现任务
            .add("variety", page -> processVarietyTPC(page))  // 网综页的总播放量 //// http://v.qq.com/variety/column/column_21953.html
            .add("#WEB_HOME_BANNER", this::processBanner)
            .add("/channel/tv#WEB_CHANNEL_BANNER", this::processBanner);

    private static Map<String, Category> TYPE_MAP = new HashMap<String, Category>() {{
        put("cartoon&2", Category.JAPAN_ANIME);

    }};

    //https://v.qq.com/x/bu/pagesheet/list?append=1&channel=cartoon&iarea=2&listpage=2&offset=30&pagesize=30
    private void cartoonAutoFind(Page page) {
        Job job = ((DelayRequest) page.getRequest()).getJob();
        String url = job.getUrl();

        Map<String, String> allParam = UrlUtils.getAllParam(url);
        String channel = allParam.get("channel");//cartoon
        String iarea = allParam.get("iarea");//2->日本
        String offset = allParam.get("offset");//2->页面偏移量
        String pagesize = allParam.get("pagesize");//2->pagesize
        List<Show> shows = new ArrayList<>();

        Category category = TYPE_MAP.get(channel + "&" + iarea);
        if (null == category) return;

        Document document = page.getHtml().getDocument();

        Elements items = document.getElementsByClass("figure_detail figure_detail_two_row");

        for (Element item : items) {
            Element a = item.getElementsByTag("a").first();
            if (null != a) {
                String showUrl = a.attr("href");
                String title = a.attr("title");
                String code = FetchCodeEnum.getCode(showUrl + "?source=autoFind");
                this.saveAutoFind(title, showUrl, code, job, category, shows);
            }
        }

        putModel(page, shows);

        //next page
        String offPrefix = "&offset=";
        int oldPage = Integer.parseInt(offset);
        if (oldPage < 500) {
            String pageUrl = StringUtils.replace(url, (offPrefix + offset), (offPrefix + (oldPage + Integer.parseInt(pagesize))));
            Job pageJob = new Job(pageUrl);
            DbEntityHelper.derive(job, pageJob);
            String code = Md5Util.getMd5(pageUrl);
            pageJob.setCode(code);
            putModel(page, pageJob);
        }
    }

    /**
     * 保存
     */
    private void saveAutoFind(String title, String url, String code, Job old, Category category, List<Show> shows) {

        if (title.contains("DVD版") || title.contains("网络版") || title.endsWith("CUT")) {
            return;
        }

        if (StringUtils.isBlank(code)) {
            return;
        }

        Show show = new Show(title, code, CommonEnum.Platform.TENG_XUN.getCode(), 0);
        if (url.indexOf("?") > 0) {
            url = url.substring(0, url.indexOf("?"));  // 带参数的页面模板不一样
        }
        show.setUrl(url);
        show.setSource(3);//3-代表自动发现的剧
        show.setCategory(category.name());
        //Job
        //  Job newJob = DbEntityHelper.deriveNewJob(old, url);
        //  newJob.setCode(code);
        //  newJob.setFrequency(FrequencyConstant.GENERAL_SHOW_INFO);
        //  jobs.add(newJob);

        shows.add(show);
    }

    /**
     * 代码从TengXunFilmSpider类拷贝而来 http://v.qq.com/x/cover/b3pp0bf8q1hr813.html
     *
     * @param page
     */
    private void processNetMovie(Page page) {
        Job job = ((DelayRequest) page.getRequest()).getJob();
        /** 自定义变量 */
        String url = page.getUrl().get();

        // (特殊处理)https://v.qq.com/x/cover/uwy95kwz21l189p/a0023df9mx1.html这种类型的电影实际获取commentId应该是uwy95kwz21l189p
        String ids = RegexUtil.getDataByRegex(url, "cover/(.*).html");
        String code = job.getCode();
        List<Job> jobList = Lists.newArrayList();

        // 从页面上获取当前vid
        String vid = page.getHtml().xpath("//link[@rel=\"canonical\"").regex("x/cover/.*/(.*)\\.html", 1).get();
        String coverId = page.getHtml().xpath("//link[@rel=\"canonical\"").regex("x/cover/(.*)/.*\\.html", 1).get();

        if (StringUtils.isNotBlank(vid)) {
//			Job job1 = new Job(String.format(PLAYCOUNT_URL_ONE, vid));
            // 使用父级id获取播放量，包含片花
            Job job1 = new Job(String.format(TOTAL_PLAY_COUNT, coverId));
            DbEntityHelper.derive(job, job1);
            job1.setCode(job.getCode());
            jobList.add(job1);
        }

        // commentId job
        // TODO 备用判断(若该判断还是有错，可以再加一条判断，综艺的id长度一般为15，剧的id一般为11. 没办法的办法再用)
        Job commentIdJob = null;
        if (ids.split("/").length > 1) {
            commentIdJob = new Job(String.format(COMMENT_CID_URL, op, ids.split("/")[0]));
        } else {
            if (code.length() == 15) {
                commentIdJob = new Job(String.format(COMMENT_CID_URL, op, code));
            } else if (code.length() == 11) {
                // 例如动漫之类页面的code其实是vid，平成上列的vid api url会异常
                commentIdJob = new Job(String.format(COMMENT_VID_URL, op, code));
            }
        }
        if (commentIdJob != null) {
            DbEntityHelper.derive(job, commentIdJob);
            commentIdJob.setCode(code);
            commentIdJob.setFrequency(FrequencyConstant.COMMENT_BEFOR_PROCESS);

            jobList.add(commentIdJob);
        }

        // 彈幕targetID任務
        Html html = page.getHtml();
        String realUrl = html.xpath("//link[@rel='canonical']/@href").toString();
        String cid_vid = RegexUtil.getDataByRegex(realUrl, "cover/(.*).html");
        if (cid_vid != null) {
            String[] strs = cid_vid.split("/");
            String cid = strs[0];
            String vvid = strs[1];
            String targetUrl = String.format(DANMU_TARGETID_URL, vvid, cid);
            Job danmuTargetJob = new Job(targetUrl);
            DbEntityHelper.derive(job, danmuTargetJob);
            danmuTargetJob.setCode(job.getCode());

            jobList.add(danmuTargetJob);
        }

        if (!jobList.isEmpty()) {
            putModel(page, jobList);
        }
    }

    /***
     * 播放页 1.生成剧集listJob
     *
     * @param page
     */
    public void specialPageProcess(Page page) {

        Job oldJob = ((DelayRequest) page.getRequest()).getJob();
        String jobUrl = oldJob.getUrl();
        String id = jobUrl.substring(jobUrl.lastIndexOf("/") + 1, jobUrl.lastIndexOf("."));


        if (!SPECIFIC.contains(oldJob.getCode())) {
            Job newJob = new Job(String.format(SHOW_LIST_URL, id));
            DbEntityHelper.derive(oldJob, newJob);
            putModel(page, newJob);
            // 专辑总播量任务
//			putModel(page, this.totalPlayCount(id, page));
        } else {
            Html html = page.getHtml();
            Integer platformId = oldJob.getPlatformId();
            Integer parentId = oldJob.getShowId();
            String title = html.xpath("//h1[@class='video_title_cn']/a/text()").get();
            String pid = page.getUrl().regex("\\/\\w\\/(.*?)\\.html", 1).get();
            // 专辑总播量任务
            putModel(page, this.totalPlayCount(pid, page));

            html.xpath("//div[@class='mod_episode']/span//a").nodes().stream()
                    .filter(a -> !a.xpath("//span[@class=\"mark_v\"]/img[@alt=\"预告\"]").match()).forEach(a -> {
                try {
                    Integer episode = Integer.valueOf(
                            a.xpath("//span[@itemprop=\"episodeNumber\"]/text()").regex("([0-9]+)", 1).get());
                    String childUrl = a.xpath("///@href").get();
                    String vid = a.xpath("///@href").regex("\\/cover\\/.*?\\/(.*?)\\.html", 1).get();
                    if (StringUtils.isBlank(vid)) {
                        return;
                    }

                    // show item
                    Show show = new Show(title + "_" + episode, vid, platformId, parentId);
                    show.setDepth(2);
                    show.setEpisode(episode);
                    show.setParentCode(oldJob.getCode());
                    show.setUrl(childUrl);
                    putModel(page, show);

                    // commentId job
                    Job jobComment = DbEntityHelper.derive(oldJob, new Job());
                    jobComment.setUrl(String.format(COMMENT_VID_URL, op, vid));
                    jobComment.setCode(show.getCode());
                    putModel(page, jobComment);

                    // danmuId job
                    Job danmuTargetJob = new Job(String.format(DANMU_TARGETID_URL, vid, pid));
                    DbEntityHelper.derive(oldJob, danmuTargetJob);
                    danmuTargetJob.setCode(vid);
                    putModel(page, danmuTargetJob);

//                    // child show playcount job
//                    Job job = new Job();
//                    DbEntityHelper.derive(oldJob, job);
//                    job.setCode(vid);
//                    job.setUrl(String.format(ONE_EPISODE, vid));
//                    putModel(page, job);

                } catch (NumberFormatException e) {
                    log.error(e.getMessage(), e);
                }
            });
        }
    }

    /***
     * 自动发现剧 http://v.qq.com/x/list/movie?year=2017&offset=0&sort=19 最新电影
     * http://v.qq.com/x/list/tv?sort=19&iyear=2017&offset=0&iarea=814 最新剧
     * http://v.qq.com/x/list/variety?offset=0&sort=4&iyear=2017&iarea=-1 热播综艺
     */
    private void find(Page page) {

        Html html = page.getHtml();
        // list div
        List<Selectable> lists = html.xpath("//ul[@class='figures_list']/li").nodes();

        if (CollectionUtils.isEmpty(lists)) {
            log.error("download page no any result!");
            return;
        }

        Job job = ((DelayRequest) page.getRequest()).getJob();
        String jobUrl = job.getUrl();
        String type = RegexUtil.getDataByRegex(jobUrl, "/x/list/(\\S+)\\?", 1);


        List<Show> shows = Lists.newArrayList();
        List<Job> jobs = Lists.newArrayList();
        List<AutoFindLogs> findLogs = Lists.newArrayList();

        lists.stream().forEach(li -> save(li, job, shows, findLogs, jobs, type));

        if (CollectionUtils.isNotEmpty(shows)) {
            putModel(page, shows);
        }
        if (CollectionUtils.isNotEmpty(findLogs)) {
            putModel(page, findLogs);
        }
        if (CollectionUtils.isNotEmpty(jobs)) {
            putModel(page, jobs);
        }
    }

    /**
     * 保存
     */
    private void save(Selectable li, Job old, List<Show> shows, List<AutoFindLogs> findLogs, List<Job> jobs,
                      String type) {
        Selectable a = li.xpath("strong/a");

        // 基础数据a
        String title = a.xpath("a/@title").get();
        String url = a.xpath("a/@href").get();
        String code = li.xpath("a/@data-float").get();

        if (title.contains("DVD版") || title.contains("网络版") || title.endsWith("CUT")) {
            return;
        }

        // show
        Show show = new Show();
        if (StringUtils.equals(type, "movie") || StringUtils.equals(type, "dv")) {
            show.setCategory(Category.NETWORK_MOVIE.name());
        } else if (StringUtils.equals(type, "tv")) {
            show.setCategory(Category.TV_DRAMA.name());
            url = String.format(DETAIL_URL_PRE, code.charAt(0), code);
            findLogs.add(new AutoFindLogs(title, Category.TV_DRAMA.name(), CommonEnum.Platform.TENG_XUN.getCode(), url,
                    code));
        } else if (StringUtils.equals(type, "variety")) {
            show.setCategory(Category.TV_VARIETY.name());
            String tmpCode = FetchCodeEnum.getCode(url + "&source=autoFind");
            if (StringUtils.isNotBlank(tmpCode)) {
                url = String.format(DETAIL_URL_PRE, tmpCode.charAt(0), tmpCode);
                code = tmpCode;
            } else {
                return;
            }
        } else if (StringUtils.equals(type, "child")) {
            if (old.getUrl().contains("iarea=3")) {
                show.setCategory(Category.KID_ANIME.name());
            } else if (old.getUrl().contains("iarea=1") || old.getUrl().contains("iarea=2")) {
                show.setCategory(Category.FOREIGN_KID_ANIME.name());
            }
        }
        if (StringUtils.isBlank(code)) {
            return;
        }

        show.setName(title);
        show.setCode(code);
        show.setPlatformId(CommonEnum.Platform.TENG_XUN.getCode());
        show.setParentId(0);
        show.setUrl(url);
        show.setSource(3);// 3-代表自动发现的剧

        // Job
        Job newJob = DbEntityHelper.deriveNewJob(old, url);
        newJob.setCode(code);

        shows.add(show);
        jobs.add(newJob);
    }

    // 无法判断是电视剧还是网剧
    @Deprecated
    private void findByBanner(Page page) {

        Html html = page.getHtml();
        // list div
        List<Selectable> lists = html.xpath("//div[@class='slider_nav']/a").nodes();

        if (CollectionUtils.isEmpty(lists)) {
            log.error("download page no any result!");
            return;
        }

        Job job = ((DelayRequest) page.getRequest()).getJob();

        List<Show> shows = Lists.newArrayList();
        List<Job> jobs = Lists.newArrayList();
        List<AutoFindLogs> findLogs = Lists.newArrayList();

        lists.stream().forEach(li -> saveBannerDrama(li, job, shows, findLogs, jobs, page));

        if (CollectionUtils.isNotEmpty(shows)) {
            putModel(page, shows);
        }
        if (CollectionUtils.isNotEmpty(findLogs)) {
            putModel(page, findLogs);
        }
        if (CollectionUtils.isNotEmpty(jobs)) {
            putModel(page, jobs);
        }
    }

    private void saveBannerDrama(Selectable a, Job old, List<Show> shows, List<AutoFindLogs> findLogs, List<Job> jobs, Page page) {
        // 基础数据a
        String url = a.xpath("a/@href").get();
        Pattern p = Pattern.compile("x/cover/\\S*\\.html");
        Matcher m = p.matcher(url);
        while (m.find()) {

            String title = a.xpath("a/text()").get();
            int subTitle = title.indexOf("：");
            if (subTitle < 0) {
                subTitle = title.indexOf(":");
            }

            title = title.substring(0, subTitle);

            String detailUrl = "http://v.qq.com/detail/%s/%s.html";

            String code = FetchCodeEnum.getCode(url);

            if (StringUtils.isBlank(code)) {
                return;
            }
            detailUrl = String.format(detailUrl, code.charAt(0), code);

            // show
            Show show = new Show(title, code, CommonEnum.Platform.TENG_XUN.getCode(),
                    0);
            show.setCategory(Category.TV_DRAMA.name());
            show.setUrl(detailUrl);
            show.setSource(3);// 3-代表自动发现的剧
            findLogs.add(new AutoFindLogs(title, Category.TV_DRAMA.name(), CommonEnum.Platform.TENG_XUN.getCode(), detailUrl,
                    code));

            // Job
            Job newJob = DbEntityHelper.deriveNewJob(old, detailUrl);

            newJob.setCode(code);
            shows.add(show);
            jobs.add(newJob);
            return;
        }
    }

    /***
     * 特殊综艺专辑播放量获取
     *
     * @param page
     */
    private void processVarietyTPC(Page page) {
        Job oldJob = ((DelayRequest) page.getRequest()).getJob();

        Long totalPlayCount = NumberHelper.bruteParse(page.getHtml().xpath("//span[@class='num']/text()").get(), -1L);

        ShowLog showLog = new ShowLog();
        DbEntityHelper.derive(oldJob, showLog);
        showLog.setPlayCount(totalPlayCount);
        putModel(page, showLog);

    }

    private Job totalPlayCount(String id, Page page) {

        Job oldJob = ((DelayRequest) page.getRequest()).getJob();
        if (oldJob != null) {
            Job job = new Job();
            DbEntityHelper.derive(oldJob, job);
            job.setCode(oldJob.getCode());
            job.setUrl(String.format(TOTAL_PLAY_COUNT, id));
            return job;
        }
        return null;
    }
    
    private void processBanner(Page page) {
    	BannerType bannerType = BannerType.valueOf(page.getUrl().regex(".*#(WEB_HOME_BANNER|WEB_CHANNEL_BANNER)$").get());
    	List<String> bannerLinks = page.getHtml().xpath("//div[@class='slider_nav _quicklink slider_nav_watched']//a/@href").all();
        if(bannerLinks!=null) {
        	bannerLinks.forEach(l->{
        		String albumId = RegexUtil.getDataByRegex(l, "\\/x\\/cover\\/([a-z0-9]{15})\\.html");
        		if(null!=albumId) {
        			BannerRecommendation br = new BannerRecommendation(albumId,Platform.TENG_XUN.getCode(),bannerType);
        			putModel(page,br);
        		}
        	});
        }
        
        if(bannerType==BannerType.WEB_HOME_BANNER) {
        	createUrlJob(page, WEB_CHANNEL_BANNER_DRAMA);
        	createUrlJob(page, MOBILE_HOME_BANNER);
        	createUrlJob(page, MOBILE_CHANNEL_BANNER);
        }
    }
    
    private void createUrlJob(Page page,String url) {
    	Job j = new Job(url);
    	j.setCode(Md5Util.getMd5(url));
    	j.setPlatformId(Platform.TENG_XUN.getCode());
    	putModel(page,j);
    }

    @Override
    public PageRule getPageRule() {
        return rule;
    }

    @Override
    public Site getSite() {
        return this.site;
    }
}
