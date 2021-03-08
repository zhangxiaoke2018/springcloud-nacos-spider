package com.jinguduo.spider.spider.fiction;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.constant.CommonEnum.FictionChannel;
import com.jinguduo.spider.common.constant.CommonEnum.FictionIncome;
import com.jinguduo.spider.common.constant.CommonEnum.Platform;
import com.jinguduo.spider.common.util.DateUtil;
import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.common.util.Md5Util;
import com.jinguduo.spider.data.table.*;
import com.jinguduo.spider.data.text.FictionCommentText;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.utils.UrlUtils;
import lombok.Data;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.http.client.config.CookieSpecs;
import org.springframework.util.StringUtils;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@SuppressWarnings("all")
@CommonsLog
@Worker
public class QDMobileSpider extends CrawlSpider {

    private static final String DETAIL_URL = "m.qidian.com\\/book\\/(\\d+)#channel-(\\d+)$";
    private static final Pattern PATTERN_DETAIL = Pattern.compile(DETAIL_URL);
    private static final Pattern PATTERN_WORD_LEN = Pattern.compile("(\\d+\\.\\d+|\\d+).*");
    private static final String COMMENT_FORMAT = "https://m.qidian.com/majax/book/getBookForum?_csrfToken=%s&bookId=%s";
    private static final String DETAIL_FORMAT = "https://m.qidian.com/book/%d#channel-%d";
    private static final String SCORE_URL = "https://book.qidian.com/info/%s";
    private static final String CATALOG_URL = "https://m.qidian.com/book/%s/catalog";
    private static final String COMMENT_LIST = "https://m.qidian.com/majax/book/getCommentList?_csrfToken=%s&bookId=%s&pageNum=%d";
    private static final String BOOK_FORUM_LIST = "https://m.qidian.com/majax/forum/getBookForumList?_csrfToken=%s&bookId=%s&pageNum=%d";
    private Site site = SiteBuilder.builder().setDomain("m.qidian.com").setCookieSpecs(CookieSpecs.IGNORE_COOKIES)
            .build();

    private PageRule rules = PageRule.build()
            .add("^https:\\/\\/m\\.qidian\\.com\\/rank\\/male$", this::processEntrance)
            .add("\\/majax\\/book\\/getBookForum", this::processComment)
            .add("\\/majax\\/rank\\/", this::processRank)
            .add("getCommentList", this::processCommentList)
            .add("getBookForumList", this::processForumList)
            .add(DETAIL_URL, this::processDetail)
            .add("/catalog", this::processChapter);

    private static final String RANK_RECOMMEND = "https://m.qidian.com/majax/rank/reclist?_csrfToken=%s&gender=%s&pageNum=%d&catId=-1&rankPeriod=%d";
    private static final String RANK_YUEPIAO = "https://m.qidian.com/majax/rank/yuepiaolist?_csrfToken=%s&gender=%s&pageNum=%d&catId=-1&yearmonth=%s";
    private static final String RANK_HOTSALE = "https://m.qidian.com/majax/rank/hotsaleslist?_csrfToken=%s&gender=%s&pageNum=%d&catId=-1";
    private static final String RANK_CLICK = "https://m.qidian.com/majax/rank/clicklist?_csrfToken=%s&gender=%s&pageNum=%d&catId=-1&rankPeriod=%d";
    private static final String RANK_NEWFANS = "https://m.qidian.com/majax/rank/newfanslist?_csrfToken=%s&gender=%s&pageNum=%s&catId=-1&rankPeriod=%d";
    private static final String RANK_SIGNLIST = "https://m.qidian.com/majax/rank/signlist?_csrfToken=%s&gender=%s&pageNum=%s&catId=-1";

    private static final String PATH_COVER = "https://qidian.qpic.cn/qdbimg/349573/%s/300";

    private static final String RANK_NEW_1 = "https://m.qidian.com/majax/rank/newbooklist?_csrfToken=%s&gender=%s&pageNum=%d&catId=%d";
    private static final String RANK_NEW_2 = "https://m.qidian.com/majax/rank/newauthorlist?_csrfToken=%s&gender=%s&pageNum=%d&catId=%d";
    private static final int[] CATEGORIES = {-1, 1, 2, 4, 5, 6, 7, 8, 9, 10, 12, 15, 21, 22};

    private static final Integer PAGESIZE = 20;

