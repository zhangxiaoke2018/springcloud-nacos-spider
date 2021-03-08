package com.jinguduo.spider.spider.fiction;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.constant.CommonEnum.FictionChannel;
import com.jinguduo.spider.common.constant.CommonEnum.Platform;
import com.jinguduo.spider.common.util.DateUtil;
import com.jinguduo.spider.common.util.Md5Util;
import com.jinguduo.spider.common.util.NumberHelper;
import com.jinguduo.spider.common.util.RegexUtil;
import com.jinguduo.spider.data.table.*;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.utils.UrlUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

@Worker
public class QQReaderSpider extends CrawlSpider {

    private static Map<String, String> RANK_ACTIONID_TYPE_MAP = new HashMap<String, String>() {{
        put("501192", "风云榜_月榜_男");
        put("501193", "风云榜_月榜_女");
        put("500268", "畅销榜_周榜_男");
        put("500271", "畅销榜_周榜_女");
        put("500417", "推荐榜_周榜_男");
        put("500420", "推荐榜_周榜_女");
    }};

    private Site site = new SiteBuilder().setDomain("newandroid.reader.qq.com")
            .addSpiderListener(new QQReaderHeaderListener()).build();

    private static final String rankUrl = "http://newandroid.reader.qq.com/v6_6_6/listDispatch?action=rank&pagestamp=%d&plan=1%s";
    private static final String detailUrl = "http://newandroid.reader.qq.com/v6_6_6/nativepage/book/detail?bid=%s&actionTag=%s";
    private static final String coverUrl = "http://wfqqreader-1252317822.image.myqcloud.com/cover/%d/%d/t3_%d.jpg";
    private static final String categoryUrl = "http://newandroid.reader.qq.com/v6_6_6/queryOperation?categoryFlag=%d";
    private static final String categoryListUrl = "http://newandroid.reader.qq.com/v6_6_6/listDispatch?actionTag=,-1,-1,-1,-1,10&actionId=%d&action=categoryV3&pagestamp=%d";

    String[] rankParams = {"&actionTag=girl&actionId=501193", "&actionTag=girl&actionId=500271",
            "&actionTag=girl&actionId=500289", "&actionTag=girl&actionId=500280", "&actionTag=girl&actionId=500429",
            "&actionTag=girl&actionId=500420", "&actionTag=girl&actionId=500309", "&actionTag=girl&actionId=502257",

            "&actionTag=boy&actionId=501192", "&actionTag=boy&actionId=500268", "&actionTag=boy&actionId=500286",
            "&actionTag=boy&actionId=500277", "&actionTag=boy&actionId=500306", "&actionTag=boy&actionId=500426",
            "&actionTag=boy&actionId=500417", "&actionTag=boy&actionId=501275", "&actionTag=boy&actionId=502256"};
    private PageRule rule = PageRule.build()
            .add("^http:\\/\\/newandroid\\.reader\\.qq\\.com\\/v6_6_6\\/listDispatch$", this::processEntrance)
            .add("categoryFlag=", this::processCategoryEntrance).add("action=categoryV3", this::processCategory)
            .add("action=rank", this::processRank).add("/book/detail", this::processDetail);


    private void processEntrance(Page page) {
        createRankJobs(page);
        createJob(page, String.format(categoryUrl, 1));
        createJob(page, String.format(categoryUrl, 2));
    }

    private void processCategoryEntrance(Page page) {
        JSONObject data = page.getJson().toObject(JSONObject.class);

        JSONArray categories = null;

        if (data.containsKey("girlCategoryList")) {
            categories = data.getJSONArray("girlCategoryList");
        } else if (data.containsKey("boyCategoryList")) {
            categories = data.getJSONArray("boyCategoryList");
        }

        if (categories == null)
            return;

        for (int i = 0, size = categories.size(); i < size; i++) {
            int actionId = categories.getJSONObject(i).getIntValue("actionId");
            if (actionId == 20076)
                continue;
            for (int j = 1; j < 17; j++) {
                createJob(page, String.format(categoryListUrl, actionId, j));
            }
        }
    }

    private void processCategory(Page page) {
        JSONObject data = page.getJson().toObject(JSONObject.class);
        int type = data.getJSONObject("info").getIntValue("type");
        JSONArray books = data.getJSONArray("bookList");

        for (int i = 0, size = books.size(); i < size; i++) {
            String bookId = books.getJSONObject(i).getString("bid");
            createJob(page, String.format(detailUrl, bookId, type == 2 ? "girl" : "boy"));
        }
    }

    private void createRankJobs(Page page) {
        for (String rankParam : rankParams) {
            for (int i = 1; i < 6; i++) {
                createJob(page, String.format(rankUrl, i, rankParam));
            }
        }
    }


