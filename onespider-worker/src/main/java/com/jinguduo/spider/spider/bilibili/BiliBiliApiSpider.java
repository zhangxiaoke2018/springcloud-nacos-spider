package com.jinguduo.spider.spider.bilibili;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.constant.FrequencyConstant;
import com.jinguduo.spider.common.util.DateUtil;
import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.common.util.RegexUtil;
import com.jinguduo.spider.data.table.ComicBilibili;
import com.jinguduo.spider.data.table.CommentLog;
import com.jinguduo.spider.data.table.Show;
import com.jinguduo.spider.data.text.CommentText;
import com.jinguduo.spider.webmagic.Page;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Worker
@CommonsLog
public class BiliBiliApiSpider extends CrawlSpider {

    private final String REPLY_URL = "https://api.bilibili.com/x/v2/reply?pn=%d&type=1&oid=%s&sort=0";

    //评论人数
    private final String COMMENT_NUMBER_URL="https://api.bilibili.com/x/v2/reply?&pn=1&type=1&oid=%s";

    //弹幕
    private static final String COMMENT_URL = "https://comment.bilibili.com/%s.xml";

    private Site site = SiteBuilder.builder()
            .addHeader("referer", " https://manga.bilibili.com")
            .setDomain("api.bilibili.com").build();

    private PageRule pageRule = PageRule.build()
            //bilibili剧集的评论解析
            .add("reply\\?pn=", page -> processReply(page))
            //bilibili漫画的评论解析
            .add("reply\\?type=", page -> processComicComment(page))
            .add("/pgc/web/season/section",page ->processShowList(page));

    private void processComicComment(Page page) {
        Job oldJob = ((DelayRequest) page.getRequest()).getJob();

        JSONObject object = JSONObject.parseObject(page.getRawText());
        if (object.getInteger("code") != 0) return;

        JSONObject replyObj = object.getJSONObject("data");
        JSONObject pageObject = replyObj.getJSONObject("page");
        Integer commentCount = pageObject.getInteger("acount");

        Date today = DateUtil.getDayStartTime(new Date());
        String code = oldJob.getCode();

        ComicBilibili cb = new ComicBilibili(code,today,commentCount == null ? 0 :commentCount);
        putModel(page,cb);

    }

    /**
     * 解析评论JSON hots：热门评论显示在前三个，replies：评论对象array，1pn:20count
     * https://api.bilibili.com/x/v2/reply?pn=1&type=1&oid=12965661&sort=0
     *
     * @param page
     */
    private void processReply(Page page) {
        Job oldJob = ((DelayRequest) page.getRequest()).getJob();
        String replyStr = page.getRawText();
        JSONObject object = JSONObject.parseObject(replyStr);
        if (object.getInteger("code") != 0) return;
        List<CommentText> commentTextList = new ArrayList<>();
        JSONObject replyObj = object.getJSONObject("data");
        //活跃的评论组3条
        JSONArray hotsArray = replyObj.getJSONArray("hots");
        if (null != hotsArray && hotsArray.size() > 0) hotsArray.forEach(obj -> {
            JSONObject hotsObj = (JSONObject) obj;
            CommentText commentText = generateCommentText(hotsObj);
            DbEntityHelper.derive(oldJob, commentText);
            commentTextList.add(commentText);
            JSONArray replies = hotsObj.getJSONArray("replies");
            if (null != replies && replies.size() > 0) {
                processReply(page, replies, commentTextList);
            }
        });
        //顶部的评论只有1条
        JSONObject topObj = replyObj.getJSONObject("top");
        if (null != topObj){
            CommentText topCommentText = generateCommentText(topObj);
            DbEntityHelper.derive(oldJob, topCommentText);
            commentTextList.add(topCommentText);
            JSONArray topReplies = topObj.getJSONArray("replies");
            if (null != topReplies && topReplies.size() > 0) {
                processReply(page, topReplies, commentTextList);
            }
        }
        //上顶的评论
        JSONObject upperTopObj = replyObj.getJSONObject("upper").getJSONObject("top");
        if (null != upperTopObj){
            CommentText upperTopCommentText = generateCommentText(upperTopObj);
            DbEntityHelper.derive(oldJob, upperTopCommentText);
            commentTextList.add(upperTopCommentText);
            JSONArray topReplies = upperTopObj.getJSONArray("replies");
            if (null != topReplies && topReplies.size() > 0) {
                processReply(page, topReplies, commentTextList);
            }
        }
        //一般的评论20条
        JSONArray replies = replyObj.getJSONArray("replies");
        if (null != replies && replies.size() > 0) {
            processReply(page, replies, commentTextList);
            createReply(page, replyObj.getJSONObject("page").getInteger("num") + 1);
        }
        if (CollectionUtils.isNotEmpty(commentTextList)) {
            putModel(page, commentTextList);
        }
        JSONObject pageJSONObject=replyObj.getJSONObject("page");
        Integer  commentsNum=Integer.parseInt(pageJSONObject.getString("acount"));

        CommentLog c=new CommentLog();
        DbEntityHelper.derive(oldJob,c);
        c.setCommentCount(commentsNum);
        putModel(page,c);

    }