    public void processEntrance(Page page) {
        String male = "male";
        String female = "female";
        String token = getCSRFToken(page.getHeaders().get("set-cookie"));

        createRankJob(page, RANK_YUEPIAO, token, male);
        createRankJob(page, RANK_YUEPIAO, token, female);
        createRankJob(page, RANK_HOTSALE, token, male);
        createRankJob(page, RANK_HOTSALE, token, female);
        createRankJob(page, RANK_CLICK, token, male);
        createRankJob(page, RANK_CLICK, token, female);
        createRankJob(page, RANK_RECOMMEND, token, male);
        createRankJob(page, RANK_RECOMMEND, token, female);

        //粉丝榜
        createRankJob(page, RANK_NEWFANS, token, male);
        createRankJob(page, RANK_NEWFANS, token, female);

        //签约榜
        createRankJob(page, RANK_SIGNLIST, token, male);

        //新作榜不分类型抓取25页面
        createRankJob(page, RANK_NEW_1, token, male);

        createNewRankJob(page, RANK_NEW_1, token, male);
        createNewRankJob(page, RANK_NEW_2, token, male);
    }

    public void processRank(Page page) {
        String url = page.getUrl().get();
        String gender = page.getUrl().regex(".*\\&gender=(male|female).*", 1).get();
        Integer channelCode = gender.equals("male") ? FictionChannel.BOY.getCode() : FictionChannel.GIRL.getCode();
        JSONObject response = page.getJson().toObject(JSONObject.class);
        if (response != null && response.getInteger("code") == 0
                && response.getJSONObject("data").containsKey("records")) {
            JSONArray list = response.getJSONObject("data").getJSONArray("records");
            int size = list.size();
            if (size <= 0) {
                return;
            }

            //获取榜单排名，为防止意外影响主数据获取，整体try
            rankTypeResult rankTypeResult = new rankTypeResult();
            List<FictionOriginalBillboard> billboardList = new ArrayList<>();
            Date day = DateUtil.getDayStartTime(new Date());
            Integer pageNum = 1;
            rankTypeResult = this.getRankType(url);
            pageNum = rankTypeResult.getPageNum();


            JSONObject item;
            for (int j = 0; j < size; j++) {
                item = list.getJSONObject(j);
                Long bookId = item.getLong("bid");
                Fiction fiction = new Fiction();
                fiction.setCode(String.valueOf(bookId));
                fiction.setAuthor(item.getString("bAuth"));
                fiction.setName(item.getString("bName"));
                fiction.setIntro(item.getString("desc"));
                fiction.setTags(item.getString("cat"));
                fiction.setCover(String.format(PATH_COVER, String.valueOf(bookId)));
                String cnt = item.getString("cnt");
                if (cnt == null) {
                    fiction.setTotalLength(0);
                } else {
                    Matcher m = PATTERN_WORD_LEN.matcher(cnt);
                    if (m.find()) {
                        fiction.setTotalLength((int) (10000 * Float.valueOf(m.group(1))));
                    }
                }
                String state = item.getString("state");
                if (!StringUtils.isEmpty(state))
                    fiction.setIsFinish("连载中".equals(state) ? 0 : 1);
                fiction.setPlatformId(Platform.QI_DIAN.getCode());
                fiction.setChannel(channelCode);
                putModel(page, fiction);

                createDetailJob(page, String.format(DETAIL_FORMAT, bookId, channelCode));
                createDetailJob(page, String.format(SCORE_URL, bookId));


                //如果需要保存榜单
                if (rankTypeResult.getIsSave()) {
                    FictionOriginalBillboard billboard = new FictionOriginalBillboard();
                    billboard.setPlatformId(Platform.QI_DIAN.getCode());
                    billboard.setType(rankTypeResult.getRankType());
                    billboard.setDay(day);
                    billboard.setRank((pageNum - 1) * PAGESIZE + j + 1);
                    billboard.setCode(String.valueOf(bookId));
                    billboard.setBillboardUpdateTime(rankTypeResult.getBillboardUpdateTime());
                    billboardList.add(billboard);
                }
            }
            putModel(page, billboardList);

        }
    }


    private void createNewRankJob(Page page, String api, String token, String gender) {
        for (int catId : CATEGORIES) {
            for (int i = 1; i < 4; i++) {
                Job job = new Job(String.format(api, token, gender, i, catId));
                job.setPlatformId(Platform.QI_DIAN.getCode());
                job.setCode(Md5Util.getMd5(job.getUrl()));
                putModel(page, job);
            }
        }
    }


