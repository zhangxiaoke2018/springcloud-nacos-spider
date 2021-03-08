package com.jinguduo.spider.spider.manmanmanhua;

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
import com.jinguduo.spider.data.table.Comic;
import com.jinguduo.spider.data.table.ComicAuthorRelation;
import com.jinguduo.spider.data.table.ComicEpisodeInfo;
import com.jinguduo.spider.data.table.ComicMmmh;
import com.jinguduo.spider.webmagic.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * I'm a cute code dog
 * User: LingChen
 * Date:2017/11/7
 * Time:11:27
 */
@Worker
@Slf4j
public class ComicMmmhSpider extends CrawlSpider {

    public final static String API_URL = "https://api.manmanapp.com/v3";

    //漫画详情参数{"worksId":"1403734","limit":"10","api":"works/index","token":"Ag8j+2+7i2BgXCWqqrAPBFVK+ib0PC0gZUs19DczSAEDiqpSDdqzXZhSQCe6R9RJ","info":"5.0.9_OPPO R11_android_android_4.4.2_866174010386528_yingyongbao"}
    public final static String COMIC_DETAIL_PARAM = "{\"worksId\":\"%s\",\"limit\":\"3000\",\"api\":\"works/index\",\"token\":\"manmanDefaultToken\",\"info\":\"5.0.9_OPPO R11_android_android_4.4.2_866174010386528_yingyongbao\"}";

    //漫画分集评论{"quality":1,"api":"comic/detail","token":"Ag8j+2+7i2BgXCWqqrAPBFVK+ib0PC0gZUs19DczSAEDiqpSDdqzXZhSQCe6R9RJ","info":"5.0.9_OPPO R11_android_android_4.4.2_866174010386528_yingyongbao","comic_id":"1405576"}
    public final static String COMIC_EPISODE_DETAIL_PARAM = "{\"quality\":1,\"api\":\"comic/detail\",\"token\":\"manmanDefaultToken\",\"info\":\"5.0.9_OPPO R11_android_android_4.4.2_866174010386528_yingyongbao\",\"comic_id\":\"%s\"}";

    private Site site = SiteBuilder.builder()
            .setDomain("api.manmanapp.com")
            .addSpiderListener(new ComicMmmhDownLoaderListener())
            //  .setAcceptStatCode(Sets.newHashSet(500, 200))
            .build();

    /**
     * 根据类型分发任务
     * 进入的url有如下4种
     * 1、排行榜任务
     * 2、漫画详情（暂时可拿到分集）
     * 3、分集列表
     * 4、分集评论等信息
     * 以url后跟的 ? 为分类标准
     */
    private PageRule rules = PageRule.build()
            .add("rank", page -> analyzeRank(page))//排行榜
            .add("index", page -> analyzeComic(page))
            // .add("works/comic-list", page -> analyzeEpisodeList(page))
            .add("detail", page -> analyzeEpisodeDetail(page));


    private void analyzeRank(Page page) throws UnsupportedEncodingException {
        Job job = ((DelayRequest) page.getRequest()).getJob();

        JSONObject jsonObject = JSONObject.parseObject(page.getRawText());
        if (200 != jsonObject.getInteger("code").intValue()) {
            log.error("api.manmanapp.com fail ,this url is .{}, result is ->{}", job.getUrl(), page.getRawText());
            return;
        }
        JSONArray list = jsonObject.getJSONObject("data").getJSONArray("list");
        //生成漫画详情任务

        List<Job> jobs = new ArrayList<>();

        for (Object comicObj : list) {
            JSONObject comic = (JSONObject) comicObj;
            comic = comic.getJSONObject("works");
            String id = comic.getString("id");
            String format = String.format(COMIC_DETAIL_PARAM, id);
            format = URLEncoder.encode(format, "utf-8");
            Job comicJob = new Job(API_URL + "?" + format);
            comicJob.setCode("mmmh-" + id);
            comicJob.setPlatformId(35);
            jobs.add(comicJob);
        }
        putModel(page, jobs);
    }


