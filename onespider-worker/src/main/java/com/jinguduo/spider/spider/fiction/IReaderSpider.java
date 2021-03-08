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
public class IReaderSpider extends CrawlSpider {
    private Site site = new SiteBuilder().setDomain("ah2.zhangyue.com").build();

    private static final String categoryUrl = "http://ah2.zhangyue.com/zybk/api/category/category";
    private static final String categoryListUrl = "http://ah2.zhangyue.com/zybk/api/category/index?act=filter&categoryType=%s&order=download&hasFilterInfo=0&status=0&order=download&categoryId=%d&page=%d&pageSize=100&p3=17080003";
    private static final String rankUrl = "http://ah2.zhangyue.com/zybk/api/rank/books?currentPage=%d&pageSize=100%s";
    private static final String detailUrl = "http://ah2.zhangyue.com/zybk/api/detail/index?bid=%s&pk=RANK%s";
    private static final String webDetailUrl = "http://www.ireader.com/index.php?ca=bookdetail.index&bid=%s";
    private static String[] rankParams = {"&sectionId=29416&type=normal&style=NEW-STYLE137&freqKey=ch_male_rank",
            "&sectionId=29418&type=normal&style=NEW-STYLE137&freqKey=ch_male_rank",
            "&sectionId=29417&type=normal&style=NEW-STYLE137&freqKey=ch_male_rank",
            "&sectionId=19276&type=normal&style=style1&freqKey=ch_male_rank",
            "&sectionId=19277&type=normal&style=style1&freqKey=ch_male_rank",
            "&sectionId=1&type=ticket&style=NEW-STYLE174&freqKey=ch_male_rank",
            "&sectionId=18341&type=normal&style=NEW-STYLE137&freqKey=ch_male_rank",
            "&sectionId=32736&type=normal&style=NEW-STYLE137&freqKey=ch_female_rank",
            "&sectionId=3&type=ticket&style=NEW-STYLE175&freqKey=ch_female_rank",
            "&sectionId=14372&type=normal&style=style1&freqKey=ch_female_rank",
            "&sectionId=18318&type=normal&style=style1&freqKey=ch_female_rank",
            "&sectionId=29421&type=normal&style=NEW-STYLE137&freqKey=ch_female_rank",
            "&sectionId=29420&type=normal&style=NEW-STYLE137&freqKey=ch_female_rank",
            "&sectionId=29419&type=normal&style=NEW-STYLE137&freqKey=ch_female_rank"};

    private static Map<String, String> SECTIONID_RANK_TYPE_MAP = new HashMap<String, String>() {{
        put("29416", "原创榜_男生");
        put("29418", "热销榜_男生");
        put("1", "月票榜_男生");

        put("29419", "原创榜_女生");
        put("29420", "热销榜_女生");
        put("3", "月票榜_女生");

    }};

    private PageRule rule = PageRule.build()
            .add("^http:\\/\\/ah2\\.zhangyue\\.com$", this::processEntrance)
            .add("/category/category", this::processCategoryEntrance)
            .add("/category/index", this::processCategory)
            .add("/rank/books", this::processRank)
            .add("/detail/index", this::processDetail);

    private void processEntrance(Page page) {
        createRankJobs(page);
        createJob(page, categoryUrl);
    }

    private void processCategory(Page page) {
        String channel = page.getUrl().regex(".*categoryType=(women|man).*").get();
        JSONObject data = page.getJson().toObject(JSONObject.class);
        JSONArray books = data.getJSONObject("body").getJSONObject("sectionModule").getJSONObject("section")
                .getJSONArray("books");
        for (int i = 0, size = books.size(); i < size; i++) {
            String bookId = books.getJSONObject(i).getString("id");
            String pk = "man".equals(channel) ? "ch_male_rank" : "ch_female_rank";
            createJob(page, String.format(detailUrl, bookId, pk));
        }
    }

    private void processCategoryEntrance(Page page) {
        JSONObject response = page.getJson().toObject(JSONObject.class);
        if (response.containsKey("body")) {
            JSONArray data = response.getJSONObject("body").getJSONArray("categoryList");
            JSONArray items;
            for (int i = 0, size = data.size(); i < size; i++) {
                String category = data.getJSONObject(i).getString("category");
                if (category.equals("women") || category.equals("man")) {
                    items = data.getJSONObject(i).getJSONArray("items");
                    for (int j = 0, jsize = items.size(); j < jsize; j++) {
                        int categoryId = items.getJSONObject(j).getIntValue("categoryId");
                        for (int k = 1; k < 3; k++) {
                            createJob(page, String.format(categoryListUrl, category, categoryId, k));
                        }
                    }
                }
            }
        }
    }

    private void createRankJobs(Page page) {
        for (String param : rankParams) {
            String url = String.format(rankUrl, 1, param);
            createJob(page, url);
        }
    }

