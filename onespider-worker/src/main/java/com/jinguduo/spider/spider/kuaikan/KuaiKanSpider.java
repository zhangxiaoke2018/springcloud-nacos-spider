package com.jinguduo.spider.spider.kuaikan;

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
import com.jinguduo.spider.common.util.TextUtils;
import com.jinguduo.spider.data.table.Comic;
import com.jinguduo.spider.data.table.ComicBanner;
import com.jinguduo.spider.data.table.ComicBestSellingRank;
import com.jinguduo.spider.data.table.ComicOriginalBillboard;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.utils.UrlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.ParseException;
import java.util.*;

/**
 * I'm a cute code dog
 * User: LingChen
 * Date:2017/7/31
 * Time:16:48
 */
@Slf4j
@Worker
public class KuaiKanSpider extends CrawlSpider {

    /**
     * http://www.kuaikanmanhua.com/web/tags/19?count=200&page=0
     * https://www.kuaikanmanhua.com/ranking/4
     */

    private static final String KUAIKAN_BASE_URL = "https://www.kuaikanmanhua.com/v1/search/by_tag?since=%s&count=48&f=3&tag=0";

    private static final String KUAIKAN_DETAIL_URL = "https://api.kkmh.com/v1/topics/%s/";

    private static final String KUAIKAN_CHANGXIAO_RANK_URL = "https://www.kuaikanmanhua.com/ranking/4";

    private static final String BILLBOARD_URL = "https://www.kuaikanmanhua.com/v2/pweb/rank/topics?rank_id=%s";

    private static final String HOME_URL = "https://www.kuaikanmanhua.com";

    private static final Map<Integer, String> BILLBOARD_SUFFIX_MAP = new HashMap<Integer, String>() {{
        put(2, "?????????");
        put(3, "?????????");
        put(4, "?????????");
        put(5, "?????????");
        put(6, "?????????");
        put(7, "?????????");
        put(8, "?????????");
        put(9, "?????????");
    }};


    private Site site = SiteBuilder.builder()
            .setDomain("www.kuaikanmanhua.com")
            .build();

    private PageRule rules = PageRule.build()
            .add(".com$", page -> createTask(page))
            .add("since=0&count", page -> createAllTask(page))
            .add("/ranking/4", page -> analyzeChangxiaoRank(page))
            .add("/pweb/rank/topics", page -> billboard(page))
            .add("/web/comic/", page -> processBannerDetail(page))
            .add("/search/by_tag", page -> analyzeComicList(page));

    //????????????
    private void processBannerDetail(Page page) {
        Job job = ((DelayRequest) page.getRequest()).getJob();
        String url = job.getUrl();
        String source = UrlUtils.getParam(url, "source");
        if (StringUtils.isEmpty(source)) return;

        Document document = page.getHtml().getDocument();
        Element title = document.getElementsByClass("title").first();
        Element comic = title.getElementsByAttributeValueContaining("href", "/web/topic/").first();
        String href = comic.attr("href");
        String codeNum = StringUtils.replace(href, "/web/topic/", "");
        if (StringUtils.isEmpty(codeNum)) return;

        ComicBanner cb = new ComicBanner();
        cb.setCode("kuaikan-" + codeNum);
        cb.setDay(DateUtil.getDayStartTime(new Date()));
        cb.setPlatformId(30);
        cb.setSource(source);
        cb.setName(comic.text());

        putModel(page, cb);
    }

