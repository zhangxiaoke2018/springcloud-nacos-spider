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
import com.jinguduo.spider.common.constant.FrequencyConstant;
import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.common.util.Md5Util;
import com.jinguduo.spider.common.util.RegexUtil;
import com.jinguduo.spider.data.table.Show;
import com.jinguduo.spider.data.table.ShowLog;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.utils.UrlUtils;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 09/11/2016 5:07 PM
 */
@Worker
@CommonsLog
public class BiliBiliAnimeSpider extends CrawlSpider {

    private Site site = SiteBuilder.builder().setDomain("bangumi.bilibili.com").build();//http://bangumi.bilibili.com/anime/5626

    //播放量和分集
//    private static final String PLAY_COUNT_URL = "http://bangumi.bilibili.com/jsonp/seasoninfo/%s.ver?callback=seasonListCallback&jsonp=jsonp";
    private static final String PLAY_COUNT_URL = "http://bangumi.bilibili.com/ext/web_api/season_count?season_id=%s&season_type=4";


    //获取弹幕cid
    private static final String GET_SOURCE_URL = "http://bangumi.bilibili.com/web_api/get_source?episode_id=%s&csrf=";
    //弹幕
    private static final String COMMENT_URL = "https://comment.bilibili.com/%s.xml";
    //评论
    private final String REPLY_URL = "https://api.bilibili.com/x/v2/reply?pn=%d&type=1&oid=%s&sort=0";

    //评论人数
    private final String COMMENT_NUMBER_URL = "https://api.bilibili.com/x/v2/reply?&pn=1&type=1&oid=6654184";

    //新链接
    private static final String NEW_URL = "https://www.bilibili.com/bangumi/media/md%s/";

    //日漫自动发现
    private static final String JAPAN_ANIME_AUTOFIND_URL = "https://bangumi.bilibili.com/media/web_api/search/result?season_version=-1&area=-1&is_finish=-1&copyright=-1&season_status=-1&season_month=-1&pub_date=-1&style_id=-1&order=3&st=1&sort=0&page=1&season_type=1&pagesize=20";

    private PageRule rules = PageRule.build()
            .add("/anime", page -> createCountJob(page))
            .add("seasoninfo", page -> processAnime(page))
            .add("season_count", page -> processAnimePlayCount(page))
            .add("/media/web_api/search/result", page -> processAutoFindAnime(page))
            .add("web_api/get_source", page -> createBarrageJob(page));

    private void processAutoFindAnime(Page page) {
        Job oldJob = ((DelayRequest) page.getRequest()).getJob();
        String oldUrl = oldJob.getUrl();


        JSONObject jsonObject = JSONObject.parseObject(page.getRawText());
        Integer status = jsonObject.getInteger("code");
        //检查奇怪的玩意
        if (0 != status) return;
        JSONObject result = jsonObject.getJSONObject("result");
        JSONArray datas = result.getJSONArray("data");
        List<Job> jobs = new ArrayList<>();
        for (int i = 0; i < datas.size(); i++) {
            JSONObject data = datas.getJSONObject(i);
            //子页面链接，用来获取真实的专辑页
            String link = data.getString("link");
            Job showJob = new Job(link);
            DbEntityHelper.derive(oldJob, showJob);
            String code = Md5Util.getMd5(link);
            showJob.setCode(code);
            jobs.add(showJob);
        }

        String oldPage = UrlUtils.getParam(oldUrl, "page");
        if ("1".equals(oldPage)) {
            String prefix = "&page=";
            for (int i = 2; i < 25; i++) {
                String pageUrl = StringUtils.replace(oldUrl, (prefix + oldPage), (prefix + i));
                Job pageJob = new Job(pageUrl);
                DbEntityHelper.derive(oldJob, pageJob);
                String code = Md5Util.getMd5(pageUrl);
                pageJob.setCode(code);
                jobs.add(pageJob);
            }
        }

        putModel(page, jobs);
    }


    private void processAnimePlayCount(Page page) {

        Job oldJob = ((DelayRequest) page.getRequest()).getJob();
        if (oldJob == null) {
            return;
        }
        JSONObject jsonObject = page.getJson().toObject(JSONObject.class);

        Long views = jsonObject.getJSONObject("result").getLong("views");

        ShowLog showLog = new ShowLog();
        showLog.setCode(oldJob.getCode());
        showLog.setPlatformId(oldJob.getPlatformId());
        showLog.setPlayCount(views);

        putModel(page, showLog);

    }

