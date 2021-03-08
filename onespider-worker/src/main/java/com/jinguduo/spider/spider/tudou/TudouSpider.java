package com.jinguduo.spider.spider.tudou;


import java.sql.Timestamp;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.spider.listener.UserAgentSpiderListener;
import com.jinguduo.spider.common.constant.FrequencyConstant;
import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.common.util.NumberHelper;
import com.jinguduo.spider.data.table.CommentLog;
import com.jinguduo.spider.data.table.Show;
import com.jinguduo.spider.data.table.ShowLog;
import com.jinguduo.spider.data.text.CommentText;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.selector.Html;
import com.jinguduo.spider.webmagic.selector.Selectable;

import lombok.extern.apachecommons.CommonsLog;

//已下架爬虫任务
//@Worker
@CommonsLog
@Deprecated
public class TudouSpider extends CrawlSpider {

    Logger logger = LoggerFactory.getLogger(TudouSpider.class);

    private Site site = SiteBuilder.builder()
            .addSpiderListener(new UserAgentSpiderListener())
            .setDomain("www.tudou.com").build();

    private final static String TD = "http://www.tudou.com/crp/getAlbumvoInfo.action?charset=utf-8&acode=%s&areaCode=110000";

    private final static String TD_S = "http://dataapi.youku.com/getData?num=200001&icode=%s";

    private final static String TUDOU_COMMENT_URL = "http://www.tudou.com/comments/itemnewcomment.srv?iid=%s&page=1&rows=1&charset=utf-8&app=anchor";

    private final static String TUDOU_COMMENT_URL_20170222 = "http://www.tudou.com/crp/itemSum.action?iabcdefg=%s&uabcdefg=0";

    private final static String DANMU_URL = "http://service.danmu.tudou.com/list?ct=1001&mat=1&iid=%s&mcount=5";

    //土豆评论文本URL
    private final static String COMMENT_CONTENT_URL = "http://www.tudou.com/comments/itemnewcomment.srv?iid=%s&page=1&rows=50";

    //评论文本分页请求区间
    private final static Integer INCREMENT_SCOPE = 1;

    private PageRule rules = PageRule.build()
            .add("/albumcover/", page -> processAlbumCover(page))//综艺专辑页入口：http://www.tudou.com/albumcover/sm44IPEJjik.html
            .add("/crp/getAlbumvoInfo", page -> list(page))//综艺分集期刊List process
            .add("/albumplay/", page -> processNetMovie(page))
            .add("(/listplay/|/programs/)", page -> processNetMovieSelf(page))
            .add("itemSum.action", page -> comments(page))//评论
            .add("itemnewcomment.srv",page -> commentText(page));//评论文本


    /**
     * 解析评论文本
     * created by gsw
     * 2017年2月22日17:16:07
     * @param page
     */
    private void commentText(Page page) {
        Job oldJob = ((DelayRequest) page.getRequest()).getJob();
        String rawText = page.getRawText();
        JSONObject jsonObject = JSON.parseObject(rawText);
        if(StringUtils.isEmpty(jsonObject.getString("data"))
                ||"{}".equals(jsonObject.getString("data"))||"[]".equals(jsonObject.getString("data"))){
            log.debug("no comments content needed!");
            return;
        }
        /**评论文本json集合*/
        List<JSONObject> jsonObjects = (List)jsonObject.getJSONArray("data");
        /**点赞量及恢复量json*/
        JSONObject upAndReplyJson = jsonObject.getJSONObject("popularityList");
        if(CollectionUtils.isNotEmpty(jsonObjects)) {
            List<CommentText> commentTexts = Lists.newArrayListWithCapacity(jsonObjects.size());
            jsonObjects.stream().forEach(jsonObj ->analysis(jsonObj,page,oldJob,commentTexts,upAndReplyJson) );
            putModel(page, commentTexts);
        }

        /**生成下一个任务*/
        createNextJob(page,oldJob,jsonObjects);
    }

