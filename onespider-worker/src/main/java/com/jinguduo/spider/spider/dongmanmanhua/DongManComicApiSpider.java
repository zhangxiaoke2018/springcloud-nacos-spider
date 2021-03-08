package com.jinguduo.spider.spider.dongmanmanhua;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.util.DateUtil;
import com.jinguduo.spider.data.table.*;
import com.jinguduo.spider.webmagic.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Slf4j
@Worker
public class DongManComicApiSpider extends CrawlSpider {

    private Site site = SiteBuilder.builder()
            .setDomain("apis.dongmanmanhua.cn")
            .build();

    private String commentUrl = "https://apis.dongmanmanhua.cn/v2/comment?titleNo=%s&episodeNo=%s";

    //静态，省的每次都生成
    private static final List<Integer> EPISODE_INIT_LIST = new ArrayList() {{
        for (int i = 1; i <= 5000; i++) {
            add(i);
        }
    }};

    //分集点赞数,每次分集出1000条,再多就出错了
    //https://www.dongmanmanhua.cn/v1/title/1189/episode/likeAndCount?episodeNos=1,2,3,4,5
    private String EPISODE_LIKES_URL = "https://www.dongmanmanhua.cn/v1/title/%s/episode/likeAndCount?episodeNos=%s";

    private PageRule rules = PageRule.build()
            .add("info2", page -> info(page))
            .add("comment", page -> comment(page))
            .add("ranking2", page -> billboard(page))
            .add("/episode/list/", page -> episode(page));

    private void billboard(Page page) {
        Job job = ((DelayRequest) page.getRequest()).getJob();
        JSONObject jsonObject = page.getJson().toObject(JSONObject.class);

        JSONObject message = jsonObject.getJSONObject("message");
        JSONObject result = message.getJSONObject("result");
        Date day = DateUtil.getDayStartTime(new Date());
        Integer platformId = 52;

        List<ComicOriginalBillboard> list = new ArrayList();
        for (String rankType : result.keySet()) {
            JSONObject rankPackage = result.getJSONObject(rankType);
            JSONArray rankList = rankPackage.getJSONArray("rankList");
            for (Object o : rankList) {
                JSONObject comic = (JSONObject) o;
                Integer rank = comic.getInteger("place");
                JSONObject comicInfo = comic.getJSONObject("webtoon");
                String comicId = comicInfo.getString("titleNo");
                String code = "dmmh-" + comicId;
                String name = comicInfo.getString("title");
                ComicOriginalBillboard billboard = new ComicOriginalBillboard();
                billboard.setDay(day);
                billboard.setPlatformId(platformId);
                billboard.setBillboardType(rankType);
                billboard.setRank(rank);
                billboard.setCode(code);
                billboard.setName(name);
                list.add(billboard);
            }
        }
        putModel(page, list);

    }


    private void info(Page page) {
        Job job = ((DelayRequest) page.getRequest()).getJob();

        JSONObject jsonObject = page.getJson().toObject(JSONObject.class);

        JSONObject titleInfo = jsonObject.getJSONObject("message").getJSONObject("result").getJSONObject("titleInfo");
        String author = titleInfo.getString("writingAuthorName");
        Comic comic = new Comic();
        comic.setCode(job.getCode());
        comic.setPlatformId(job.getPlatformId());
        comic.setName(titleInfo.getString("title"));
        comic.setIntro(titleInfo.getString("synopsis"));
        comic.setAuthor(author);
        comic.setHeaderImg("https://cdn.dongmanmanhua.cn" + titleInfo.getString("thumbnail"));
        comic.setEpisode(titleInfo.getInteger("totalServiceEpisodeCount"));
        comic.setEndEpisodeTime(new Date(titleInfo.getLong("lastEpisodeRegisterYmdt")));


        putModel(page, comic);

        ComicAuthorRelation car = new ComicAuthorRelation();
        car.setPlatformId(job.getPlatformId());
        car.setComicCode(job.getCode());
        car.setAuthorId(author);
        car.setAuthorName(author);

        putModel(page, car);

        ComicDmmh dmmh = new ComicDmmh();
        dmmh.setCode(job.getCode());
        dmmh.setReadCount(titleInfo.getLong("readCount"));
        dmmh.setFavoriteCount(titleInfo.getLong("favoriteCount"));
        dmmh.setMana(titleInfo.getLong("mana"));
        dmmh.setLikeitCount(titleInfo.getLong("likeitCount"));

        putModel(page, dmmh);

        List<Job> jobs = new ArrayList<>();
        String titleNo = titleInfo.getString("titleNo");
        Integer totalEpisode = titleInfo.getInteger("totalServiceEpisodeCount");
        for (int i = 1; i <= totalEpisode; i++) {
            String url = String.format(commentUrl, titleNo, i);
            Job commentJob = new Job(url);
            commentJob.setCode(job.getCode());
            commentJob.setPlatformId(52);
            jobs.add(commentJob);
        }

        putModel(page, jobs);

    }

