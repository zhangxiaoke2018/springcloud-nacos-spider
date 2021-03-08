package com.jinguduo.spider.spider.bilibili;

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
import com.jinguduo.spider.common.util.Md5Util;
import com.jinguduo.spider.data.table.Comic;
import com.jinguduo.spider.data.table.ComicAuthorRelation;
import com.jinguduo.spider.data.table.ComicBilibili;
import com.jinguduo.spider.data.table.ComicEpisodeInfo;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.model.HttpRequestBody;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by lc on 2019/11/20
 * 漫画任务分别要进行以下步骤：
 * 1、创建全漫画抓取任务
 * 2、抓取漫画列表，生成单漫画任务(如有需要，则保存部分Comic实体信息)
 * 3、保存Comic实体信息、选填 comic_author_relation 信息，
 * 选填comic_bilibili信息/创建comic_bilibili信息任务 选填 comic_episode_info /生成comic_episode_info 任务
 * 4、保存comic_bilibili实体
 * 5、onetube-lol 导入bi及相关计算任务
 */
@Slf4j
@Worker
@SuppressWarnings("all")
public class ComicBilibiliSpider extends CrawlSpider {

    //https://manga.bilibili.com 创建任务入口url
    private static final Integer BILI_PLATFORMID = 24;
    private static final String CODE_PREFIX = "BILI-";

    private static String COMIC_LIST_URL = "https://manga.bilibili.com/twirp/comic.v1.Comic/ClassPage?device=pc&platform=web";
    private static String INIT_COMIC_LIST_BODY = "{\"area_id\":-1, \"is_finish\":-1, \"is_free\":-1, \"order\":0, \"page_num\":%s, \"page_size\":18, \"style_id\":-1 }";

    private static String COMIC_DETAIL_URL = "https://manga.bilibili.com/twirp/comic.v2.Comic/ComicDetail?device=pc&platform=web";
    private static String INIT_COMIC_DETAIL_BODY = "{\"comic_id\":%s }";

    private static String INIT_COMIC_COMMENT_URL = "https://api.bilibili.com/x/v2/reply?type=22&oid=%s";

    //POST请求
    private static String INIT_COMIC_MONTH_TICKETS_URL = "https://manga.bilibili.com/twirp/comic.v1.Comic/GetEntranceForRank?comic_id=%s&type=2";

    private Site site = SiteBuilder.builder()
            .addHeader("referer", "https://manga.bilibili.com")
            .setDomain("manga.bilibili.com")
            .addSpiderListener(new ComicBillibiliDownloaderListener())
            .build();

    private PageRule rules = PageRule.build()
            .add("Comic/ClassPage", page -> getComicList(page))
            .add("Comic/ComicDetail", page -> getComicDetail(page))
            .add("Comic/GetEntranceForRank", page -> getTicket(page))
            .add("manga.bilibili.com$", page -> createComicListJob(page));

    private void getTicket(Page page) {
        Job oldJob = ((DelayRequest) page.getRequest()).getJob();

        JSONObject json = JSONObject.parseObject(page.getRawText());
        if (0 != json.getInteger("code")) return;

        JSONObject data = json.getJSONObject("data");
        JSONObject entrance = data.getJSONObject("entrance");

        Integer month_tickets = entrance.getInteger("month_tickets");
        Integer fans = entrance.getInteger("fans");

        Date today = DateUtil.getDayStartTime(new Date());
        String code = oldJob.getCode();

        ComicBilibili cb = new ComicBilibili(code, today, null);
        cb.setMonthTickets(month_tickets);
        cb.setFans(fans);
        putModel(page, cb);

    }


    private void createComicListJob(Page page) {
        //创建post请求--傻傻的生成150页任务就完事儿了
        Integer maxPage = 150;
        for (int i = 1; i <= maxPage; i++) {
            String body = String.format(INIT_COMIC_LIST_BODY, i);
            Job job = new Job(COMIC_LIST_URL);
            job.setMethod("POST");
            job.setHttpRequestBody(HttpRequestBody.json(body, "utf-8"));
            job.setPlatformId(BILI_PLATFORMID);
            job.setCode(Md5Util.getMd5(body));
            putModel(page, job);
        }
    }