    //生成通过接口获取播放量的任务
    private void createCountJob(Page page) {
        log.debug("process bilibili anime " + page.getUrl().get());

        Job oldJob = ((DelayRequest) page.getRequest()).getJob();
        if (oldJob == null) {
            return;
        }
        Job newJob = DbEntityHelper.deriveNewJob(oldJob, String.format(PLAY_COUNT_URL, RegexUtil.getDataByRegex(oldJob.getUrl(), "anime/(\\d*)", 1)));
        putModel(page, newJob);
        //旧链接更新
        String url = oldJob.getUrl();
        String a = url.replaceAll("[^(0-9)]", "");
        String newUrl = String.format(NEW_URL, Integer.valueOf(a));
        Job newUrlJob = DbEntityHelper.deriveNewJob(oldJob, newUrl);
        putModel(page, newUrlJob);
    }


    private void processAnime(Page page) {

        Job oldJob = ((DelayRequest) page.getRequest()).getJob();
        if (oldJob == null) {
            return;
        }
        long playCount = 0L;

        String rawText = page.getRawText();
        String replace = rawText.replace("seasonListCallback(", "");
        JSONObject o = (JSONObject) JSONObject.parse(replace.substring(0, replace.lastIndexOf(")")));

        JSONObject result = o.getJSONObject("result");

        playCount = result.getLongValue("play_count");
        ShowLog showLog = new ShowLog();
        DbEntityHelper.derive(oldJob, showLog);
        showLog.setPlayCount(playCount);
        putModel(page, showLog);

        JSONArray episodes = result.getJSONArray("episodes");
        String title = result.getString("title");

        episodes.forEach(episode -> {
            JSONObject epi = (JSONObject) episode;
            String episodeId = epi.getString("episode_id");
            String showName = epi.getString("index_title");
            String indexStr = epi.getString("index");
            if (StringUtils.isBlank(showName)) {
                showName = String.join(" ", title, indexStr);
            }
            Integer episodeIndex = 0;
            if (StringUtils.isBlank(indexStr)) {
                return;
            } else if (NumberUtils.isDigits(indexStr)) {//纯数字
                episodeIndex = Integer.valueOf(indexStr);
            } else {//包含-"1-2"、"特别篇""10.5"这种暂不存
                return;
            }
            /** 生成show */
            Show show = new Show(showName, episodeId, oldJob.getPlatformId(), oldJob.getShowId());
            show.setDepth(2);
            show.setEpisode(episodeIndex);
            show.setParentCode(oldJob.getCode());
            putModel(page, show);
            /** 生成获取cid资源的job */
            Job getSourceJob = new Job(String.format(GET_SOURCE_URL, episodeId), "POST");
            DbEntityHelper.derive(oldJob, getSourceJob);
            getSourceJob.setCode(episodeId);
            getSourceJob.setFrequency(FrequencyConstant.BARRAGE_TEXT);//设置任务爬取频率
            putModel(page, getSourceJob);
        });

    }

    /**
     * 获取资源数据,创建获取弹幕的job和评论文本job
     *
     * @param page
     */
    private void createBarrageJob(Page page) {
        Job oldJob = ((DelayRequest) page.getRequest()).getJob();
        String rawText = page.getRawText();
        JSONObject obj = JSONObject.parseObject(rawText);
        String cid = obj.getJSONObject("result").getString("cid");
        String aid = obj.getJSONObject("result").getString("aid");
        //生成弹幕爬取任务
        Job commentJob = new Job(String.format(COMMENT_URL, cid));
        DbEntityHelper.derive(oldJob, commentJob);
        commentJob.setFrequency(FrequencyConstant.BARRAGE_TEXT);//设置任务爬取频率
        putModel(page, commentJob);
        //生成评论爬取任务
        Job replyJob = new Job(String.format(REPLY_URL, 1, aid));
        DbEntityHelper.derive(oldJob, replyJob);
        replyJob.setFrequency(FrequencyConstant.COMMENT_TEXT);
        putModel(page, replyJob);
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