    private void createDetailJob(Page page, String link) {
        Job oldJob = ((DelayRequest) page.getRequest()).getJob();
        Job job = DbEntityHelper.derive(oldJob, new Job(link));
        job.setCode(Md5Util.getMd5(link));
        job.setPlatformId(Platform.QI_DIAN.getCode());
        putModel(page, job);
    }

    private void createRankJob(Page page, String api, String token, String channel) {
        Job oldJob = ((DelayRequest) page.getRequest()).getJob();
        if (RANK_YUEPIAO.equals(api)) {
            Calendar calendar = new GregorianCalendar();
            int year = calendar.get(Calendar.YEAR);
            int month = 1 + calendar.get(Calendar.MONTH);
            String yearMonth = String.valueOf(year) + (month < 10 ? "0" + month : String.valueOf(month));
            //25页
            for (int i = 1; i <= 25; i++) {
                Job job = new Job(String.format(RANK_YUEPIAO, token, channel, i, yearMonth));
                DbEntityHelper.derive(oldJob, job);
                job.setPlatformId(Platform.QI_DIAN.getCode());
                job.setCode(Md5Util.getMd5(job.getUrl()));
                putModel(page, job);
            }
            //25页
        } else if (RANK_HOTSALE.equals(api)) {
            for (int i = 1; i <= 25; i++) {
                Job job = new Job(String.format(RANK_HOTSALE, token, channel, i));
                DbEntityHelper.derive(oldJob, job);
                job.setPlatformId(Platform.QI_DIAN.getCode());
                job.setCode(Md5Util.getMd5(job.getUrl()));
                putModel(page, job);
            }
        } else if (RANK_CLICK.equals(api)) {
            for (int i = 0; i < 24; i++) {
                Job job = new Job(String.format(RANK_CLICK, token, channel, 1 + i / 2, 1 + i % 2));
                DbEntityHelper.derive(oldJob, job);
                job.setPlatformId(Platform.QI_DIAN.getCode());
                job.setCode(Md5Util.getMd5(job.getUrl()));
                putModel(page, job);
            }
            //30页
        } else if (RANK_RECOMMEND.equals(api)) {
            for (int i = 1; i <= 30; i++) {
                //j 控制榜单类型。1、总榜2、月榜 3、周榜
                for (int j = 1; j <= 3; j++) {
                    Job job = new Job(String.format(RANK_RECOMMEND, token, channel, i, j));
                    DbEntityHelper.derive(oldJob, job);
                    job.setPlatformId(Platform.QI_DIAN.getCode());
                    job.setCode(Md5Util.getMd5(job.getUrl()));
                    putModel(page, job);
                }
            }
            //10页
        } else if (RANK_NEWFANS.equals(api)) {
            for (int i = 1; i <= 10; i++) {
                //2.月榜。3.周榜
                for (int j = 2; j <= 3; j++) {
                    Job job = new Job(String.format(RANK_NEWFANS, token, channel, i, j));
                    DbEntityHelper.derive(oldJob, job);
                    job.setPlatformId(Platform.QI_DIAN.getCode());
                    job.setCode(Md5Util.getMd5(job.getUrl()));
                    putModel(page, job);
                }
            }
        } else if (RANK_SIGNLIST.equals(api)) {
            for (int i = 1; i <= 25; i++) {
                Job job = new Job(String.format(RANK_SIGNLIST, token, channel, i));
                DbEntityHelper.derive(oldJob, job);
                job.setPlatformId(Platform.QI_DIAN.getCode());
                job.setCode(Md5Util.getMd5(job.getUrl()));
                putModel(page, job);
            }
        } else if (RANK_NEW_1.equals(api)) {
            for (int i = 1; i <= 25; i++) {
                Job job = new Job(String.format(RANK_NEW_1, token, channel, i, -1));
                DbEntityHelper.derive(oldJob, job);
                job.setPlatformId(Platform.QI_DIAN.getCode());
                job.setCode(Md5Util.getMd5(job.getUrl()));
                putModel(page, job);
            }
        }
    }

