package com.jinguduo.spider.spider.douban;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.Lists;

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
import com.jinguduo.spider.common.util.NumberHelper;
import com.jinguduo.spider.common.util.RegexUtil;
import com.jinguduo.spider.data.table.DoubanCommentsText;
import com.jinguduo.spider.data.table.DoubanLog;
import com.jinguduo.spider.data.table.ShowActors;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.selector.Html;

/**
 * Created by jack on 2017/7/17.
 */

@Slf4j
@Worker
public class DoubanMobileSpider extends CrawlSpider {

    private final static String CREDITS_URL = "https://m.douban.com/rexxar/api/v2/%s/%s/credits"; //影人接口
    private final static String NEW_M_MOVIE_COMMENTS_RUL = "https://m.douban.com/rexxar/api/v2/%s/%s/interests?count=20&order_by=latest&start=%s&ck=&for_mobile=1"; //短评接口

    private static final Integer NEXT_JOB_STEP = 20;
    
    private Site site = SiteBuilder.builder()
            .setDomain("m.douban.com")
            //.setCookieSpecs(CookieSpecs.IGNORE_COOKIES)
            .setUserAgent("Mozilla/5.0 (iPhone; CPU iPhone OS 10_0 like Mac OS X) AppleWebKit/602.1.38 (KHTML, like Gecko) Version/10.0 Mobile/14A300 Safari/602.1")
            .addHeader("Referer","https://m.douban.com/")
            .build();

    private PageRule pageRule = PageRule.build()
//            .add("/movie/subject/\\d+/", page -> themePage(page))
//            .add("/rexxar/api/v2/tv/\\d+/credits", page -> credits(page))
            .add("/rexxar/api/v2/.+/\\d+/interests", page -> comments(page));

    /**
     * 专题页面
     * @param page
     */
    private void themePage(Page page){
        Job oldJob = ((DelayRequest)page.getRequest()).getJob();

        Html html = page.getHtml();
        final int bad = -1;
        Double score = (double) bad; // 评分
        Integer judgerCount = bad; // 评分人数
        String ratingPepole = pick(html, "//p[@class='rating']//text()");
        if (ratingPepole != null) {
            judgerCount = NumberHelper.bruteParse(pick(html, "//p[@class='rating']/span[2]/text()"), 0);
            score = NumberHelper.bruteParse(pick(html, "//p[@class='rating']//strong/text()"), 0.0D);
        }
        // 讨论数（页面没有，需要请求接口）
        Integer disscussionCount = 0;

        // 影评/剧评数
        Integer reviewCount = NumberHelper.bruteParse(
                pick(html, "//section[@class='subject-reviews']/h2/text()"), bad);

        // 评论数（短评）
        Integer shortCommentsCount = NumberHelper.bruteParse(
                pick(html, "//section[@class='subject-comments']/h2/text()"), bad);

        // 全部分集短评数（M站页面没有）
        Integer allDiscussionCount = bad;

        // save douban data
        //插入到新表
        DoubanLog dlog = new DoubanLog();
        dlog.setVideoScore(new BigDecimal(score));
        dlog.setJudgerCount(judgerCount);
        dlog.setDiscussionCount(disscussionCount);
        dlog.setBriefComment(shortCommentsCount);
        dlog.setReviewCount(reviewCount);
        dlog.setAllPdBriefComment(allDiscussionCount);
        dlog.setCode(oldJob.getCode());
        putModel(page, dlog);

        //0是电视剧，1是电影
        String type = pick(html, "//section[@class='subject-intro']/h2/text()");
        if (type != null) {
            type = type.contains("剧集") ? "tv" : "movie";
        }

        this.createCreditsJob(page, type);
        this.createCommentJob(page, shortCommentsCount, type);
    }

    /**
     * 创建生成影人接口任务
     * @param page
     * @param type 电视剧/电影
     */
    private void createCreditsJob(Page page, String type){
        Job oldJob = ((DelayRequest)page.getRequest()).getJob();
        String oldUrl = oldJob.getUrl();
        String id = RegexUtil.getDataByRegex(oldUrl, "subject/(\\d*)/", 1);
        String url = String.format(CREDITS_URL, type, id);
        Job creditsJob = new Job(url);
        DbEntityHelper.derive(oldJob, creditsJob);
        creditsJob.setFrequency(FrequencyConstant.ACTOR_INFO);
        putModel(page, creditsJob);
    }