    private void processRank(Page page) {
        String channel = page.getUrl().regex(".*&freqKey=(ch_male_rank|ch_female_rank).*").get();
        JSONObject response = page.getJson().toObject(JSONObject.class);
        if (response.containsKey("body")) {
            JSONArray body = response.getJSONArray("body");
            JSONObject ob;
            for (int i = 0, size = body.size(); i < size; i++) {
                ob = body.getJSONObject(i);
                String bookId = ob.getString("id");
                String url = String.format(detailUrl, bookId, channel);
                createJob(page, url);
            }


            //保存排行
            String url = page.getUrl().get();
            Map<String, String> allParams = UrlUtils.getAllParam(url);
            String sectionId = allParams.get("sectionId");
            String rankType = SECTIONID_RANK_TYPE_MAP.get(sectionId);
            //如果是指定榜单，才会进行此步骤
            if (StringUtils.isNotEmpty(rankType)) {
                Date day = DateUtil.getDayStartTime(new Date());

                String currentPage = allParams.get("currentPage");
                String pageSize = allParams.get("pageSize");
                Integer beforeSize = (Integer.parseInt(currentPage) - 1) * Integer.parseInt(pageSize);
                List<FictionOriginalBillboard> billboardList = new ArrayList<>();

                for (int i = 0; i < body.size(); i++) {
                    JSONObject book = body.getJSONObject(i);
                    String bookId = book.getString("id");

                    FictionOriginalBillboard billboard = new FictionOriginalBillboard();
                    billboard.setPlatformId(Platform.IREADER.getCode());
                    billboard.setType(rankType);
                    billboard.setDay(day);
                    billboard.setRank(beforeSize + i + 1);
                    billboard.setCode(String.valueOf(bookId));
                    billboardList.add(billboard);
                }
                putModel(page, billboardList);
            }
        }
    }

    private void processDetail(Page page) {
        FictionChannel channel = page.getUrl().regex(".*&pk=RANK(ch_male_rank|ch_female_rank).*").get()
                .equals("ch_female_rank") ? FictionChannel.GIRL : FictionChannel.BOY;
        JSONObject response = page.getJson().toObject(JSONObject.class);
        if (response.containsKey("body")) {
            JSONObject body = response.getJSONObject("body");
            JSONObject bookInfo = body.getJSONObject("bookInfo");
            Fiction fiction = new Fiction();
            fiction.setAuthor(bookInfo.getString("author"));
            fiction.setChannel(channel.getCode());
            fiction.setCode(bookInfo.getString("bookId"));
            fiction.setCover(bookInfo.getString("picUrl"));
            fiction.setIsFinish(bookInfo.getString("completeState").equals("N") ? 0 : 1);
            fiction.setIntro(bookInfo.getString("desc").trim());
            fiction.setName(bookInfo.getString("bookName"));
            fiction.setPlatformId(Platform.IREADER.getCode());

            JSONArray tagInfos = bookInfo.getJSONArray("tagInfo");
            StringBuilder builder = null;
            for (int i = 0, size = tagInfos.size(); i < size; i++) {
                if (builder == null) {
                    builder = new StringBuilder();
                } else {
                    builder.append("/");
                }
                builder.append(tagInfos.getJSONObject(i).getString("name"));
            }

            if (builder != null)
                fiction.setTags(builder.toString());
            fiction.setTotalLength(transformatWordCount(bookInfo.getString("wordCount")));
            putModel(page, fiction);

            JSONObject commentInfo = body.getJSONObject("commentList").getJSONObject("circleInfo");
            int commentCount = 0;
            if (commentInfo.containsKey("topicNum"))
                commentCount += commentInfo.getIntValue("topicNum");
            if (commentInfo.containsKey("replyNum"))
                commentCount += commentInfo.getIntValue("replyNum");
            FictionCommentLogs commentLog = new FictionCommentLogs();
            commentLog.setCode(bookInfo.getString("bookId"));
            commentLog.setCommentCount(commentCount);
            commentLog.setPlatformId(Platform.IREADER.getCode());
            putModel(page, commentLog);

            if (bookInfo.containsKey("attribute")) {
                JSONObject attribute = bookInfo.getJSONObject("attribute");
                long clickCount = NumberHelper.parseShortNumber(attribute.getString("popularityNum"), 0);
                FictionPlatformClick click = new FictionPlatformClick();
                click.setCode(fiction.getCode());
                click.setClickCount(clickCount);
                click.setPlatformId(Platform.IREADER.getCode());
                putModel(page, click);

                int likeCount = (int) NumberHelper.parseShortNumber(attribute.getString("likeNum"), 0);
                FictionPlatformFavorite favorite = new FictionPlatformFavorite();
                favorite.setCode(fiction.getCode());
                favorite.setFavoriteCount(likeCount);
                favorite.setPlatformId(Platform.IREADER.getCode());
                putModel(page, favorite);
            }

            createJob(page, String.format(webDetailUrl, fiction.getCode()));

        }
    }

    private int transformatWordCount(String wordCountStr) {
        float wordCount = Float.valueOf(RegexUtil.getDataByRegex(wordCountStr, "^(\\d+(\\.\\d+)?).*"));
        if (wordCountStr.endsWith("万字")) {
            wordCount *= 10000;
        } else if (wordCountStr.endsWith("亿字")) {
            wordCount *= 100000000;
        }
        return (int) wordCount;
    }

    private void createJob(Page page, String url) {
        Job job = new Job(url);
        job.setCode(Md5Util.getMd5(url));
        job.setPlatformId(Platform.IREADER.getCode());
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
