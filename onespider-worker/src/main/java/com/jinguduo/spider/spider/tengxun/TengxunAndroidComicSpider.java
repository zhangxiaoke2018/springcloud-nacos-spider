package com.jinguduo.spider.spider.tengxun;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.util.DateUtil;
import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.common.util.tengxun.CryptUtils;
import com.jinguduo.spider.data.table.*;
import com.jinguduo.spider.webmagic.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.jinguduo.spider.spider.tengxun.TengxunComicSpider.TENGXUN_RANKINGID_MAP;

/**
 * Created by lc on 2019/3/27
 */
@Slf4j
@Worker
public class TengxunAndroidComicSpider extends CrawlSpider {


    private static final String AUTHOR_DETAIL_URL = "http://android.ac.qq.com/7.21.3/User/getVGuestHomePage/host_qq/%s";
    private Site site = SiteBuilder.builder()
            .setDomain("android.ac.qq.com")
            .setUserAgent("okhttp/3.2.0")
            .addSpiderListener(new TengxunAndroidComicDownLoaderListener())
            .build();

    private PageRule rules = PageRule.build()
            .add("/comicChapterList", page -> analyzeEpisode(page))
            .add("/comicDetail", page -> analyzeDetail(page))
            .add("/getVGuestHomePage", page -> analyzeAuthor(page))
            .add("/rankDetail/rank_id/14", page -> analyzeChangxiaoRank(page))
            .add("/rankDetail/rank_id/", page -> analyzeRank(page));

    private void analyzeRank(Page page) {
        Job job = ((DelayRequest) page.getRequest()).getJob();
        String url = job.getUrl();
        String jsonStr = CryptUtils.ees3DecodeECB2Str(page.getRawText().getBytes());
        JSONObject jsonObject = JSONObject.parseObject(jsonStr);
        Integer error_code = jsonObject.getInteger("error_code");
        //请求有问题或接口改版
        if (null == error_code || error_code != 2) {
            return;
        }
        Date day = DateUtil.getDayStartTime(new Date());
        Integer platformId = 32;

        String rankingId = StringUtils.substring(url, StringUtils.indexOf(url, "rank_id/") + 8, StringUtils.indexOf(url, "/page"));
        String billboardType = TENGXUN_RANKINGID_MAP.get(Integer.valueOf(rankingId));

        JSONArray datas = jsonObject.getJSONArray("data");
        String last_change_time = jsonObject.getString("last_change_time");
        Date thisUpdateTime = this.tengxunGetThisUpdateTime(last_change_time, day);
        List<ComicOriginalBillboard> list = new ArrayList<>();
        for (Object dataObj : datas) {
            JSONObject data = (JSONObject) dataObj;
            String code = "qq-" + data.getString("comic_id");
            String title = new String(data.getString("title"));
            Integer rank = data.getInteger("rank");

            ComicOriginalBillboard billboard = new ComicOriginalBillboard();
            billboard.setDay(day);
            billboard.setPlatformId(platformId);
            billboard.setBillboardType(billboardType);
            billboard.setRank(rank);
            billboard.setCode(code);
            billboard.setName(title);
            billboard.setBillboardUpdateTime(thisUpdateTime);
            list.add(billboard);
        }

        putModel(page, list);

    }

    public static void main(String[] args) {
        String a = "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";
        String jsonStr = CryptUtils.ees3DecodeECB2Str(a.getBytes());
        System.out.println(jsonStr);
    }

    private void analyzeAuthor(Page page) {
        Job job = ((DelayRequest) page.getRequest()).getJob();
        String jsonStr = CryptUtils.ees3DecodeECB2Str(page.getRawText().getBytes());
        JSONObject jsonObject = JSONObject.parseObject(jsonStr);
        Integer error_code = jsonObject.getInteger("error_code");
        //请求有问题或接口改版
        if (null == error_code || error_code != 2) {
            return;
        }
        Integer platformId = 32;
        JSONObject data = jsonObject.getJSONObject("data");
        String nick_name = data.getString("nick_name");
        String oldUrl = job.getUrl();
        String authorId = StringUtils.substring(oldUrl, StringUtils.indexOf(oldUrl, "/host_qq/") + 9, oldUrl.length());
        String comicCode = job.getCode();
        ComicAuthorRelation car = new ComicAuthorRelation();
        car.setComicCode(comicCode);
        car.setAuthorName(nick_name);
        car.setAuthorId(authorId);
        car.setPlatformId(platformId);
        putModel(page, car);


    }

    private void analyzeChangxiaoRank(Page page) {
        Job job = ((DelayRequest) page.getRequest()).getJob();
        String jsonStr = CryptUtils.ees3DecodeECB2Str(page.getRawText().getBytes());
        JSONObject jsonObject = JSONObject.parseObject(jsonStr);
        Integer error_code = jsonObject.getInteger("error_code");
        //请求有问题或接口改版
        if (null == error_code || error_code != 2) {
            return;
        }

        Date day = DateUtil.getDayStartTime(new Date());

        Integer platformId = 32;


        JSONArray datas = jsonObject.getJSONArray("data");
        for (Object dataObj : datas) {
            JSONObject data = (JSONObject) dataObj;
            String code = "qq-" + data.getString("comic_id");
            String title = new String(data.getString("title"));
            Integer rank = data.getInteger("rank");

            //trend 3:保持、4:新晋、1、上升、2:下降
            Integer trend = data.getInteger("trend");

            Integer riseStatus;
            if (trend == 1) {
                riseStatus = 1;
            } else if (trend == 2) {
                riseStatus = -1;
            } else if (trend == 3) {
                riseStatus = 0;
            } else {
                riseStatus = 2;
            }

            ComicBestSellingRank cr = new ComicBestSellingRank();
            cr.setPlatformId(platformId);
            cr.setDay(day);
            cr.setRank(rank);
            cr.setCode(code);
            cr.setName(title);
            cr.setRiseStatus(riseStatus);
            putModel(page, cr);
        }

    }

