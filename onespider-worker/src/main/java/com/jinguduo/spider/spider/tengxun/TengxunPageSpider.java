package com.jinguduo.spider.spider.tengxun;


import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.spider.listener.UserAgentSpiderListener;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.exception.AntiSpiderException;
import com.jinguduo.spider.common.exception.PageBeChangedException;
import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.data.table.Show;
import com.jinguduo.spider.data.table.VipEpisode;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.selector.PlainText;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 * <p>
 * 用户录入网综页面 http://v.qq.com/detail/4/48476.html
 * <p>
 * 取出48476为网综id
 * 拼接 http://s.video.qq.com/loadplaylist?callback=jQuery19102759763043414536_1465884369999&low_login=1&type=6&id=<网综id>&plname=qq&vtype=3&video_type=10&inorder=1&otype=json&_=1465884370000
 *
 * @DATE 16/6/14 上午11:24
 */
@Worker
@CommonsLog
public class TengxunPageSpider extends CrawlSpider {

    private final static String ONE_EPISODE = "http://sns.video.qq.com/tvideo/fcgi-bin/batchgetplaymount?id=%s&otype=json";

    private final static String ONE_EPISODE_YEAR_JOB = "http://s.video.qq.com/loadplaylist?callback=jQuery19109938518798332361_1467475343725&low_login=1&type=4&id=%s&plname=qq&vtype=3&video_type=10&inorder=1&otype=json&year=%s&_=1467475343736";

    private final static String TOTAL_PLAY_COUNT = "http://data.video.qq.com/fcgi-bin/data?tid=70&&appid=10001007&appkey=e075742beb866145&callback=jQuery19109213305850191142_1468217242170&low_login=1&idlist=%s&otype=json&_=1468217242171";

    private final static String COMMENT_ID_URL = "http://ncgi.video.qq.com/fcgi-bin/video_comment_id?otype=json&op=%s&vid=%s";

    private final static String ZONGYI_COMMENT_ID_URL = "http://ncgi.video.qq.com/fcgi-bin/video_comment_id?otype=json&op=%s&cid=%s";

    //弹幕targetid
    private final static String DANMU_TARGETID_URL = "http://bullet.video.qq.com/fcgi-bin/target/regist?vid=%s&cid=%s";

    private static final List<String> specialVariety = Lists.newArrayList("63801", "56114", "56234", "69844", "68362", "68145", "67213");

    private Site site = SiteBuilder.builder()
            .addSpiderListener(new UserAgentSpiderListener())
            .setDomain("s.video.qq.com")
            .build();

    private PageRule rule = PageRule.build()
            .add(".", page -> tenXunProcess(page));/*step1：详情页入口*/

    public void tenXunProcess(Page page) {
        Job oldJob = ((DelayRequest) page.getRequest()).getJob();
        String url = oldJob.getUrl();

        String rawText = page.getRawText();
        String json_str = null;
        if (rawText.lastIndexOf(")") > 0) {
            json_str = rawText.substring(rawText.indexOf("(") + 1, rawText.lastIndexOf(")"));
        } else {
            throw new AntiSpiderException("s.video.qq.com 分集未获取到");
        }
        JSONObject json = JSONObject.parseObject(json_str);
        JSONObject videoPlayList = json.getJSONObject("video_play_list");
        if (videoPlayList == null) {
            throw new PageBeChangedException(rawText);
        }

        // 处理pay_type属性不存在抛出的空指针
        String category = videoPlayList.getString("pay_type");  // 视频分类
        if ("1".equals(category) || "2".equals(category)) {  // 电影||网络剧
            this.dramaProcess(page, videoPlayList);
        } else if (url.contains("type=6")) {
            this.networkVarietyProcess(category, page, videoPlayList);
        } else if (url.contains("type=4")) {
            this.networkVarietyExProcess(category, page);
        }
    }

    /**
     * 网综处理 pay_type:0
     * 2016/11/9 新增电视综艺，分集评论数抓取
     *
     * @param page
     * @param videoPlayList
     */
    private void networkVarietyProcess(String category, Page page, JSONObject videoPlayList) {

        Job oldJob = ((DelayRequest) page.getRequest()).getJob();

        String id = page.getUrl().regex("\\&id=(\\d+)\\&", 1).get();

        List<String> years = (List<String>) videoPlayList.get("year");

        JSONObject playList = (JSONObject) videoPlayList.get("playlist");

        List<Job> jobs = Lists.newArrayList();
        List<Show> shows = Lists.newArrayList();
        List<VipEpisode> vips = Lists.newArrayList();
        if (years != null && playList != null) {
            for (int i = 0; i < years.size(); i++) {
                String year = years.get(i);
                List<JSONObject> plays = (List<JSONObject>) playList.get(year);
                if (plays == null || plays.size() == 0) {
                    Job job = new Job();
                    DbEntityHelper.derive(oldJob, job);
                    job.setCode(oldJob.getCode());
                    job.setUrl(String.format(ONE_EPISODE_YEAR_JOB, id, year));
                    jobs.add(job);
                } else {
                    this.networkVarietyGet(category, oldJob, plays, jobs, shows, vips);
                    String pid = plays.get(0).getString("id");  // 任取一个
                    Job job = this.totalPlayCount(pid, page);
                    jobs.add(job);
                }
            }
        }
        putModel(page, jobs);
        putModel(page, shows);
        if (!vips.isEmpty()) {
            putModel(page, vips);
        }
    }