    /**
     * ????????????
     */
    private void createTask(Page page) {
        Job job = ((DelayRequest) page.getRequest()).getJob();
        List<Job> jobs = new ArrayList<>();
        //??????????????????
        String newUrl = String.format(KUAIKAN_BASE_URL, 0);
        Job job2 = new Job(newUrl);
        DbEntityHelper.derive(job, job2);
        jobs.add(job2);

        Job job3 = new Job(KUAIKAN_CHANGXIAO_RANK_URL);
        DbEntityHelper.derive(job, job3);
        jobs.add(job3);

        for (Integer rankingId : BILLBOARD_SUFFIX_MAP.keySet()) {
            String billboardUrl = String.format(BILLBOARD_URL, rankingId);

            Job billboardJob = new Job(billboardUrl);
            DbEntityHelper.derive(job, billboardJob);
            jobs.add(billboardJob);
        }

        //index????????????banner??????
        String tail = "?source=HOME";
        Document document = page.getHtml().getDocument();
        Element slider = document.getElementById("BanneContentSlider");
        Elements banners = slider.getElementsByTag("a");
        for (Element banner : banners) {
            String href = banner.attr("href");
            String bannerUrl = HOME_URL + href + tail;

            Job bannerJob = new Job(bannerUrl);
            DbEntityHelper.derive(job, bannerJob);
            jobs.add(bannerJob);

        }

        putModel(page, jobs);

    }

    /**
     * ????????????2
     */
    private void createAllTask(Page page) {
        Job job = ((DelayRequest) page.getRequest()).getJob();
        JSONObject jsonObject = JSONObject.parseObject(page.getRawText());
        Integer total = jsonObject.getInteger("total");
        total = total / 48 + 1;
        for (int i = 1; i < total; i++) {
            Integer pageNum = i * 48;
            String newUrl = String.format(KUAIKAN_BASE_URL, pageNum);
            Job job2 = new Job(newUrl);
            DbEntityHelper.derive(job, job2);
            putModel(page, job2);
        }

    }

    private void analyzeComicList(Page page) {
        Job job = ((DelayRequest) page.getRequest()).getJob();

        JSONObject resObject = JSONObject.parseObject(page.getRawText());
        JSONObject datas = resObject.getJSONObject("data");
        JSONArray topics = datas.getJSONArray("topics");
        for (Object topicO : topics) {
            JSONObject topic = (JSONObject) topicO;
            Comic comic = new Comic();
            comic.setPlatformId(30);
            //??????id
            Integer comicId = topic.getInteger("id");
            //??????code
            comic.setCode("kuaikan-" + comicId);
            //??????
            JSONArray categories = topic.getJSONArray("category");
            String categoriesStr = StringUtils.join(categories.toArray(), "/");

            comic.setSubject(categoriesStr);
            //??????
            comic.setName(topic.getString("title"));
            //??????
            comic.setHeaderImg(topic.getString("cover_image_url"));
            //??????
            comic.setIntro(TextUtils.removeBadText(topic.getString("description")));
            //????????????  1 ?????? 2 ??????
            Integer finishStatus = topic.getInteger("update_status");
            comic.setFinished(finishStatus.equals(2));

            //??????
           /* JSONObject user = topic.getJSONObject("user");
            comic.setAuthor(user.getString("nickname"));*/

            //??????
            comic.setEpisode(topic.getInteger("comics_count"));
//            log.info("www.kuaikan.com save comic ->{}", comic.toString());
            //??????????????????
            String detailUrl = String.format(KUAIKAN_DETAIL_URL, comicId);
            Job job2 = new Job(detailUrl);
            DbEntityHelper.derive(job, job2);
            putModel(page, job2);

            putModel(page, comic);
        }


    }