    public void processDetail(Page page) {
        Matcher urlMather = PATTERN_DETAIL.matcher(page.getRequest().getUrl());
        urlMather.find();
        String bookId = urlMather.group(1);

        FictionIncomeLogs incomeLogs = new FictionIncomeLogs();
        incomeLogs.setCode(bookId);
        incomeLogs.setIncomeId(FictionIncome.QIDIAN_YUEPIAO.getCode());
        String ticketStr = page.getHtml().getDocument().getElementsByClass("month-ticket-cnt").get(0).text();
        if (StringUtils.hasText(ticketStr)) {
            incomeLogs.setIncomeNum(Integer.valueOf(ticketStr));
        }
        putModel(page, incomeLogs);

        String token = getCSRFToken(page.getHeaders().get("set-cookie"));
        if (StringUtils.hasText(token)) {
            createCommentJob(page, bookId, token);
        }

        createJob(page, String.format(CATALOG_URL, bookId));
    }

    private void createCommentJob(Page page, String bookId, String CSRFToken) {
        Job oldJob = ((DelayRequest) page.getRequest()).getJob();
        String url = String.format(COMMENT_FORMAT, CSRFToken, bookId);
        Job job = DbEntityHelper.derive(oldJob, new Job(url));
        job.setCode(Md5Util.getMd5(url));
        job.setPlatformId(Platform.QI_DIAN.getCode());
        putModel(page, job);
    }

    private void createCommentListJob(Page page, String bookId, String CSRFToken, Integer pageIndex) {
        String url = String.format(COMMENT_LIST, CSRFToken, bookId, pageIndex);
        createJob(page, url);
    }

    private void createForumListJob(Page page, String bookId, String CSRFToken, Integer pageIndex) {
        String url = String.format(BOOK_FORUM_LIST, CSRFToken, bookId, pageIndex);
        createJob(page, url);
    }

    private void createJob(Page page, String url) {
        Job job = new Job(url);
        job.setCode(Md5Util.getMd5(url));
        job.setPlatformId(Platform.QI_DIAN.getCode());
        putModel(page, job);
    }

    public void processComment(Page page) {
        JSONObject object = page.getJson().toObject(JSONObject.class);
        if (object != null && object.containsKey("data") && object.getJSONObject("data").containsKey("totalCnt")) {
            int commentCount = object.getJSONObject("data").getInteger("totalCnt");

            if (commentCount > 0) {
                String code = page.getUrl().regex(".*&bookId=(\\d+)$", 1).get();

                FictionCommentLogs commentLogs = new FictionCommentLogs();
                commentLogs.setCode(code);
                commentLogs.setPlatformId(Platform.QI_DIAN.getCode());
                commentLogs.setCommentCount(commentCount);
                putModel(page, commentLogs);
                String crsfToken = page.getUrl().regex(".*_csrfToken=(.*)&.*", 1).get();
                if (org.springframework.util.StringUtils.hasText(crsfToken)) {
                    createCommentListJob(page, code, crsfToken, 1);
                    createForumListJob(page, code, crsfToken, 1);
                }
            }
        }
    }

    public void processForumList(Page page) {
        JSONObject object = page.getJson().toObject(JSONObject.class);
        if (object != null && object.containsKey("data") && object.getJSONObject("data").containsKey("threadList")) {
            String code = page.getUrl().regex(".*&bookId=(\\d+)&.*", 1).get();
            JSONArray records = object.getJSONObject("data").getJSONArray("threadList");
            JSONObject item;
            List<FictionCommentText> comments = new ArrayList<>(records.size());
            for (int i = 0, size = records.size(); i < size; i++) {
                item = records.getJSONObject(i);
                String title = item.getString("title");
                if (title == null)
                    title = "";
                else
                    title = title.trim();
                String content = title + " " + item.getString("body").trim();
                content = content.replaceAll("\t|\r|\n", " ");
                if (StringUtils.hasText(content) && !"NULL".equalsIgnoreCase(content)) {
                    FictionCommentText commentText = new FictionCommentText();
                    commentText.setCode(code);
                    commentText.setContent(content);
                    commentText.setPlatformId(Platform.QI_DIAN.getCode());
                    commentText.setCommentRate(0f);
                    commentText.setUserName(item.getString("userName"));
                    commentText.setCreateTime(fixStrTime(item.getString("dateTime")));
                    commentText.setCommentId(item.getString("threadId"));
                    commentText.setReplyCount(0);
                    commentText.setChapterId("0");
                    comments.add(commentText);
                }

            }

            putModel(page, comments);

            String crsfToken = page.getUrl().regex(".*_csrfToken=(.*)&bookId=.*", 1).get();
            Integer pageIndex = object.getJSONObject("data").getInteger("pageNum");
            Integer totalCount = object.getJSONObject("data").getInteger("total");
            Integer pageSize = object.getJSONObject("data").getInteger("pageSize");
            Integer isLast = object.getJSONObject("data").getInteger("isLast");

            if (pageIndex % 5 == 1 && StringUtils.hasText(crsfToken) && isLast == 0 && pageIndex < 100) {
                for (int j = pageIndex + 1, jSize = pageIndex + Math.min(5, totalCount / pageSize + 1 - pageIndex); j <= jSize; j++)
                    createForumListJob(page, code, crsfToken, j);
            }
        }
    }