    private void analyzeDetail(Page page) {
        Job job = ((DelayRequest) page.getRequest()).getJob();
        String jsonStr = CryptUtils.ees3DecodeECB2Str(page.getRawText().getBytes());
        JSONObject jsonObject = JSONObject.parseObject(jsonStr);
        Integer error_code = jsonObject.getInteger("error_code");
        //请求有问题或接口改版
        if (null == error_code || error_code != 2) {
            return;
        }
        JSONObject data = jsonObject.getJSONObject("data");
        JSONObject comic = data.getJSONObject("comic");
        Integer comic_id = comic.getInteger("comic_id");
        Long playCount = comic.getLong("pgv_count");
        Date day = DateUtil.getDayStartTime(new Date());

        String code = "qq-" + comic_id;
        ComicTengxun tx = new ComicTengxun();
        tx.setCode(code);
        tx.setDay(day);
        tx.setHotNum(playCount);
        tx.setComicId(String.valueOf(comic_id));
        putModel(page, tx);

        String artist_uin = comic.getString("artist_uin");
        String authorUrl = String.format(AUTHOR_DETAIL_URL, artist_uin);

        Job authorJob = new Job(authorUrl);
        DbEntityHelper.derive(job, authorJob);
        authorJob.setCode(code);
        putModel(page, authorJob);

    }

    private void analyzeEpisode(Page page) {

        Job job = ((DelayRequest) page.getRequest()).getJob();

        String jsonStr = CryptUtils.ees3DecodeECB2Str(page.getRawText().getBytes());


        JSONObject jsonObject = JSONObject.parseObject(jsonStr);
        Integer error_code = jsonObject.getInteger("error_code");
        //请求有问题或接口改版
        if (null == error_code || error_code != 2) {
            return;
        }

        //http://android.ac.qq.com/7.21.3/Comic/comicChapterList/comic_id/%s
        String oldUrl = job.getUrl();
        String code = "qq-" + StringUtils.substring(oldUrl, StringUtils.indexOf(oldUrl, "/comic_id/") + 10);


        JSONObject data = jsonObject.getJSONObject("data");
        JSONArray chapter_list = data.getJSONArray("chapter_list");


        int size = chapter_list.size();


        List<ComicEpisodeInfo> saveList = new ArrayList<>();
        Date day = DateUtil.getDayStartTime(new Date());
        for (int i = 1, episodeNum = size; i <= size; i++, episodeNum--) {
            JSONObject chapter = (JSONObject) chapter_list.get(i - 1);
            String title = "";
            Date updateTime;
            Integer vip_status = 0;
            try {
                title = new String(chapter.getString("chapter_title").getBytes(), "utf-8");
            } catch (Exception e) {
                // e.printStackTrace();
            }
            String update_time_str = chapter.getString("update_date");

            try {
                updateTime = DateUtils.parseDate(update_time_str, "yyyy-MM-dd");
            } catch (Exception e) {
                // e.printStackTrace();
                updateTime = new Date();
            }
            //1免费，3收费
            Integer payStatus = chapter.getInteger("icon_type");
            vip_status = payStatus == 3 ? 1 : 0;

            // TODO: 2019/3/28 单集点赞数
            Integer good_count = chapter.getInteger("good_count");


            ComicEpisodeInfo info = new ComicEpisodeInfo();
            info.setCode(code);
            info.setDay(day);
            info.setPlatformId(32);
            info.setName(title);
            info.setEpisode(episodeNum);
            info.setComicCreatedTime(updateTime);
            info.setVipStatus(vip_status);
            info.setLikeCount(good_count);
            saveList.add(info);
        }
        putModel(page, saveList);
    }

    /**
     * 奇怪的格式，无法完全获取，转换失败返回null
     */
    private Date tengxunGetThisUpdateTime(String nextString, Date today) {
        if (StringUtils.isEmpty(nextString) || !StringUtils.contains(nextString, "月")) {
            return null;
        }
        try {
            String dateFormPattern = "MM月dd日";
            //下次放榜时间：05月30日（周四）11:00
            nextString = StringUtils.replace(nextString, "下次放榜时间：", "");//05月30日（周四）11:00
            String dayStr = StringUtils.substring(nextString, 0, StringUtils.indexOf(nextString, "（"));
            String hourStr = StringUtils.substring(nextString, StringUtils.indexOf(nextString, "）") + 1,
                    StringUtils.indexOf(nextString, ":"));

            //string -> date ->string 统一格式
            Date day = DateUtils.parseDate(dayStr, dateFormPattern);
            dayStr = DateFormatUtils.format(day, dateFormPattern);
            //result
            Date thisUpdateTime = null;

            //1.今天的时间向后推8天，查询所有的时期并格式化成对应日期
            for (int i = 0; i < 8; i++) {
                Date testDay = DateUtils.addDays(today, i);
                String testDayStr = DateFormatUtils.format(testDay, dateFormPattern);
                if (dayStr.equals(testDayStr)) {
                    thisUpdateTime = DateUtils.addHours(DateUtils.addDays(testDay, -7), Integer.valueOf(hourStr));
                    break;
                }
            }

            return thisUpdateTime;
        } catch (ParseException e) {
            return null;
        }
    }

    @Override
    public PageRule getPageRule() {
        return rules;
    }

    @Override
    public Site getSite() {
        return site;
    }
}
