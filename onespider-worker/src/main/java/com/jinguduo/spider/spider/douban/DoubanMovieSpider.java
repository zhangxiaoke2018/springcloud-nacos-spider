package com.jinguduo.spider.spider.douban;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jdk.nashorn.internal.ir.IfNode;
import jdk.nashorn.internal.ir.annotations.Ignore;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.Lists;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;

import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.spider.listener.UserAgentSpiderListener;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.constant.FrequencyConstant;
import com.jinguduo.spider.common.exception.AntiSpiderException;
import com.jinguduo.spider.common.type.SequenceCounterQuota;
import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.common.util.NumberHelper;
import com.jinguduo.spider.common.util.RegexUtil;
import com.jinguduo.spider.data.table.DouBanActor;
import com.jinguduo.spider.data.table.DouBanShow;
import com.jinguduo.spider.data.table.DoubanCommentsText;
import com.jinguduo.spider.data.table.DoubanLog;
import com.jinguduo.spider.data.table.ShowActors;
import com.jinguduo.spider.service.QiniuService;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.selector.Html;
import com.jinguduo.spider.webmagic.selector.Selectable;

@Slf4j
@Worker
public class DoubanMovieSpider extends CrawlSpider {

    private final static Integer INTEGER_ZERO = Integer.valueOf(0);

    private final static Integer INTEGER_TWENTY = Integer.valueOf(20);

    private final static String NEW_PAGIN_PARAM_URL = "?sort=time&status=%s&start=%s&limit=20";//????????????

    private final static String HOT_PAGIN_PARAM_URL = "?sort=new_score&status=%s&start=%s&limit=20";//????????????

	private final static String NEW_M_MOVIE_COMMENTS_RUL = "https://m.douban.com/rexxar/api/v2/movie/%s/interests?count=20&order_by=time&start=%s&ck&for_mobile=1"; // ????????????
	private final static String NEW_M_TV_COMMENTS_RUL = "https://m.douban.com/rexxar/api/v2/tv/%s/interests?count=20&order_by=time&start=%s&ck&for_mobile=1"; // ???????????????

    private final static List<String> SPECIAL_CODE = Arrays.asList("27187021", "7053738", "10563794", "25931446");

    @Autowired
    private QiniuService qiniuService;

    private Site site = SiteBuilder.builder().setDomain("movie.douban.com")
            .addDownloaderListener(new DoubanRandomCookieDownloaderListener()
                    .addAbnormalStatusCode(403, 404, 500, 501, 503)
                    .setQuota(new SequenceCounterQuota(5))
                    .setProbability(0.2))
            // user-agent ?????????
            .addSpiderListener(new UserAgentSpiderListener()).build();

    // DouBan Movie main page process
    private PageRule rules = PageRule.build()
            .add("/subject/\\d+/$", page -> themePage(page))
            .add("/subject/\\d+/#gd-showname", page -> themePage(page))
            .add("/subject/\\d+/$", page -> getSubject(page))
            .add("/subject/\\d+/#gd-showname", page -> getSubject(page))
            //
            .add("celebrity", page -> getActorInfo(page))
            //??????
            .add("/subject/\\d+/comments\\?status=[A-Z]", page -> commentsPagin(page))//????????????????????????==>????????????!(sort=new_score) or ????????????!(sort=time)
            .add("celebrities", page -> celebrities(page));//?????? ????????????