    private void analyzeComic(Page page) throws UnsupportedEncodingException {
        Job job = ((DelayRequest) page.getRequest()).getJob();
        //error
        if (page.getRawText().length() < 50) {
            log.error("api.manmanapp.com fail ,this url is ->{},this code is ->{}", job.getUrl(), job.getCode());
            return;
        }
        JSONObject jsonObject = JSONObject.parseObject(page.getRawText());
        if (200 != jsonObject.getInteger("code").intValue()) {
            log.error("api.manmanapp.com fail ,this url is .{}, result is ->{}", job.getUrl(), page.getRawText());
            return;
        }
        JSONObject data = jsonObject.getJSONObject("data");

        String id = data.getString("id");
        String cover_image_url = data.getString("cover_image_url");
        String title = data.getString("title");
        String description = data.getString("description");
        //标签
        Map cates = data.getObject("cate", Map.class);
        String tags = StringUtils.join(cates.values().toArray(), "/");
        Integer reads = data.getInteger("reads");
        String likeStr = data.getString("likes");
        Integer likes = Integer.valueOf(likeStr);
        JSONObject author = data.getJSONObject("author");
        String nickName = author.getString("nickname");
        String authorId = author.getString("id");
        JSONArray comics = data.getJSONArray("comics");

        String code = "mmmh-" + id;
        Date day = DateUtil.getDayStartTime(new Date());

        Comic comic = new Comic();
        comic.setCode(code);
        comic.setPlatformId(35);
        comic.setName(title);
        comic.setHeaderImg(cover_image_url);
        comic.setAuthor(nickName);
        comic.setTags(tags);
        comic.setIntro(description);
        comic.setEpisode(comics.size());
        putModel(page, comic);
        ComicAuthorRelation car = new ComicAuthorRelation();
        car.setPlatformId(35);
        car.setAuthorId(authorId);
        car.setComicCode(code);
        car.setAuthorName(nickName);
        putModel(page, car);

        ComicMmmh mmmh = new ComicMmmh();
        mmmh.setCode(code);
        mmmh.setDay(day);
        mmmh.setReadsNum(reads);
        mmmh.setLikesNum(likes);
        putModel(page, mmmh);

        //  List<Job> comicCommentJob = new ArrayList<>();
        List<ComicEpisodeInfo> episodeInfoList = new ArrayList<>();

        for (Object comicObj : comics) {
            JSONObject comicEpi = (JSONObject) comicObj;
            String idEpi = comicEpi.getString("id");
            String titleEpi = comicEpi.getString("title");
            Integer words_numEpi = comicEpi.getInteger("words_num");
            Long publish_timeEpi = comicEpi.getLong("publish_time");
            Integer likesEpi = comicEpi.getInteger("likes");
            Integer is_readEpi = comicEpi.getInteger("is_read");
            Integer vipStatus = is_readEpi == 1 ? 0 : 1;

            ComicEpisodeInfo info = new ComicEpisodeInfo();
            info.setCode(code);
            info.setDay(day);
            info.setPlatformId(35);
            info.setName(titleEpi);
            info.setEpisode(words_numEpi);
            info.setVipStatus(vipStatus);
            info.setComicCreatedTime(new Date(publish_timeEpi * 1000));
            info.setChapterId(idEpi);
            info.setLikeCount(likesEpi);
            episodeInfoList.add(info);


            //TODO 由于对面网站接口问题，分集评论抓取差异过大，暂时取消
//            String format = String.format(COMIC_EPISODE_DETAIL_PARAM, idEpi);
//            format = URLEncoder.encode(format, "utf-8");
//            Job comicJob = new Job(API_URL + "?" + format);
//            comicJob.setCode(code);
//            comicJob.setPlatformId(35);
//            comicCommentJob.add(comicJob);
        }
        //  putModel(page, comicCommentJob);
        putModel(page, episodeInfoList);
    }


    private void analyzeEpisodeDetail(Page page) {
        Job job = ((DelayRequest) page.getRequest()).getJob();
        //error
        if (page.getRawText().length() < 50) {
            log.error("api.manmanapp.com fail ,this url is .{}, result is ->{}", job.getUrl(), page.getRawText());
            return;
        }
        JSONObject jsonObject = JSONObject.parseObject(page.getRawText());
        if (200 != jsonObject.getInteger("code").intValue()) {
            log.error("api.manmanapp.com fail ,this url is .{}, result is ->{}", job.getUrl(), page.getRawText());
            return;
        }
        String code = job.getCode();
        Date day = DateUtil.getDayStartTime(new Date());


        JSONObject data = jsonObject.getJSONObject("data");

        String episodeId = data.getString("id");
        Integer episode = data.getInteger("words_num");
        Integer comments = data.getInteger("comments");
        Integer likes = data.getInteger("likes");
        ComicEpisodeInfo info = new ComicEpisodeInfo();
        info.setCode(code);
        info.setDay(day);
        info.setPlatformId(35);
        info.setEpisode(episode);
        info.setChapterId(episodeId);
        info.setLikeCount(likes);
        info.setComment(comments);
        putModel(page, info);
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