    public void processCommentList(Page page) {
        JSONObject object = page.getJson().toObject(JSONObject.class);
        if (object != null && object.containsKey("data") && object.getJSONObject("data").containsKey("records")) {
            String code = page.getUrl().regex(".*&bookId=(\\d+)&.*", 1).get();
            JSONArray records = object.getJSONObject("data").getJSONArray("records");
            JSONObject item;
            List<FictionCommentText> comments = new ArrayList<>(records.size());
            for (int i = 0, size = records.size(); i < size; i++) {
                item = records.getJSONObject(i);
                String content = item.getString("comment").replaceAll("\t|\r|\n", " ");
                if (StringUtils.hasText(content) && !"NULL".equalsIgnoreCase(content)) {
                    FictionCommentText commentText = new FictionCommentText();
                    commentText.setCode(code);
                    commentText.setContent(content);
                    commentText.setPlatformId(Platform.QI_DIAN.getCode());
                    commentText.setCommentRate(item.getFloat("star"));
                    commentText.setUserName(item.getString("nickName"));
                    commentText.setCreateTime(fixTime(item.getString("time")));
                    commentText.setCommentId(item.getString("rateId"));
                    commentText.setReplyCount(0);
                    commentText.setChapterId("0");
                    comments.add(commentText);
                }

            }

            if (!CollectionUtils.isEmpty(comments))
                putModel(page, comments);

            String crsfToken = page.getUrl().regex(".*_csrfToken=(.*)&bookId=.*", 1).get();
            Integer pageIndex = object.getJSONObject("data").getInteger("pageIndex");
            Integer totalCount = object.getJSONObject("data").getInteger("totalCnt");
            Integer pageSize = object.getJSONObject("data").getInteger("pageSize");
            Integer isLast = object.getJSONObject("data").getInteger("isLast");

            if (pageIndex % 5 == 1 && StringUtils.hasText(crsfToken) && isLast == 0 && pageIndex < 100) {
                for (int j = pageIndex + 1, jSize = pageIndex + Math.min(5, totalCount / pageSize + 1 - pageIndex); j <= jSize; j++)
                    createCommentListJob(page, code, crsfToken, j);
            }
        }
    }

    private Timestamp fixStrTime(String string) {
        // TODO Auto-generated method stub
        return new Timestamp(DateUtil.dateParse(string).getTime());
    }

    private Timestamp fixTime(String string) {
        // TODO Auto-generated method stub
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm");
        Timestamp stamp = new Timestamp(System.currentTimeMillis());
        try {
            stamp = new Timestamp(format.parse(string).getTime());
        } catch (ParseException e) {

        }
        return stamp;
    }

    private String getCSRFToken(List<String> cookies) {
        if (CollectionUtils.isEmpty(cookies))
            return "";

        List<String> cookieValues = cookies.stream().flatMap(s -> Arrays.asList(s.split(";")).stream())
                .collect(Collectors.toList());

        for (String cookie : cookieValues) {
            if (cookie.startsWith("_csrfToken=")) {
                return cookie.substring("_csrfToken=".length());
            }
        }

        return "";
    }

    private void processChapter(Page page) {
        String code = page.getUrl().regex(".*/book/(\\d+)/catalog$", 1).get();
        String catalog = page.getHtml().regex("g_data.volumes = ([^\n]+)").get();
        JSONArray json = JSONObject.parseArray(catalog.replace(';', ' '));

        int totalCount = 0;
        int freeCount = 0;
        JSONObject item;
        for (int i = 0, volumeCount = json.size(); i < volumeCount; i++) {
            JSONArray list = json.getJSONObject(i).getJSONArray("cs");
            for (int j = 0, listSize = list.size(); j < listSize; j++) {
                item = list.getJSONObject(j);
                if (item.getIntValue("sS") == 1)
                    freeCount++;
                totalCount++;
            }
        }


        FictionChapters chapter = new FictionChapters();
        chapter.setCode(code);
        chapter.setPlatformId(Platform.QI_DIAN.getCode());
        chapter.setIsVip(freeCount < totalCount ? 1 : 0);
        chapter.setFreeChapterCount(freeCount);
        chapter.setTotalChapterCount(totalCount);
        putModel(page, chapter);
    }