    //风云榜_月榜_男 501192			风云榜_月榜_女 501193
    //畅销榜_周榜_男 500268			畅销榜_周榜_女 500271
    //推荐榜_周榜_男 500417			推荐榜_周榜_女 500420
    //RANK_ACTIONID_TYPE_MAP
    private void processRank(Page page) {
        String actionTag = page.getUrl().regex(".*actionTag=(girl|boy).*").get();
        JSONObject data = page.getJson().toObject(JSONObject.class);
        if (data.containsKey("bookList")) {
            JSONArray bookList = data.getJSONArray("bookList");
            for (int i = 0, size = bookList.size(); i < size; i++) {
                String bookId = bookList.getJSONObject(i).getString("bid");
                createJob(page, String.format(detailUrl, bookId, actionTag));
            }
        }

        String url = page.getUrl().get();

        String actionId = UrlUtils.getParam(url, "actionId");
        String pageNum = UrlUtils.getParam(url, "pagestamp");
        Integer beforeSize = (Integer.parseInt(pageNum) - 1) * 20;
        String rankType = RANK_ACTIONID_TYPE_MAP.get(actionId);
        if (!StringUtils.isEmpty(rankType)) {
            List<FictionOriginalBillboard> billboards = new ArrayList<>();
            Date day = DateUtil.getDayStartTime(new Date());
            JSONArray bookList = data.getJSONArray("bookList");
            List<FictionOriginalBillboard> billboardList = new ArrayList<>();
            for (int i = 0; i < bookList.size(); i++) {
                JSONObject book = (JSONObject) bookList.get(i);
                String bid = book.getString("bid");
                FictionOriginalBillboard billboard = new FictionOriginalBillboard();
                billboard.setPlatformId(Platform.QQ_READER.getCode());
                billboard.setType(rankType);
                billboard.setDay(day);
                billboard.setRank(beforeSize + i + 1);
                billboard.setCode(String.valueOf(bid));
                billboardList.add(billboard);
            }

            putModel(page, billboardList);


        }

    }

    private String getCoverUrl(String bookId) {
        long bid = Long.valueOf(bookId);
        return String.format(coverUrl, bid % 1000, bid, bid);
    }

    private void processDetail(Page page) {
        String channel = page.getUrl().regex(".*actionTag=(girl|boy).*").get();

        JSONObject data = page.getJson().toObject(JSONObject.class);
        if (!data.containsKey("introinfo"))
            return;
        JSONObject intro = data.getJSONObject("introinfo");
        Fiction fictionInfo = new Fiction();
        if (intro.containsKey("book")) {
            JSONObject book = intro.getJSONObject("book");
            fictionInfo.setAuthor(book.getString("author"));
            fictionInfo
                    .setChannel("girl".equals(channel) ? FictionChannel.GIRL.getCode() : FictionChannel.BOY.getCode());
            fictionInfo.setCode(book.getString("id"));
            fictionInfo.setCover(getCoverUrl(book.getString("id")));
            fictionInfo.setIntro(book.getString("intro").trim());
            fictionInfo.setIsFinish(book.getInteger("finished"));
            fictionInfo.setName(book.getString("title"));
            fictionInfo.setPlatformId(Platform.QQ_READER.getCode());
            fictionInfo.setTags(book.getString("categoryname"));
            fictionInfo.setTotalLength(book.getInteger("totalwords"));
            putModel(page, fictionInfo);

            if (intro.containsKey("scoreInfo")) {
                JSONObject scoreInfo = intro.getJSONObject("scoreInfo");
                // "scoreInfo":{"score":"4.5","intro":"(25.7万人评)","scoretext":"9.3"}
                FictionPlatformRate rate = new FictionPlatformRate();
                rate.setCode(fictionInfo.getCode());
                rate.setPlatformId(Platform.QQ_READER.getCode());
                rate.setRate(scoreInfo.getFloat("scoretext"));
                String rateUser = RegexUtil.getDataByRegex(scoreInfo.getString("intro"), "\\((.*)人评\\)");
                if (rateUser != null)
                    rate.setUserCount((int) NumberHelper.parseShortNumber(rateUser, 0));
                putModel(page, rate);
            }

            if (intro.containsKey("statisticInfo")) {
                JSONArray statisticInfo = intro.getJSONArray("statisticInfo");
                JSONObject statisticItem;
                for (int i = 0; i < statisticInfo.size(); i++) {
                    statisticItem = statisticInfo.getJSONObject(i);
                    if ("收藏".equals(statisticItem.getString("name"))) {
                        int favoriteCount = (int) NumberHelper.parseShortNumber(statisticItem.getString("number"), 0);
                        FictionPlatformFavorite favorite = new FictionPlatformFavorite();
                        favorite.setCode(fictionInfo.getCode());
                        favorite.setFavoriteCount(favoriteCount);
                        favorite.setPlatformId(Platform.QQ_READER.getCode());
                        putModel(page, favorite);
                    } else if ("读过".equals(statisticItem.getString("name"))) {
                        long clickCount = NumberHelper.parseShortNumber(statisticItem.getString("number"), 0);
                        FictionPlatformClick click = new FictionPlatformClick();
                        click.setCode(fictionInfo.getCode());
                        click.setClickCount(clickCount);
                        click.setPlatformId(Platform.QQ_READER.getCode());
                        putModel(page, click);
                    }
                }
            }

        }

        FictionCommentLogs commentLog = new FictionCommentLogs();
        int commentCount = data.getJSONObject("commentinfo").getIntValue("commentcount");
        commentLog.setCode(fictionInfo.getCode());
        commentLog.setCommentCount(commentCount);
        commentLog.setPlatformId(Platform.QQ_READER.getCode());
        putModel(page, commentLog);
    }

    private void createJob(Page page, String url) {
        Job job = new Job(url);
        job.setPlatformId(Platform.QQ_READER.getCode());
        job.setCode(Md5Util.getMd5(url));
        putModel(page, job);
    }

    @Override
    public Site getSite() {
        // TODO Auto-generated method stub
        return site;
    }

    @Override
    public PageRule getPageRule() {
        // TODO Auto-generated method stub
        return rule;
    }

}
