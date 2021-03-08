package com.jinguduo.spider.spider.kanmanhua;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.util.DateUtil;
import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.data.table.Comic;
import com.jinguduo.spider.data.table.ComicEpisodeInfo;
import com.jinguduo.spider.data.table.ComicKanmanhua;
import com.jinguduo.spider.webmagic.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * I'm a cute code dog
 * User: LingChen
 * Date:2018/1/25
 * Time:15:25
 */
@Slf4j
@Worker
public class KanmanhuaSpider extends CrawlSpider {
    /**
     * baseUrl = http://getconfig-globalapi.yyhao.com/app_api/v5/rankinglist/?platformname=android&productname=kmh
     * supportUrl = http://getconfig-globalapi.yyhao.com/app_api/v5/getcomicinfo_support/?comic_id=%s&give=1&platformname=android&productname=kmh
     * bodyUrl = http://getconfig-globalapi.yyhao.com/app_api/v5/getcomicinfo_body/?comic_id=&s&platformname=android&productname=kmh
     * commentUrl = http://changyan.sohu.com/api/2/topic/load?topic_source_id=comic%s&client_id=cysGR3Ozm&topic_url=
     */

    /**
     * new baseUrl = http://rankdata-globalapi.321mh.com/app_api/v1/comic/getRankDataDetials/?sort_type=all&product_id=1&rank_type=heat&time_type=%s&query_time=%s&platformname=android&productname=kmh
     */

    private static final String IMAGE_URL = "http://image.samanlehua.com/mh/%s.jpg";

    private static final String KANMANHUA_NEW_BASE_URL = "http://rankdata-globalapi.321mh.com/app_api/v1/comic/getRankDataDetials/?sort_type=%s&product_id=0&rank_type=%s&time_type=%s&query_time=%s&platformname=android&productname=kmh";

    /**
     * post_body  -> userauth=VYCU5WafEplIQNqtpIytxJkaH3KQPesh2Xx8zl9gFiSJbZ%2BKxlQG1OcQCMwPDKgcj75FkeShsnSw3eRwhOVifV1gpx%2F7ZJ7EKZHT%2BUwSA%2FbpCme3QRX%2Bhc%2ByjY1erxSHrDGL%2B2Ymb%2BOrW15r8qXgOiO1Twx2HC1ilPJDKn4%2Bvlo%3D
     * url  -> http://kanmanapi-main.321mh.com/v1/comic/getchapterlikelistbycomic?comic_id=9680
     */
    private static final String EPISODE_LIKES_URL = "http://kanmanapi-main.321mh.com/v1/comic/getchapterlikelistbycomic?comic_id=%s";

    private static final String TYPE_WEEK = "week";
    private static final String TYPE_DAY = "day";
    private static final String TYPE_MONTH = "month";
    private static final String TYPE_TOTAL = "total";

    //榜单类型
    public static final Map<String, String> XIAOMINGTAIJI_RANK_TYPE_MAP = new HashMap<String, String>() {{
        put("heat", "人气");
//        put("pv", "阅读");
//        put("uv", "日活");
//        put("collect_num", "收藏");
//        put("share_times", "分享");
//        put("comment_num", "留言");
//        put("reward_gold", "赞赏");
//        put("gift_coin", "礼物");
//        put("ticket_num", "月票");
//        put("recommend_num", "推荐");
//        put("score_num", "评分");
//        put("clock_times", "打卡");
    }};
    //排序类型
    public static final Map<String, String> XIAOMINGTAIJI_SORT_TYPE_MAP = new HashMap<String, String>() {{
        put("all", "综合榜");
        put("self", "自制榜");
        put("new", "新作榜");
        put("dark", "黑马榜");
        put("free", "免费榜");
        put("boy", "少年榜");
        put("girl", "少女榜");
        put("serialize", "连载榜");
        put("finish", "完结榜");
        put("charge", "付费榜");
    }};
    public static final Map<String, String> XIAOMINGTAIJI_TIME_TYPE_MAP = new HashMap<String, String>() {{
        put("total", "总榜");
        put("day", "日榜");
        put("week", "周榜");
        put("month", "月榜");
    }};

    private Site site = SiteBuilder.builder()
            .setDomain("getconfig-globalapi.yyhao.com")
            .build();

    private PageRule rules = PageRule.build()
            .add("/rankinglist/", page -> createNewBaseTask(page))
//            .add("/getcomicinfo_support/", page -> getSupport(page))
            .add("/getcomicinfo_body/", page -> getBody(page));