    private void comment(Page page) {
        Job job = ((DelayRequest) page.getRequest()).getJob();
        String url = page.getUrl().get();

        JSONObject jsonObject = page.getJson().toObject(JSONObject.class);
        Integer code = jsonObject.getInteger("code");

        if (code != 200) {
            return;
        }

        Integer commentCount = jsonObject.getJSONObject("data").getInteger("showTotalCount");

        ComicDmmhComment comment = new ComicDmmhComment();
        comment.setCode(job.getCode());
        comment.setCommentCount(commentCount);
        Integer episode = Integer.valueOf(this.getParam(url, "episodeNo"));
        comment.setEpisode(episode);

        putModel(page, comment);

    }

    private void episode(Page page) {
        Job job = ((DelayRequest) page.getRequest()).getJob();
        JSONObject jsonObject = page.getJson().toObject(JSONObject.class);
        JSONObject message = (JSONObject) jsonObject.get("message");
        JSONObject result = message.getJSONObject("result");
        JSONObject episodeList = result.getJSONObject("episodeList");
        JSONArray episode = episodeList.getJSONArray("episode");
        List<ComicEpisodeInfo> episodeInfos = Lists.newArrayList();
        Date day = DateUtil.getDayStartTime(new Date());
        for (int i = 0; i < episode.size(); i++) {
            JSONObject json = (JSONObject) episode.get(i);
            ComicEpisodeInfo info = new ComicEpisodeInfo();
            info.setCode(job.getCode());
            info.setPlatformId(52);
            info.setDay(day);
            info.setName(json.getString("episodeTitle").trim());
            info.setEpisode(json.getInteger("episodeNo"));
            info.setVipStatus(0);
            info.setComicCreatedTime(new Date(json.getLong("registerYmdt")));
            episodeInfos.add(info);
        }
        putModel(page, episodeInfos);


        //任务指不定有几个
        List<Job> jobs = new ArrayList<>();

        //分集数
        Integer size = episodeInfos.size();

        if (size <= 1000) {
            List<Integer> list = EPISODE_INIT_LIST.subList(0, size);
            Job episodeLiksJob = this.createEpisodeLiksJob(job, list);
            jobs.add(episodeLiksJob);
        } else {
            int remainder = size % 1000;
            int whole = size / 1000;
            Integer end = 1000;
            for (int i = 0; i < whole; i++) {
                Integer start = i * 1000;
                end = (i + 1) * 1000;
                List<Integer> list = EPISODE_INIT_LIST.subList(start, end);
                Job episodeLiksJob = this.createEpisodeLiksJob(job, list);
                jobs.add(episodeLiksJob);
            }
            if (remainder > 0) {
                List<Integer> list = EPISODE_INIT_LIST.subList(end, (end + remainder));
                Job episodeLiksJob = this.createEpisodeLiksJob(job, list);
                jobs.add(episodeLiksJob);
            }
        }

        putModel(page, jobs);


    }

    private String getParam(String url, String name) {
        String params = url.substring(url.indexOf("?") + 1, url.length());
        Map<String, String> split = Splitter.on("&").withKeyValueSeparator("=").split(params);
        return split.get(name);
    }


    private Job createEpisodeLiksJob(Job oldJob, List<Integer> episodeList) {
        String episodeStr = StringUtils.join(episodeList, ",");
        Job episodeListsJob = new Job();
        episodeListsJob.setCode(oldJob.getCode());
        String comicId = StringUtils.replace(oldJob.getCode(), "dmmh-", "");
        String likesUrl = String.format(EPISODE_LIKES_URL, comicId, episodeStr);
        episodeListsJob.setUrl(likesUrl);
        return episodeListsJob;
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