    private void commentsPagin(Page page) {

        final Job mainJ = ((DelayRequest) page.getRequest()).getJob();
        final String comment_prefix_url = page.getUrl().regex("(.*?)\\?",1).get();
        final Html html = page.getHtml();
        //?????????????????????
        final String[] types = {""};
        html.xpath("//ul[@class='CommentTabs']/li")
                .nodes().stream()
                .forEach(selectable -> {
                    try {
                        if (selectable.regex("is-active").match()) {//????????????
                            Pair<Integer,String> numType = commentTypeAndNum(selectable.xpath("///span/text()"));
                            types[0] = numType.getSecond();
                            if (INTEGER_ZERO.compareTo(numType.getFirst()) < 0) {//?????????
                                String status = numType.getSecond();
                                if (INTEGER_TWENTY.compareTo(numType.getFirst()) < 0) {//???????????????
                                    Integer total = numType.getFirst();
                                    for (int i = 0; i<=total; i+=20) {
                                        putModel(page,
                                                DbEntityHelper.derive( mainJ,
                                                        new Job(comment_prefix_url + String.format(NEW_PAGIN_PARAM_URL, status, i)
                                                ))
                                        );
                                    }
                                }
                            }
                        } else {//?????????????????????????????? ==> "???????????????"
                            if (StringUtils.isNotBlank(selectable.xpath("///a/text()").regex("(\\d+)",1).get()) &&
                                    !StringUtils.equals("0", selectable.xpath("///a/text()").regex("(\\d+)",1).get())){
                                putModel(page,
                                        DbEntityHelper.derive(mainJ,
                                                new Job(selectable.xpath("///a/@href").get())
                                        )
                                );
                            }
                        }
                    } catch (Exception e) {
                        log.error("douban comment error url: {}", page.getRequest().getUrl(), e);
                    }
                });
        if (StringUtils.isBlank(types[0])){
            return;
        }
        this.comments(page);//???????????????????????????
    }

    /**
     * ??????</br>
     * ??????</br>
     * @param page
     */
    private void comments(Page page) {

        Job mainJ = ((DelayRequest) page.getRequest()).getJob();

        String code = mainJ.getCode();
        Html html = page.getHtml();

        //?????????????????????
        String[] type = {""};
        html.xpath("//ul[@class='CommentTabs']/li")
                .nodes().stream()
                .forEach(selectable -> {
                    try {
                        if (selectable.regex("is-active").match()) {//????????????
                            type[0] = commentType(selectable.xpath("///span/text()"));
                        }
                    } catch (Exception e) {
                        log.error("douban comment error url: {}",page.getUrl(), e);
                    }
                });
        if (StringUtils.isBlank(type[0])){
            return;
        }

        //????????????
        html.xpath("//div[@class='comment-item']")
                .nodes().stream()
                .forEach(selectable -> {
                    try {
                        Selectable commentS = selectable.xpath("///div[@class='comment']");
                        DoubanCommentsText dct = new DoubanCommentsText();
                        dct.setCode(code);
                        dct.setCommentId(Long.valueOf(selectable.xpath("///@data-cid").get()));
                        dct.setContent(commentS.xpath("///p/text()").get());
                        dct.setCreateTime(Timestamp.valueOf(commentS.xpath("///h3/span[@class='comment-info']/span[@class='comment-time']/@title").get()));
                        dct.setNickName(commentS.xpath("///h3/span[@class='comment-info']/a/text()").get());
                        dct.setStar(NumberHelper.parseInt(commentS.xpath("///h3/span[@class='comment-info']/span[@class='rating']/@class").regex("allstar(\\d+)",1).get(),0));
                        dct.setType(type[0]);
                        dct.setUp(NumberHelper.parseInt(commentS.xpath("///h3/span[@class='comment-vote']/span[@class='votes']/text()").get(),0));
                        putModel(page, dct);
                    } catch (Exception e) {
                        log.error("douban comment content error url :{}",page.getUrl(), e);
                    }
                });
    }

    /***
     * get comment status
     * ep: "F" & "P"
     * @param xpath
     * @return status
     * @throws Exception
     */
    private String commentType(Selectable xpath) throws Exception {
        String type = null;
        // 1:?????? 2:?????? 3:??????
        if (xpath.regex("??????").match()) {
            type = "F";
        } else if (xpath.regex("??????").match()) {//??????????????????
            type = "F";
        } else if (xpath.regex("??????").match()) {
            type = "P";
        }
        return type;
    }