    /**
     * http://rankdata-globalapi.321mh.com/app_api/v1/comic/getRankDataDetials/?
     * sort_type=%s
     * &product_id=0
     * &rank_type=%s
     * &time_type=%s
     * &query_time=%s
     * &platformname=android
     * &productname=kmh
     */
    private void createNewBaseTask(Page page) {
        Job job = ((DelayRequest) page.getRequest()).getJob();
        //获取昨天
        String yesterday = DateUtil.getYesterday();
        List<Job> jobs = new ArrayList<>();
        //任务极限 480个！！！
        for (String sortType : XIAOMINGTAIJI_SORT_TYPE_MAP.keySet()) {
            for (String rankType : XIAOMINGTAIJI_RANK_TYPE_MAP.keySet()) {
                for (String timeType : XIAOMINGTAIJI_TIME_TYPE_MAP.keySet()) {
                    String jobUrl = String.format(KANMANHUA_NEW_BASE_URL, sortType, rankType, timeType, yesterday);
                    Job newJob = new Job(jobUrl);
                    newJob.setCode(sortType + "/" + rankType + "/" + timeType);
                    newJob.setPlatformId(36);
                    jobs.add(newJob);
                }
            }
        }
        putModel(page, jobs);
    }

    /**
     * 解析信息页
     */
    private void getBody(Page page) {
        Job job = ((DelayRequest) page.getRequest()).getJob();

        //返回数据为json格式转换为map
        JSONObject json = JSONObject.parseObject(page.getJson().get());
        String comic_name = json.getString("comic_name");
        JSONObject comic_type = json.getJSONObject("comic_type");
        List<String> comicType = new ArrayList<>();
        if (null != comic_type) {
            for (String key : comic_type.keySet()) {
                String value = comic_type.getString(key);
                comicType.add(value);
            }
        }
        String typeStr = StringUtils.join(comicType, "/");
        Integer comic_status = json.getInteger("comic_status");
        Boolean finished = comic_status == 2 ? true : false;

        String comic_author = json.getString("comic_author");

        String comic_desc = json.getString("comic_desc");

        Integer shoucang = json.getInteger("shoucang");
        Long renqi = json.getLong("renqi");
        JSONArray episodes = json.getJSONArray("comic_chapter");
        Integer episode = episodes.size();
        List<ComicEpisodeInfo> episodeList = new ArrayList();
        Date day = DateUtil.getDayStartTime(new Date());
        int j = 1;
        for (int i = episode - 1; i >= 0; i--, j++) {
            JSONObject episodeComic = (JSONObject) episodes.get(i);
            String title = episodeComic.getString("chapter_name");
            Integer isbuy = episodeComic.getInteger("isbuy");
            Integer vipStatus = null == isbuy || isbuy != 1 ? 0 : 1;
            Date create_date = new Date(episodeComic.getLong("create_date") * 1000);
            String chapter_topic_id = episodeComic.getString("chapter_topic_id");
            ComicEpisodeInfo info = new ComicEpisodeInfo();
            info.setCode(job.getCode());
            info.setPlatformId(36);
            info.setDay(day);
            info.setName(title);
            info.setEpisode(j);
            info.setVipStatus(vipStatus);
            info.setComicCreatedTime(create_date);
            info.setChapterId(chapter_topic_id);
            episodeList.add(info);
        }


        Long update_time = json.getLong("update_time");
        Date endTime = new Date(update_time * 1000);

        String code = job.getCode();
        String comic_id = StringUtils.replace(code, "kan-", "");
        Comic comic = new Comic();
        comic.setCode(job.getCode());
        comic.setPlatformId(36);
        comic.setName(comic_name);
        comic.setHeaderImg(String.format(IMAGE_URL, comic_id));
        //换到新的接口中
        //comic.setAuthor(comic_author);
        comic.setTags(typeStr);
        comic.setIntro(comic_desc);
        comic.setFinished(finished);
        comic.setEpisode(episode);
        comic.setEndEpisodeTime(endTime);
        putModel(page, comic);

        ComicKanmanhua kan = new ComicKanmanhua();
        kan.setCode(code);
        kan.setDay(DateUtil.getDayStartTime(new Date()));
        kan.setShoucang(shoucang);
        kan.setRenqi(renqi);
        putModel(page, kan);
        putModel(page, episodeList);

        String comicId = StringUtils.replace(job.getCode(), "kan-", "");

        String episodeLikesUrl = String.format(EPISODE_LIKES_URL, comicId);

        Job epLikesJob = new Job(episodeLikesUrl);
        DbEntityHelper.derive(job, epLikesJob);
        epLikesJob.setCode(job.getCode());
        putModel(page, epLikesJob);

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