    private void getComicList(Page page) {
        JSONObject json = JSONObject.parseObject(page.getRawText());
        if (0 != json.getInteger("code")) return;

        JSONArray datas = json.getJSONArray("data");

        for (int i = 0; i < datas.size(); i++) {
            JSONObject data = datas.getJSONObject(i);
            String seasonId = data.getString("season_id");
            String code = CODE_PREFIX + seasonId;

            //创建详情页任务
            Job detailJob = new Job(COMIC_DETAIL_URL);
            detailJob.setMethod("POST");
            String detailBody = String.format(INIT_COMIC_DETAIL_BODY, seasonId);
            detailJob.setHttpRequestBody(HttpRequestBody.json(detailBody, "utf-8"));
            detailJob.setCode(code);
            detailJob.setPlatformId(BILI_PLATFORMID);
            putModel(page, detailJob);

            //创建评论页任务
            Job commentJob = new Job(String.format(INIT_COMIC_COMMENT_URL, seasonId));
            commentJob.setCode(code);
            commentJob.setPlatformId(BILI_PLATFORMID);
            putModel(page, commentJob);

            //创建月票任务
            Job ticketJob = new Job(String.format(INIT_COMIC_MONTH_TICKETS_URL, seasonId));
            ticketJob.setCode(code);
            ticketJob.setPlatformId(BILI_PLATFORMID);
            ticketJob.setMethod("POST");
            putModel(page, ticketJob);
        }


    }

    private void getComicDetail(Page page) {
        JSONObject json = JSONObject.parseObject(page.getRawText());
        if (0 != json.getInteger("code")) return;

        JSONObject data = json.getJSONObject("data");

        String seasonId = data.getString("id");
        String code = CODE_PREFIX + seasonId;

        String title = data.getString("title").trim();
        //横图horizontal_cover 竖图vertical_cover 小图square_cover  此链接没有小图
        String headerImg = data.getString("vertical_cover");
        String[] authorNameStrs = data.getObject("author_name", String[].class);
        String[] styles = data.getObject("styles", String[].class);
        String intro = data.getString("evaluate");
        Integer isFinish = data.getInteger("is_finish");
        Integer episode = data.getInteger("total");


        Comic comic = new Comic();
        comic.setCode(code);
        comic.setPlatformId(BILI_PLATFORMID);
        comic.setName(title);
        comic.setHeaderImg(headerImg);
        String tags = StringUtils.join(styles, "/");
        comic.setTags(tags);
        String authorName = StringUtils.join(authorNameStrs, "/");
        comic.setAuthor(authorName);
        comic.setIntro(intro);
        comic.setEpisode(episode);
        comic.setFinished(isFinish == 1 ? true : false);


        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        JSONArray ep_list = data.getJSONArray("ep_list");

        JSONObject endEpisode = ep_list.getJSONObject(0);
        String endUpdateTimeStr = endEpisode.getString("pub_time");
        try {
            Date endUpdateTime = sdf.parse(endUpdateTimeStr);
            comic.setEndEpisodeTime(endUpdateTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        //===========================
        putModel(page, comic);

        for (String authorNameStr : authorNameStrs) {
            ComicAuthorRelation car = new ComicAuthorRelation();
            car.setPlatformId(BILI_PLATFORMID);
            car.setComicCode(code);
            car.setAuthorName(authorNameStr);
            car.setAuthorId(authorNameStr);
            putModel(page, car);
        }

        Date today = DateUtil.getDayStartTime(new Date());
        Integer totalComment = 0;

        for (int i = 0; i < ep_list.size(); i++) {
            JSONObject epInfo = ep_list.getJSONObject(i);
            String epTitle = epInfo.getString("title").trim();
            Integer ord = epInfo.getInteger("ord");
            String pubTimeStr = epInfo.getString("pub_time");
            Date pubTime = null;
            try {
                pubTime = sdf.parse(pubTimeStr);
            } catch (ParseException e) {
                e.printStackTrace();
                continue;
            }

            String epId = epInfo.getString("id");
            Integer epComment = epInfo.getInteger("comments");

            Boolean is_locked = epInfo.getBoolean("is_locked");//true vip   / false not vip

            ComicEpisodeInfo cei = new ComicEpisodeInfo();
            cei.setCode(code);
            cei.setDay(today);
            cei.setPlatformId(BILI_PLATFORMID);
            cei.setName(epTitle);
            cei.setEpisode(ord);
            cei.setVipStatus(is_locked ? 1 : 0);
            cei.setComicCreatedTime(pubTime);
            cei.setChapterId(epId);
            if (null != epComment) {
                totalComment += epComment;
                cei.setComment(epComment);
            }
            putModel(page, cei);

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