    /**
     * 回复评论有可能有N级，所以使用递归解析
     *
     * @param page
     * @param array
     * @param commentTextList
     */
    private void processReply(Page page, JSONArray array, List<CommentText> commentTextList) {
        Job oldJob = ((DelayRequest) page.getRequest()).getJob();
        array.forEach(obj -> {
            JSONObject repliesObj = (JSONObject) obj;
            CommentText commentText = generateCommentText(repliesObj);
            DbEntityHelper.derive(oldJob, commentText);
            commentTextList.add(commentText);
            JSONArray repliesArray = repliesObj.getJSONArray("replies");
            if (null != repliesArray && repliesArray.size() > 0) {
                processReply(page, repliesArray, commentTextList);
            }
        });
    }

    /**
     * 解释评论JSON,生成评论文本对象
     *
     * @param obj
     * @return
     */
    private CommentText generateCommentText(JSONObject obj) {
        CommentText commentText = new CommentText();
        commentText.setCommentId(obj.getString("rpid"));
        commentText.setReplyCount(obj.getInteger("rcount"));
        commentText.setContent(obj.getJSONObject("content").getString("message"));
        commentText.setCreatedTime(new Timestamp(obj.getTimestamp("ctime").getTime() * 1000));
        commentText.setNickName(obj.getJSONObject("member").getString("uname"));
        commentText.setReplyCommentId(obj.getString("parent"));
        commentText.setUserId(obj.getString("mid"));
        return commentText;
    }

    /**
     * 生成抓取评论内容的job
     *
     * @param page
     */
    private void createReply(Page page, int pn) {
        Job oldJob = ((DelayRequest) page.getRequest()).getJob();
        String oldUrl = oldJob.getUrl();
        String id = RegexUtil.getDataByRegex(oldUrl, "/video/av(\\d*)/");
        String url = String.format(REPLY_URL, pn, id);
        Job replyJob = new Job(url);
        DbEntityHelper.derive(oldJob, replyJob);
        replyJob.setFrequency(FrequencyConstant.COMMENT_TEXT);
        putModel(page, replyJob);
    }


    public String day(){
        Date day=new Date();

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String dateString = df.format(day);

        return dateString;

    }

    private void processShowList(Page page){
        Job oldJob = ((DelayRequest) page.getRequest()).getJob();
        String showListStr = page.getRawText();
        JSONObject jsonObject = JSONObject.parseObject(showListStr);
        JSONArray showLisrArray = jsonObject.getJSONObject("result").getJSONObject("main_section").getJSONArray("episodes");
        List<String> special_code = Lists.newArrayList();
        for(int j=0;j<showLisrArray.size();j++){
            JSONObject episode=(JSONObject)showLisrArray.get(j);
            String aid=episode.getString("aid");
            String cid=episode.getString("cid");
            /**生成show*/
            Show show=new Show();
            String showName=episode.getString("long_title");
            show.setPlatformId(oldJob.getPlatformId());
            Pattern pattern=Pattern.compile("[\\u4e00-\\u9fa5]");
            String title=episode.getString("title");
            Matcher matcher1=pattern.matcher(title);
            String episodeStr=matcher1.replaceAll("");
            String code=episode.getString("id");
            if (StringUtils.isBlank(showName)){
                // 分集名字为空，用分集code代替
                showName =code;
            }
            show.setName(showName);
            boolean isNum = false;
            try {
                Integer.valueOf(episodeStr);
                isNum = true;
            } catch (NumberFormatException e) {
                isNum = false;
                special_code.add(code);   // 集数(中文，英文，2-1,) 转换失败，用下标替换
                //log.info("bili特殊剧集 code--> " + code + " 剧名--> " + showName + " title--> " + title );
            }
            try {
                show.setEpisode(Integer.parseInt(episodeStr));
            } catch (Exception e) {
                show.setEpisode(Integer.valueOf(special_code.indexOf(code)) + 1);
                //log.info(e.getMessage());
            }
            show.setCode(code);
            show.setDepth(2);
            show.setParentCode(oldJob.getCode());
            show.setUrl(String.format("www.bilibili.com/bangumi/play/ep%s",code));
            putModel(page,show);

            /**生成弹幕爬取任务**/
            Job barrageJob = new Job(String.format(COMMENT_URL,cid));
            DbEntityHelper.derive(oldJob, barrageJob);
            barrageJob.setFrequency(FrequencyConstant.BARRAGE_TEXT);//设置任务爬取频率
            barrageJob.setCode(code);
            putModel(page,barrageJob);

            /***生成爬取评论及评论人数job*/

            Job commentJob=new Job(String.format(REPLY_URL,1,aid));
            DbEntityHelper.derive(oldJob,commentJob);
            commentJob.setCode(code);
            putModel(page,commentJob);

        }
    }
    @Override
    public PageRule getPageRule() {
        return pageRule;
    }

    @Override
    public Site getSite() {
        return site;
    }
}