    private void analysis(JSONObject json,Page page,Job job,List<CommentText> cts,JSONObject upReplyJson) {
        try {
            String commentId = json.getString("commentId");//评论ID
            String content = json.getString("content");//评论文本
            Long create_time = json.getLong("publish_time");//文本创建时间
            String userId = json.getString("userID");//用户ID
            String nickname = json.getString("nickname");//用户昵称
            Integer up = 0;//点赞量
            Integer replyCount = 0;//回复数

            /**获取每个评论对应的点赞量及恢复点赞量的json*/
            JSONObject upAndReplyJson = upReplyJson.getJSONObject(commentId);
            if(null !=upAndReplyJson) {
                up = upAndReplyJson.getInteger("agree");
                replyCount = upAndReplyJson.getInteger("reply");
            }
            CommentText commentText = new CommentText();
            commentText.setCommentId(commentId);
            commentText.setUp(up);
            commentText.setReplyCount(replyCount);
            commentText.setContent(content);
            commentText.setCreatedTime(new Timestamp(create_time));
            commentText.setUserId(userId);
            commentText.setNickName(nickname);
            DbEntityHelper.derive(job, commentText);
            cts.add(commentText);
        }catch (Exception e) {
            logger.error("analysis comments content failed!",e);
        }
    }

    private void createNextJob(Page page, Job job,List<JSONObject> jsonObjectList) {
        try {
            if(jsonObjectList ==null ||jsonObjectList.isEmpty()) {
                return;
            }
            //当前进度
            Integer current = Integer.valueOf(page.getUrl().regex("&page=(\\d*)").get());
            //计算下一个任务的进度
            final Integer nextProgress = INCREMENT_SCOPE + current;
            //创建递归任务
            final String nextUrl = page.getUrl().replace("&page=(\\d*)",String.format("&page=%s",nextProgress)).get();
            Job newJob = DbEntityHelper.deriveNewJob(job,nextUrl);
            newJob.setFrequency(FrequencyConstant.COMMENT_TEXT);
            putModel(page,newJob);
        }catch (Exception e) {
            logger.error("create tudou comments content job failed,"+page.getUrl(),e);
        }
    }
    private void comments(Page page) {

        Job oldJob = ((DelayRequest) page.getRequest()).getJob();

        JSONObject jsonObject = JSONObject.parseObject(page.getRawText());

        CommentLog commentLog = new CommentLog(jsonObject.getInteger("commentNum"));
        DbEntityHelper.derive(oldJob, commentLog);

        putModel(page,commentLog);


    }

    /***
     * 土豆综艺专辑页
     * @param page
     */
    private void processAlbumCover(Page page) {
        log.debug("processAlbumCover begin by url = ["+page.getUrl()+"]");
        Job oldJob = ((DelayRequest) page.getRequest()).getJob();
        String url = page.getRequest().getUrl();

        try {
            // 提取总播放数: //div[@class='cover_keys']/div/span[@class='key_item t_1']/text()
            List<Selectable> all = page.getHtml().xpath("//div[@class='cover_keys']//span[@class='t_1']/text()").nodes();
            String playCount = all.get(all.size() - 1).get();
            ShowLog showLog = new ShowLog();
            DbEntityHelper.derive(oldJob, showLog);
            showLog.setPlayCount(NumberHelper.parseLong(playCount, -1));
            putModel(page,showLog);
        } catch (Exception e) {
            log.error(url, e);
        }
        
            // 生成子任务（专辑详情）
            String code = url.substring(url.lastIndexOf("/") + 1, url.lastIndexOf("."));
            Job newJob = new Job(String.format(TD, code));
            DbEntityHelper.derive(oldJob, newJob);
            newJob.setCode(code);
            putModel(page,newJob);
    }