    /**
     * 根据url获取榜单类型(包括榜单类型、时间)
     * 月票榜 https://m.qidian.com/majax/rank/yuepiaolist?_csrfToken=rLiNuL9peuCr0UsRWjLMMmmL7MO6QBm3Y9Te5cIR&gender=male&pageNum=1&catId=-1&yearmonth=201906
     */

    private static Map<String, String> QD_RANK_TYPE_MAP = new HashMap<String, String>() {{
        put("hotsaleslist", "畅销榜");
        put("newbooklist", "新书榜");
        put("newfanslist", "粉丝榜");
        put("reclist", "推荐榜");
        put("signlist", "签约榜");
        put("yuepiaolist", "风云榜");
    }};
    private static Map<String, String> QD_RANK_SEX_MAP = new HashMap<String, String>() {{
        put("male", "男生");
        put("female", "女生");
    }};
    private static Map<String, String> QD_RANK_SCOPE_SEX_MAP = new HashMap<String, String>() {{
        put("weekly", "周榜");
        put("monthly", "月榜");
        put("total", "总榜");
    }};

    private rankTypeResult getRankType(String url) {
        String rankType = org.apache.commons.lang3.StringUtils.substring(url, org.apache.commons.lang3.StringUtils.indexOf(url, "/rank/") + 6, org.apache.commons.lang3.StringUtils.indexOf(url, "?"));
        String finalType = QD_RANK_TYPE_MAP.get(rankType);


        Map<String, String> allParam = UrlUtils.getAllParam(url);
        String gender = allParam.get("gender");
        gender = QD_RANK_SEX_MAP.get(gender);
        if (org.apache.commons.lang3.StringUtils.isNotEmpty(gender)) {
            finalType += ("_" + gender);
        }

        String pageNum = allParam.get("pageNum");
        //j 控制榜单类型。1、总榜 total 2、月榜 monthly 3、周榜 weekly rankPeriod
        rankTypeResult result = new rankTypeResult();
        result.setPageNum(Integer.parseInt(pageNum));
        result.setIsSave(true);

        String rankPeriod;
        switch (rankType) {
            case "yuepiaolist":
                String yearmonth = allParam.get("yearmonth") + "01";
                Date billboardUpdateTime = null;
                try {
                    billboardUpdateTime = DateUtils.parseDate(yearmonth, "yyyyMMdd");
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                result.setBillboardUpdateTime(billboardUpdateTime);
                break;
            case "hotsaleslist":
                break;
            case "reclist":
                rankPeriod = allParam.get("rankPeriod");
                rankPeriod = rankPeriod.equals("1") ? "total" : rankPeriod.equals("2") ? "monthly" : rankPeriod.equals("3") ? "weekly" : "unknown";
                rankPeriod = QD_RANK_SCOPE_SEX_MAP.get(rankPeriod);
                if (org.apache.commons.lang3.StringUtils.isNotEmpty(rankPeriod)) {
                    finalType += ("_" + rankPeriod);
                }
                break;
            case "newfanslist":
                rankPeriod = allParam.get("rankPeriod");
                rankPeriod = rankPeriod.equals("1") ? "total" : rankPeriod.equals("2") ? "monthly" : rankPeriod.equals("3") ? "weekly" : "unknown";
                rankPeriod = QD_RANK_SCOPE_SEX_MAP.get(rankPeriod);
                if (org.apache.commons.lang3.StringUtils.isNotEmpty(rankPeriod)) {
                    finalType += ("_" + rankPeriod);
                }
                break;
            case "signlist":
                break;
            case "newbooklist":
                String catId = allParam.get("catId");
                result.setIsSave((null != catId && "-1".equals(catId)));
                break;
            default:
                result.setIsSave(false);
                break;
        }
        result.setRankType(finalType);
        return result;

    }

    @Data
    class rankTypeResult {
        private String rankType;
        private Boolean isSave = false;
        private Integer pageNum;
        private Date billboardUpdateTime;
    }

    @Override
    public Site getSite() {
        return site;
    }

    @Override
    public PageRule getPageRule() {
        return rules;
    }
}