    /***
     * get comment status and comment count
     * ep: ?????????12378??? ?????????213???
     * @param xpath
     * @return Pair (first:count & second:status)
     * @throws Exception
     */
    private Pair<Integer,String> commentTypeAndNum(Selectable xpath) throws Exception {
        return Pair.of(
                NumberHelper.parseInt(xpath.regex("(\\d+)",1).get(),0),
                commentType(xpath)
        );
    }

    /***
     * @param page
     * @Title ??????????????????????????????
     */
    private void themePage(Page page) {
        Job job = ((DelayRequest) page.getRequest()).getJob();
        String url = page.getRequest().getUrl();
        final Html html = page.getHtml();

        // ??????????????????&????????????,????????????????????????
        Document document = html.getDocument();
        Element celebrities = document.getElementById("celebrities");

        if(celebrities != null){
            String tmpUrl = url;

            if (url.indexOf("#") != -1){ // ??????????????????
                tmpUrl = tmpUrl.substring(0, url.indexOf("#"));
            }

            tmpUrl += "celebrities";
            Job celebritiesJob = new Job(tmpUrl);
            DbEntityHelper.derive(job, celebritiesJob);
//            celebritiesJob.setFrequency(FrequencyConstant.ACTOR_INFO);
            putModel(page, celebritiesJob);
            //log.info("create douban actorsJob: Code ---->"+ job.getCode());
        }

            final int bad = -1;

            Double score = (double) bad; // ??????
            Integer judgerCount = bad; // ????????????

            String ratingPepole = pick(html, "//div[@class='rating_sum']//text()");
            if (ratingPepole != null) {
                judgerCount = NumberHelper.bruteParse(pick(html, "//div[@class='rating_sum']//span/text()"), 0);
                score = NumberHelper.bruteParse(pick(html, "//div[@id='interest_sectl']//strong/text()"), 0.0D);
            }
//            // print log, check exception
//            if(job.getCode().equals("26939233")){
//                log.info("????????????: "+ score +  " ??????????????????: " + judgerCount );
//            }
            // ????????????(?????????????????????)
            Integer disscussionCount = NumberHelper.bruteParse(pick(html, "//div[@class='discussion-list']/a/text()"),
                    0);
            // ?????????
            Integer reviewCount = NumberHelper.bruteParse(
                    pick(html, "//section[contains(@class, 'reviews')]//span[@class='pl']/a/text()"), bad);
            // ?????????????????????
            Integer shortCommentsCount = NumberHelper.bruteParse(
                    pick(html, "//div[@id='comments-section']//span[@class='pl']/a/text()"), bad);
            // ?????????????????????
            String discussion = pick(html, "//*[@class='discussion_link']/a/text()");
            Integer allDiscussionCount = bad;
            if (discussion != null) {
                // ??????????????????????????????????????????bruteParse?????????????????????????????????
                discussion = discussion.substring(discussion.lastIndexOf("?????????"), discussion.lastIndexOf("??????"));
                allDiscussionCount = NumberHelper.bruteParse(discussion, bad);
            }
            //????????????
            List<String> scoreStrList = new ArrayList<>();
            scoreStrList = html.xpath("//div[@class='ratings-on-weight']//div[@class='item']//span[@class='rating_per']/text()").all();
            Integer scoreFiveNum = scoreStrList.size() == 0 ? null : new Double(Double.valueOf(StringUtils.replace(scoreStrList.get(0), "%", ""))*100).intValue();
            Integer scoreFourNum = scoreStrList.size() == 0 ? null :  new Double(Double.valueOf(StringUtils.replace(scoreStrList.get(1), "%", ""))*100).intValue();
            Integer scoreThreeNum = scoreStrList.size() == 0 ? null :  new Double(Double.valueOf(StringUtils.replace(scoreStrList.get(2), "%", ""))*100).intValue();
            Integer scoreTwoNum = scoreStrList.size() == 0 ? null :  new Double(Double.valueOf(StringUtils.replace(scoreStrList.get(3), "%", ""))*100).intValue();
            Integer scoreOneNum = scoreStrList.size() == 0 ? null :  new Double(Double.valueOf(StringUtils.replace(scoreStrList.get(4), "%", ""))*100).intValue();

            // save douban data
            //???????????????
            DoubanLog dlog = new DoubanLog();
            dlog.setVideoScore(new BigDecimal(score));
            dlog.setJudgerCount(judgerCount);
            dlog.setDiscussionCount(disscussionCount);
            dlog.setBriefComment(shortCommentsCount);
            dlog.setReviewCount(reviewCount);
            dlog.setAllPdBriefComment(allDiscussionCount);
            dlog.setCode(job.getCode());
            //????????????
            dlog.setScore5Num(scoreFiveNum);
            dlog.setScore4Num(scoreFourNum);
            dlog.setScore3Num(scoreThreeNum);
            dlog.setScore2Num(scoreTwoNum);
            dlog.setScore1Num(scoreOneNum);
            putModel(page, dlog);
            //log.info("Code: " + dlog.getCode() + " ????????????: " + dlog.getVideoScore() + " ????????????: " + dlog.getJudgerCount());

            //???????????????????????????M???
            createCommentJob(page, shortCommentsCount, discussion);

        //??????????????????????????????????????????
//        if (doubanCache.importan(job.getCode())) {
//            putModel(page, this.commentJob(page));
//        }
    }