    /**
     * 生成爬取M站评论文门接口的job
     * @param page
     * @param shortCommentsCount 短评数
     * @param type 电视剧/电影
     */
    private void createCommentJob(Page page, Integer shortCommentsCount, String type){
        Job oldJob = ((DelayRequest)page.getRequest()).getJob();
        String oldUrl = oldJob.getUrl();
        String id = RegexUtil.getDataByRegex(oldUrl, "subject/(\\d*)/", 1);
        for (int start = 0; start < shortCommentsCount; start += 20){
            String url = String.format(NEW_M_MOVIE_COMMENTS_RUL, type, id, start);
            Job commentsJob = new Job(url);
            DbEntityHelper.derive(oldJob, commentsJob);
            commentsJob.setFrequency(FrequencyConstant.COMMENT_TEXT);
            putModel(page, commentsJob);
        }
    }

    /**
     * 影人接口
     * @param page
     */
    private void credits(Page page){
        Job job = ((DelayRequest)page.getRequest()).getJob();
        String text = page.getRawText();
        JSONObject jsonObject = JSONObject.parseObject(text);
        JSONArray credits = jsonObject.getJSONArray("credits");
        List<ShowActors> showActors = Lists.newArrayList();
        credits.forEach(obj -> {
            JSONObject credit = (JSONObject)obj;
            String title = credit.getString("title");
            JSONArray celebrities = credit.getJSONArray("celebrities");
            celebrities.forEach(sObj -> {
                JSONObject actor = (JSONObject)sObj;
                ShowActors showActor = new ShowActors();
                showActor.setSequence(showActors.size() + 1);
                showActor.setActorNameCn(actor.getString("name"));
                showActor.setActorNameEn(actor.getString("latin_name"));
                showActor.setRole(title);
                showActor.setCode(job.getCode());
                showActors.add(showActor);
            });
        });
        putModel(page, showActors);
    }

    /**
     * 短评接口
     * @param page
     */
    private void comments(Page page){
        
        Job oldJob = ((DelayRequest)page.getRequest()).getJob();
        String code = page.getUrl().regex(".*(tv|movie)\\/(\\d+)\\/interests\\?.*",2).get();
        String url = oldJob.getUrl();
        
        String text = page.getRawText();
        JSONObject jsonObject = JSONObject.parseObject(text);
        JSONArray interests = jsonObject.getJSONArray("interests");
        interests.forEach(obj -> {
            JSONObject interest = (JSONObject)obj;
            DoubanCommentsText dct = new DoubanCommentsText();
            dct.setCode(code);
            dct.setCommentId(interest.getLong("id"));
            dct.setContent(interest.getString("comment"));
            dct.setCreateTime(Timestamp.valueOf(interest.getString("create_time")));
            dct.setNickName(interest.getJSONObject("user").getString("name"));
            JSONObject ratingObj =  interest.getJSONObject("rating");
            if (null != ratingObj){
                dct.setStar(ratingObj.getInteger("value"));
            }else {
                dct.setStar(0);
            }
            dct.setType("P");
            dct.setUp(interest.getInteger("vote_count"));
            putModel(page, dct);
        });
        
        String nextJobUrl = getNextJobUrl(url);
        if(StringUtils.isNotBlank(nextJobUrl)){
            Job commentsJob = new Job(nextJobUrl);
            DbEntityHelper.derive(oldJob, commentsJob);
            commentsJob.setFrequency(FrequencyConstant.COMMENT_TEXT);
            putModel(page, commentsJob);
        }
    }
    
    private static String getNextJobUrl(String url){
        if(StringUtils.isBlank(url)||!StringUtils.contains(url, "short_comment_count")){
            //未携带约定参数，不能进行下一个任务生成
            return null;
        }
        Integer scc = 0;
        String shortCommentCount = RegexUtil.getDataByRegex(url, "short_comment_count=(\\d+)",1);
        Integer start = Integer.valueOf(RegexUtil.getDataByRegex(url, "start=(\\d+)",1));
        if(StringUtils.isNotBlank(shortCommentCount)&&NumberHelper.isNumeric(shortCommentCount)){
            scc = Integer.valueOf(shortCommentCount);
        }
        
        start = start+NEXT_JOB_STEP;
        if(scc!=0&&scc>start){
            return url.replaceAll("start=(\\d+)", "start="+start);
        }
        return null;
    }

    private String pick(Html html, String xpath) {
        try {
            return html.xpath(xpath).get();
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
        return "-1";
    }

    @Override
    public Site getSite() {
        return site;
    }

    @Override
    public PageRule getPageRule() {
        return pageRule;
    }
}