    /***
     * 视频信息 json analysis process
     *  1.<strong>综艺</strong> 根据专题页<code>acode</code>，获取分集期刊信息并解析，
     *  2.<strong>网大</strong> 视频详情信息的解析，同网大
     *  3.insert 分集show
     *  4.insert 分集点击量
     *  5.create 分集评论 job
     * @param page
     */
    private void list(Page page) {

        log.debug("processAlbumvoInfo begin by url = ["+page.getUrl()+"]");

        Job oldJob = ((DelayRequest) page.getRequest()).getJob();

        List<ShowLog> showLogs = Lists.newArrayList();
        List<Show> shows = Lists.newArrayList();
        List<Job> jobList = Lists.newArrayList();

        JSONObject jsonObject = JSONObject.parseObject(page.getRawText());

        if( null == jsonObject && null != jsonObject.getString("error")){
            return;
        }
        List<JSONObject> jsonArray = (List) jsonObject.getJSONArray("items");

        jsonArray.stream()
                .filter(json -> !json.getBoolean("trailer"))
                .forEach(json -> analysisList(oldJob, showLogs, shows, jobList, json));

        if (CollectionUtils.isNotEmpty(showLogs)){
            putModel(page,showLogs);
        }
        if (CollectionUtils.isNotEmpty(shows)){
            putModel(page,shows);
        }
        if (CollectionUtils.isNotEmpty(jobList)){
            putModel(page,jobList);//评论任务
        }
        log.debug("end processAlbumvoInfo by url = ["+page.getUrl()+"]");
    }

    /***
     * "一夫多妻"==》欢乐戏剧人一期存在多个子期，
     * ep: 20170515 包含 1(正常),1(完整),2(超长)等期数
     * <strong>我们规定：</strong>
     * <ul>
     *     <li>正常->2017051501;
     *     <li>完整->2017051502;
     *     <li>超长->2017051503;
     */
    private final static Set<String> SPECIL_POLYGAMY = Sets.newHashSet("K4VRomOqP60");
    /***
     * 解析分集list
     */
    private void analysisList(Job oldJob, List<ShowLog> showLogs, List<Show> shows, List<Job> jobList, JSONObject json) {

        String url = json.getString("itemPlayUrl");
        String pcode = oldJob.getCode();
        if(url == null){
            return;
        }
        String videoName = json.getString("fjTitle");
        String code = url.substring(url.lastIndexOf("/") + 1, url.lastIndexOf("."));
        String iid = json.getString("itemId");
        Long playCount = json.getLong("playTimes");
        Integer episode = json.getInteger("episode");//polygamy
        if (SPECIL_POLYGAMY.contains(pcode)) {//必须遵守下面的规定，保证和优酷一致，最终实现分组合并数据
            if (videoName.contains("完整版")) {//yyyyMMdd02
                episode = Integer.valueOf(StringUtils.join(episode,"02"));
            } else if (videoName.contains("超长版")) {//yyyyMMdd03
                episode = Integer.valueOf(StringUtils.join(episode,"03"));
            } else {//yyyyMMdd01
                episode = Integer.valueOf(StringUtils.join(episode,"01"));
            }
        }
        //创建show
        createShow(shows,videoName,code,episode,oldJob);
        //播放量
        createShowLog(showLogs,playCount,code,oldJob);
        //评论数Job
        createCommentJob(jobList,iid,code,oldJob);
        //弹幕Job
        createDanmuJob(jobList,iid,code,oldJob);
        //评论文本Job
        createCommentTextJob(jobList,iid,code,oldJob);

    }