    /**
     * ????????????M????????????????????????job
     * @param page
     * @param shortCommentsCount ?????????
     * @param discussion ??????????????????title????????????null???????????????????????????null???????????????
     */
    private void createCommentJob(Page page, Integer shortCommentsCount, String discussion){
        Job oldJob = ((DelayRequest)page.getRequest()).getJob();
        String oldUrl = oldJob.getUrl();
        String baseUrl;
        if (discussion == null){
            baseUrl = NEW_M_MOVIE_COMMENTS_RUL;
        }else {
            baseUrl = NEW_M_TV_COMMENTS_RUL;
        }
        String id = RegexUtil.getDataByRegex(oldUrl, "subject/(\\d*)/", 1);
        
		for(int i=0,limitSize = Math.min(500, shortCommentsCount);i<limitSize;i+=20) {
			String url = String.format(baseUrl, id, i);
			Job commentsJob = new Job(url);
			DbEntityHelper.derive(oldJob, commentsJob);
			commentsJob.setFrequency(FrequencyConstant.COMMENT_TEXT);
			commentsJob.setCode(DigestUtils.md2Hex(url));
			putModel(page, commentsJob);
		}
    }

    private String pick(Html html, String xpath) {
        try {
            return html.xpath(xpath).get();
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
        return "-1";
    }

    // ???????????????
    private void getSubject(Page page) throws AntiSpiderException {
        Job oldJob = ((DelayRequest) page.getRequest()).getJob();
        // ?????????????????????
        DouBanShow show = DoubanHelper.getRawShowFromPage(page);
        if (StringUtils.isBlank(show.getName())) {
            //log.error("page can't get douban movie name from url : [" + show.getUrl() + "]");
            //return;
            throw new AntiSpiderException("Show Douban" + page.getRequest().getUrl());
        }
        show.setShowId(oldJob.getShowId());
        String cover = show.getCover();
        if (StringUtils.isBlank(cover)) {
            log.warn("get bad cover : [" + cover + "] from url : [" + show.getUrl() + "]");
        } else {
            String uploadCover = qiniuService.upload(cover);
            if (StringUtils.isNotBlank(uploadCover)) {
                cover = uploadCover;
            }
        }
        show.setCover(cover);
        // ???????????????????????????job
        List<Job> actorJobs = DoubanHelper.getJobsFromActors(show.getActors(), oldJob);
        // ????????????job
        if (actorJobs != null) {
            putModel(page, actorJobs);
        }
        // ??????show
        putModel(page, show);
    }

    // ??????????????????
    private void getActorInfo(Page page) throws AntiSpiderException {
        String url = page.getUrl().toString();
        if (StringUtils.containsIgnoreCase(url, "movies")) {
            return;
        }
        DouBanActor actor = DoubanHelper.getRawActorFromPage(page);
        if (StringUtils.isBlank(actor.getName())) {
            //log.error("page can't get douban movie actor name from url : [" + actor.getUrl() + "]");
            //return;
            throw new AntiSpiderException("Actor " + page.getRequest().getUrl());
        }
        String cover = actor.getCover();
        if (StringUtils.isBlank(cover)) {
            log.warn("get bad cover : [" + cover + "] from url : [" + actor.getUrl() + "]");
        } else {
            String uploadCover = qiniuService.upload(cover);
            if (StringUtils.isNotBlank(uploadCover)) {
                cover = uploadCover;
            }
        }
        actor.setCover(cover);
        putModel(page, actor);

    }

    private void celebrities(Page page) {

        Job job = ((DelayRequest) page.getRequest()).getJob();

        //log.info("to_process_douban_celebrities url:{}",job.getUrl());

        Document document = page.getHtml().getDocument();

        List<ShowActors> showActors = Lists.newArrayList();

        Elements uls = document.getElementsByClass("list-wrapper");

        if(uls == null || uls.size() == 0){
            //log.debug("the douban_code:" + job.getCode() + " celebrities page maybe error! url:" + job.getUrl());
            return;
        }
        Elements lis = null;
        for (Element ul : uls) {
            String title = ul.getElementsByTag("h2").get(0).text();
            if(title.contains("??????")){
                lis = ul.getElementsByTag("li");
                break;
            }
        }
        //log.info("douban_celebrities elements listSize : "+lis.size()+".code+"+job.getUrl());
        if(lis == null){
            //????????????????????????
            return;
        }

        for (int i = 0; i < lis.size(); i++) {
            Element li = lis.get(i);
            ShowActors showActor = new ShowActors();
            showActor.setSequence(i + 1);
            Element actorEle = li.getElementsByTag("a").get(1);
            String href = actorEle.attr("href");
            String name = actorEle.text();
            showActor.setCelebrityCode(href.substring(href.indexOf("celebrity") + 10, href.length() -1));
            Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
            Matcher m = p.matcher(name);
            if(name.indexOf(" ") == -1){
                showActor.setActorNameCn(name);
            }else{
                if(m.find()){
                    showActor.setActorNameCn(name.substring(0, name.indexOf(" ")));
                    showActor.setActorNameEn(name.substring(name.indexOf(" "),name.length()).trim());
                }else{
                    showActor.setActorNameCn(name.trim().replaceAll(" ",""));
                    showActor.setActorNameEn(name);
                }
            }
            Elements roleEle = li.getElementsByClass("role");
            if(roleEle != null && roleEle.size() != 0){
                String role = roleEle.get(0).text();
                String roleName = "";
                roleName =role.substring(role.indexOf(" "), role.length());
                if(roleName.contains("Actress") || roleName.contains("Actor") || roleName.contains("Voice") ) {
                    if(roleName.contains(")")) {
                        roleName = role.substring(role.lastIndexOf(" "), role.length() - 1).trim();
                    }else{
                        roleName = role.substring(role.lastIndexOf(" "), role.length()).trim();
                    }
                }
                if(roleName.contains("Actor") || roleName.contains("Actress") || roleName.contains("Actor/Actress") || roleName.contains("Voice")){
                    showActor.setRole("");
                }else {
                    showActor.setRole(roleName);
                }
            }

            String style = li.getElementsByClass("avatar").get(0).attr("style");
            String cover = style.substring(style.lastIndexOf("url") + 4, style.length()-1 );

            String upload = qiniuService.upload(cover);
            if(StringUtils.isNotBlank(upload)){
                showActor.setCover(upload);
            }

            showActor.setCode(job.getCode());
            showActors.add(showActor);
        }
        //log.info("douban actors list code :" + job.getCode() +"actorlistSize :" + showActors.size());
        putModel(page, showActors);
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