    private void networkVarietyExProcess(String category, Page page) {
        Job oldJob = ((DelayRequest) page.getRequest()).getJob();
        String s = page.getRawText();
        JSONObject j = JSONObject.parseObject(s.substring(s.indexOf("(") + 1, s.lastIndexOf(")")));
        String url = page.getRequest().getUrl();
        String year = url.substring(url.indexOf("year=") + 5, url.lastIndexOf("&"));
        List<JSONObject> plays = (List<JSONObject>) j.getJSONObject("video_play_list").getJSONObject("playlist").get(year);

        List<Job> jobs = Lists.newArrayList();
        List<Show> shows = Lists.newArrayList();
        List<VipEpisode> vips = Lists.newArrayList();
        this.networkVarietyGet(category, oldJob, plays, jobs, shows, vips);

        putModel(page, jobs);
        putModel(page, shows);
        if (!vips.isEmpty()) {
            putModel(page, vips);
        }
    }

    /**
     * 遍历分集List，试用于有分集的视频类型（非网大）
     * 网剧或者网大，分集list处理 pay_type:2
     *
     * @param page
     * @param videoPlayList
     */
    private void dramaProcess(Page page, JSONObject videoPlayList) {

        Job oldJob = ((DelayRequest) page.getRequest()).getJob();

        List<Job> jobs = Lists.newArrayList();
        List<Show> shows = Lists.newArrayList();
        List<VipEpisode> vips = Lists.newArrayList();
        List<JSONObject> playlist = null;
        try {
            playlist = (List) videoPlayList.getJSONArray("playlist");
        } catch (Exception ex) {
            //有对于之前来说分类是网剧的,但是确实是综艺,导致转失败了,先在这里去走一下网综吧
            this.networkVarietyProcess("2", page, videoPlayList);
        }

        if (CollectionUtils.isEmpty(playlist)) {
            return;
        }
        //专辑Id
        String pId = new PlainText(playlist.get(0).getString("url")).regex("cover\\/(.*?)\\/", 1).get();
        if (StringUtils.isBlank(pId)) {
            pId = oldJob.getCode();
        }
        final String parentId = pId;
        //获取专辑Id，生成专辑播放量Job
        jobs.add(this.totalPlayCount(pId, page));
        //过滤只保存正片（预告片 "2"; 正片 "1"）
        //playlist.stream().filter(j -> "1".equals(j.getString("type"))).forEach(j -> saveShows(j, parentId, oldJob,shows,jobs));

        //hello 女神 的type不是这种规则，我去
        playlist.stream().forEach(j -> saveShows(j, parentId, oldJob, shows, jobs, vips));

        if (!jobs.isEmpty()) {
            putModel(page, jobs);
        }
        if (!shows.isEmpty()) {
            putModel(page, shows);
        }
        if (!vips.isEmpty()) {
            putModel(page, vips);
        }
    }

    private void saveShows(JSONObject play, String pId, Job oldJob, List<Show> shows, List<Job> jobs, List<VipEpisode> vips) {
        //自定义变量
        final int op = 3;//取值：1,2,3均可
        //分集Id
        String playId = play.getString("id");
        if (StringUtils.isBlank(playId)) {
            return;
        }
        //这里有时过滤不掉预告和花絮，可能是由于在未上映之前的某些花絮和预告type为1
        if (!"1".equals(play.getString("type"))) {// 1:正片，2:预告片
            return;
        }
        // FIXME: 先忽略“番外” xp
        if (play.getString("episode_number") == null
                || play.getString("episode_number").contains("番外")) {
            return;
        }
        //show item
        Show show = new Show(play.getString("title"), playId, oldJob.getPlatformId(), oldJob.getShowId());
        show.setDepth(2);
        Integer episodeNumber = null;
        try {
            String epiStr = play.getString("episode_number").replace("第", "").replace("集", "").trim();
            episodeNumber = Integer.valueOf(epiStr);
        } catch (Exception e) {
//            e.printStackTrace();
            return;
        }
        show.setEpisode(episodeNumber);
        show.setParentCode(oldJob.getCode());
        show.setUrl(String.format(ONE_EPISODE, playId));
        shows.add(show);

        //储存vip
        this.saveVip(play, show, vips);

        //commentId job
        Job jobComment = DbEntityHelper.derive(oldJob, new Job());
        jobComment.setUrl(String.format(COMMENT_ID_URL, op, playId));
        jobComment.setCode(show.getCode());
        jobs.add(jobComment);
        //danmuId job
        Job danmuTargetJob = new Job(String.format(DANMU_TARGETID_URL, playId, pId));
        DbEntityHelper.derive(oldJob, danmuTargetJob);
        danmuTargetJob.setCode(playId);
        jobs.add(danmuTargetJob);
        //不知道干嘛的
        Job job = new Job();
        DbEntityHelper.derive(oldJob, job);
        job.setCode(playId);
        job.setUrl(String.format(ONE_EPISODE, playId));
        jobs.add(job);
    }