    private void analyzeChangxiaoRank(Page page) {
        Job job = ((DelayRequest) page.getRequest()).getJob();

        Document document = page.getHtml().getDocument();
        Element body = document.getElementsByTag("body").get(0);

        Date day = DateUtil.getDayStartTime(new Date());
        Integer platformId = 30;

        //?????????????????????1
        Elements list = body.getElementsByClass("IdItems fl");
        int eachSize = list.size() + 1;
        for (int i = 1; i < eachSize; i++) {
            Element outDiv = list.get(i - 1);
            Element outA = outDiv.getElementsByTag("a").get(0);
            String href = outA.attr("href");
            int start = StringUtils.indexOf(href, "/web/topic/") + 11;
            String comicId = StringUtils.substring(href, start, href.length());
            String code = "kuaikan-" + comicId;
            Element detailDiv = outA.getElementsByClass("details fl").get(0);
            String title = detailDiv.getElementsByClass("RankIcon icons").next().get(0).text();

            Element trend = detailDiv.getElementsByClass("trend").get(0);
            String svgText = trend.getElementsByTag("svg").text();
            String trendText = trend.text();
            String trendStr = StringUtils.replace(trendText, svgText, "").trim();

            Integer riseStatus = 0;
            Integer rise = null;
            trendStr = StringUtils.replace(trendStr, "???", "");
            if (StringUtils.contains(trendStr, "??????")) {
                riseStatus = 0;
                rise = 0;
            } else if (StringUtils.contains(trendStr, "??????")) {
                riseStatus = 1;
                String riseStr = StringUtils.replace(trendStr, "??????", "");
                rise = Integer.valueOf(riseStr);
            } else if (StringUtils.contains(trendStr, "??????")) {
                riseStatus = -1;
                String riseStr = StringUtils.replace(trendStr, "??????", "");
                rise = Integer.valueOf(riseStr);
            } else if (StringUtils.contains(trendStr, "??????")) {
                riseStatus = 2;
            }

            ComicBestSellingRank cr = new ComicBestSellingRank();
            cr.setPlatformId(platformId);
            cr.setDay(day);
            cr.setRank(i);
            cr.setCode(code);
            cr.setName(title);
            cr.setRiseStatus(riseStatus);
            cr.setRise(rise);
            putModel(page, cr);

        }

    }

    private void billboard(Page page) {
        Job job = ((DelayRequest) page.getRequest()).getJob();
        JSONObject jsonObject = JSONObject.parseObject(page.getRawText());

        Integer platformId = 30;
        Date day = DateUtil.getDayStartTime(new Date());

        JSONObject data = jsonObject.getJSONObject("data");
        //????????????
        JSONObject rank_info = data.getJSONObject("rank_info");
        String billboardType = rank_info.getString("title");
        String next_update_date = rank_info.getString("next_update_date");//6???3???17:00

        Date thisUpdateTime = null;
        try {
            thisUpdateTime = this.getBillboardUpdateTime(next_update_date, day);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        JSONArray topics = rank_info.getJSONArray("topics");

        List<ComicOriginalBillboard> list = new ArrayList();
        for (int i = 0; i < topics.size(); i++) {
            JSONObject comic = (JSONObject) topics.get(i);
            Integer rank = i + 1;
            String comicId = comic.getString("id");
            String code = "kuaikan-" + comicId;
            String name = comic.getString("title");

            ComicOriginalBillboard billboard = new ComicOriginalBillboard();
            billboard.setDay(day);
            billboard.setPlatformId(platformId);
            billboard.setBillboardType(billboardType);
            billboard.setRank(rank);
            billboard.setCode(code);
            billboard.setName(name);
            billboard.setBillboardUpdateTime(thisUpdateTime);
            list.add(billboard);
        }
        putModel(page, list);

    }


    //????????????????????????;???????????????
    private Date getBillboardUpdateTime(String nextTimeStr, Date today) throws ParseException {
        //??????
        String dateFormPattern = "MM???dd???";
        //??? MM???dd???
        String dayPrefixStr = StringUtils.substring(nextTimeStr, 0, StringUtils.indexOf(nextTimeStr, "???") + 1);
        //?????? 17:00 HH:mm
        String hourNumStr = StringUtils.substring(nextTimeStr, StringUtils.indexOf(nextTimeStr, "???") + 1
                , StringUtils.indexOf(nextTimeStr, ":"));

        //str -> date
        Date dayPrefix = DateUtils.parseDate(dayPrefixStr, dateFormPattern);
        //??????????????????
        dayPrefixStr = DateFormatUtils.format(dayPrefix, dateFormPattern);

        //result
        Date thisUpdateTime = null;
        //1.????????????????????????8??????????????????????????????????????????????????????
        for (int i = 0; i < 8; i++) {
            Date testDay = DateUtils.addDays(today, i);
            String testDayStr = DateFormatUtils.format(testDay, dateFormPattern);
            if (dayPrefixStr.equals(testDayStr)) {
                thisUpdateTime = DateUtils.addHours(DateUtils.addDays(testDay, -7), Integer.valueOf(hourNumStr));
                break;
            }
        }

        return thisUpdateTime;
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