    private void createDanmuJob(List<Job> jobList, String iid, String code, Job oldJob) {
        String durl = String.format(DANMU_URL,iid);
        try {
            Job newJob = DbEntityHelper.deriveNewJob(oldJob, durl);
            newJob.setFrequency(FrequencyConstant.BARRAGE_TEXT);
            newJob.setCode(code);
            jobList.add(newJob);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 评论文本job
     * created by gsw
     * 2017年2月22日18:49:46
     * @param jobList
     * @param vid
     * @param code
     * @param oldJob
     */
    private void  createCommentTextJob(List<Job> jobList, String vid, String code, Job oldJob) {
        Job newCommentTextJob = null;
            newCommentTextJob = new Job(String.format(COMMENT_CONTENT_URL,vid));
            DbEntityHelper.derive(oldJob, newCommentTextJob);
            newCommentTextJob.setCode(code);//code 使用分集code
            newCommentTextJob.setFrequency(FrequencyConstant.COMMENT_TEXT);
            jobList.add(newCommentTextJob);
    }
    /***
     * parent : <strong>analysisList</strong>
     * @param jobList
     * @param vid
     * @param code
     * @param oldJob
     */
    private void    createCommentJob(List<Job> jobList, String vid, String code, Job oldJob) {
        Job newCommentJob = null;
            newCommentJob = new Job(String.format(TUDOU_COMMENT_URL_20170222,vid));
            DbEntityHelper.derive(oldJob, newCommentJob);
            newCommentJob.setCode(code);//code 使用分集code
            newCommentJob.setFrequency(FrequencyConstant.COMMENT_COUNT);
            jobList.add(newCommentJob);
    }

    /***
     * parent : <strong>analysisList</strong>
     * @param showLogs
     * @param playCount
     * @param code
     * @param oldJob
     */
    private void createShowLog(List<ShowLog> showLogs, Long playCount, String code, Job oldJob) {
        ShowLog showLog = new ShowLog();
        DbEntityHelper.derive(oldJob, showLog);
        showLog.setPlayCount(playCount);
        showLog.setCode(code);
        showLogs.add(showLog);
    }

    /***
     * parent : <strong>analysisList</strong>
     * @param shows
     * @param videoName
     * @param code
     * @param episode
     * @param oldJob
     */
    private void createShow(List<Show> shows, String videoName, String code, Integer episode, Job oldJob) {
        Show show = new Show(videoName,code, oldJob.getPlatformId(), oldJob.getShowId());
        show.setDepth(2);
        show.setEpisode(episode);
        show.setParentCode(oldJob.getCode());
        shows.add(show);
    }

    /***
     * 网大 video page captrue process
     * 网络大电影视频链接入口方法：
     *  1.拼装获取视频信息（json）链接的方法，生成子任务
     * @param page
     */
    private void processNetMovie(Page page) {

        Job oldJob = ((DelayRequest) page.getRequest()).getJob();

        if (oldJob == null) {
            return;
        }

        List<Job> jobList = Lists.newArrayListWithCapacity(3);
        String iid = "";
        String commentUrl = "";
        String icode = "";

        Html html = page.getHtml();
        try {
            icode = page.getHtml().xpath("/html/body/script[1]").regex("(?s)icode:.'(\\S*)'", 1).get();
            if (null != icode) {
                Job job = new Job(String.format(TD_S, icode));
                DbEntityHelper.derive(oldJob, job);
                job.setCode(oldJob.getCode());
                jobList.add(job);
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }

        try {
            iid = html.xpath("/html/body/script[1]").regex("(?s)iid:.(\\d*)",1).get();
            if( null != iid ){
                commentUrl = String.format(TUDOU_COMMENT_URL_20170222,iid);
                Job newCommentJob = new Job(commentUrl);
                DbEntityHelper.derive(oldJob, newCommentJob);
                newCommentJob.setCode(oldJob.getCode());//网大code和job一致
                newCommentJob.setFrequency(FrequencyConstant.COMMENT_COUNT);

                jobList.add(newCommentJob);

                //弹幕Job
                createDanmuJob(jobList,iid,oldJob.getCode(),oldJob);
                //评论文本Job
                createCommentTextJob(jobList,iid,oldJob.getCode(),oldJob);
            }
        } catch (Exception e){
            log.error("unknow exception! by url :["+page.getUrl()+"]");
            log.error(e.getMessage(), e);
        }

        if(!jobList.isEmpty()){
            putModel(page,jobList);
        }
    }

    /**
     * 土豆网大的自媒体数据
     * @param page
     */
    private void processNetMovieSelf(Page page){
        log.debug("processNetMovieSelf begin by url = ["+page.getUrl()+"]");
        Job oldJob = ((DelayRequest) page.getRequest()).getJob();
        if (oldJob == null) return;
            // 生成子任务（专辑详情）
            Job newJob = new Job(String.format(TD_S, oldJob.getCode()));
            putMoviePlayCountJob(oldJob,newJob,page);
    }

    private void putMoviePlayCountJob(Job oldJob,Job newJob ,Page page){
        DbEntityHelper.derive(oldJob, newJob);
        newJob.setCode(oldJob.getCode());
        putModel(page,newJob);
    }

    @Override
    public PageRule getPageRule() {
        return rules;
    }

    @Override
    public Site getSite() {
        return this.site;
    }
}