    /**
     * 总播放量任务
     * 1.剧
     * 2.电影
     * 3.综艺：比较特殊，生成的任务并不是总的专辑播放量，下一个爬虫会做综艺特殊处理
     *
     * @param id
     * @param page
     */
    private Job totalPlayCount(String id, Page page) {
        Job oldJob = ((DelayRequest) page.getRequest()).getJob();
        Job job = new Job();
        DbEntityHelper.derive(oldJob, job);
        job.setCode(oldJob.getCode());
        job.setUrl(String.format(TOTAL_PLAY_COUNT, id));
        return job;
    }

    private void networkVarietyGet(String category, Job oldJob, List<JSONObject> plays, List<Job> jobs, List<Show> shows, List<VipEpisode> vips) {
        //预告片: "2" ; 正片: "1"
        //plays.stream().filter(j -> "1".equals(j.getString("type"))).forEach(j -> saveVarietyShow(j, oldJob, shows, jobs));
        //hello 女神 的type不是这种规则，我去
        plays.stream().forEach(j -> saveVarietyShow(category, j, oldJob, shows, jobs, vips));
    }

    private void saveVarietyShow(String category, JSONObject play, Job oldJob, List<Show> shows, List<Job> jobs, List<VipEpisode> vips) {
        final int op = 3;//登陆校验，取值：1,2,3均可
        String pId = play.getString("id");
        String vid = play.getString("vid");
        String episode = play.getString("episode_number").replaceAll("[^0-9]", "");

        if (StringUtils.isBlank(pId)) {
            return;
        }
        if (!"1".equals(play.getString("type"))) {// 1:正片，2:预告片
            return;
        }

        Job job = new Job();
        DbEntityHelper.derive(oldJob, job);
        job.setCode(pId);
        job.setUrl(String.format(ONE_EPISODE, pId));
        jobs.add(job);

        //shows
        Show show = new Show(play.getString("title"), pId, job.getPlatformId(), oldJob.getShowId());
        show.setDepth(2);
        if (!play.getString("episode_number").matches("\\d+-\\d+-\\d+") || play.getString("episode_number").matches("[u4e00-u9fa5]+")) {
            log.warn("episode_number not date, rawtext is:" + play);
            //先这么处理，明天再根据业务需要修改

            return;
        }
        show.setReleaseDate(play.getDate("episode_number"));
        show.setEpisode(Integer.valueOf(episode));
        show.setParentCode(oldJob.getCode());
        shows.add(show);

        //储存vip
        this.saveVip(play, show, vips);

        //commentId job
        String commentUrl = null;
        if (specialVariety.contains(oldJob.getCode())) {//特殊综艺
            commentUrl = String.format(COMMENT_ID_URL, op, vid);
        } else if (pId.length() == 15) {
            commentUrl = String.format(ZONGYI_COMMENT_ID_URL, op, pId);//综艺
        } else if (pId.length() == 11) {
            commentUrl = String.format(COMMENT_ID_URL, op, pId);//其他
        }
        if (commentUrl != null) {
            Job jobCommentId = DbEntityHelper.derive(job, new Job());
            jobCommentId.setUrl(commentUrl);
            jobCommentId.setCode(show.getCode());
            jobs.add(jobCommentId);
        }

        //danmuId job
        Job danmuTargetJob = new Job(String.format(DANMU_TARGETID_URL, vid, pId));
        DbEntityHelper.derive(oldJob, danmuTargetJob);
        danmuTargetJob.setCode(show.getCode());
        jobs.add(danmuTargetJob);
    }

    private void saveVip(JSONObject play, Show show, List<VipEpisode> vips) {
        if (play.getIntValue("episode_pay") == 1) {
            VipEpisode ve = new VipEpisode();
            ve.setCode(show.getCode());
            ve.setPlatformId(show.getPlatformId());
            vips.add(ve);
        }
    }

    @Override
    public Site getSite() {
        return this.site;
    }

    @Override
    public PageRule getPageRule() {
        return rule;
    }
}
