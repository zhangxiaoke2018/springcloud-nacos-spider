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
import com.jinguduo.spider.common.constant.CommonEnum.Platform;
import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.common.util.Md5Util;
import com.jinguduo.spider.data.table.FictionChapters;
import com.jinguduo.spider.webmagic.Page;
import lombok.extern.apachecommons.CommonsLog;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Worker
@CommonsLog
public class ZHMobileSpider extends CrawlSpider {
    private static final String key = "082DE6CF1178736AF28EB8065CDBE5AC";

    private static final String PARAM_DETAIL = "api_key=27A28A4D4B24022E543E&apn=wlan&appId=ZHKXS&bookId=%s&brand=Netease&channelId=A1002&channelType=H5&clientVersion=4.6.0.21&installId=1dc77f12094bda897f42000c001e03bf&model=MuMu&modelName=cancro&os=android&osVersion=19&preChannelId=A1002&screenH=1280&screenW=720&userId=0";
    private static final String PATH_DETAIL = "https://api1.zongheng.com/api/book/bookInfo?";

    private static final int[] RANK_TOP_100 = {1, 4, 19, 14};
    private static final int[] RANK_TOP_50 = {-1};
    private static final String PATH_RANK = "https://m.zongheng.com/h5/ajax/rank?pageNum=%d&pageSize=50&rankType=%d";
    private static final String PATH_CHAPTER2 = "https://m.zongheng.com/h5/ajax/chapter/list?h5=1&bookId=%s&pageNum=1&pageSize=%s";

    private Site site = SiteBuilder.builder().setDomain("m.zongheng.com").build();

    private PageRule rules = PageRule.build().add("^https:\\/\\/m\\.zongheng\\.com$", this::processMain)
            .add("/h5/ajax/rank", this::processRank)
            .add("/h5/ajax/chapter/list", this::processChapter);

    //rt:榜单类型。 1：月票、3：畅销、4：新书
    //p :页码 1 开始
    private static final String PC_RANK_URL = "http://www.zongheng.com/rank/details.html?rt=%s&d=1&p=%s";
    private static final int[] PC_RANK_TYPE = {1, 3, 4};


    public void processMain(Page page) {
        for (int rank : RANK_TOP_100) {
            for (int i = 1; i < 4; i++) {
                createJob(page, String.format(PATH_RANK, i, rank));
            }
        }

        for (int rank : RANK_TOP_50) {
            createJob(page, String.format(PATH_RANK, 1, rank));
        }

        //生成榜单任务
        for (int type : PC_RANK_TYPE) {
            for (int i = 1; i <= 10; i++) {
                createJob(page, String.format(PC_RANK_URL, type, i));
            }
        }

    }

    public void processRank(Page page) {
        try {
            JSONObject response = page.getJson().toObject(JSONObject.class);
            if (response != null && response.containsKey("ranklist")) {
                JSONArray result = response.getJSONArray("ranklist");
                if (result.size() > 0) {
                    for (int i = 0, len = result.size(); i < len; i++) {
                        String bookId = String.valueOf(result.getJSONObject(i).getLong("bookId"));
                        createJob(page, buildDetailUrl(bookId));
                    }
                }
            }
        } catch (Throwable t) {
            log.info("zong heng spider failed" + page.getRawText());
        }
    }

    public void processChapter(Page page) {
        String bookId = page.getUrl().regex(".*&bookId=(\\d+)&.*", 1).get();
        JSONObject result = page.getJson().toObject(JSONObject.class);
        if (result.containsKey("chapterlist")) {
            Integer chapterCount = result.getJSONObject("chapterlist").getInteger("chapterCount");
            JSONArray chapterList = result.getJSONObject("chapterlist").getJSONArray("chapters");
            int listSize = chapterList.size();
            int freeChapterCount = 0;
            if (chapterCount > listSize && chapterList.getJSONObject(listSize - 1).getInteger("level") == 0) {
                createJob(page, String.format(PATH_CHAPTER2, bookId, chapterCount));
            } else {
                for (int i = 0; i < listSize; i++) {
                    if (chapterList.getJSONObject(i).getInteger("level") == 0)
                        freeChapterCount++;
                }

                FictionChapters chapter = new FictionChapters();
                chapter.setCode(bookId);
                chapter.setPlatformId(Platform.ZONG_HENG.getCode());
                chapter.setFreeChapterCount(freeChapterCount);
                chapter.setIsVip(chapterCount > freeChapterCount ? 1 : 0);
                chapter.setTotalChapterCount(chapterCount);
                putModel(page, chapter);

            }
        }
    }

    private void createJob(Page page, String url) {
        Job oldJob = ((DelayRequest) page.getRequest()).getJob();
        try {
            Job job = new Job(url);
            DbEntityHelper.derive(oldJob, job);
            job.setCode(Md5Util.getMd5(url));
            job.setPlatformId(Platform.ZONG_HENG.getCode());
            putModel(page, job);
        } catch (Throwable e) {
        }

    }

    private String buildDetailUrl(String bookId) throws Throwable {
        String params = String.format(PARAM_DETAIL, bookId);
        return PATH_DETAIL + params + "&sig=" + sign(key + params + key);
    }


    private static String sign(String text) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        byte[] digest = MessageDigest.getInstance("MD5").digest(text.getBytes("UTF-8"));
        StringBuilder stringBuilder = new StringBuilder(digest.length * 2);
        for (byte b : digest) {
            if ((b & 255) < 16) {
                stringBuilder.append("0");
            }
            stringBuilder.append(Integer.toHexString(b & 255));
        }
        return stringBuilder.toString();
    }

    @Override
    public Site getSite() {
        // TODO Auto-generated method stub
        return site;
    }

    @Override
    public PageRule getPageRule() {
        // TODO Auto-generated method stub
        return rules;
    }

}
