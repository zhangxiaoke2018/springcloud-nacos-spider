package com.jinguduo.spider.spider.weiboComic;

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
import com.jinguduo.spider.common.util.DateUtil;
import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.common.util.TextUtils;
import com.jinguduo.spider.data.table.*;
import com.jinguduo.spider.webmagic.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Worker
@SuppressWarnings("all")
public class WeiboComicSpider extends CrawlSpider {

    private String HOT_NUM_RANK_URL = "http://apiwap.vcomic.com/wbcomic/comic/filter_result?page_num=%s&rows_num=100&cate_id=0&end_status=0&comic_pay_status=0&order=comic_read_num&_request_from=pc";

    private String WEIBO_COMIC_DETAIL = "http://apiwap.vcomic.com/wbcomic/comic/comic_show?comic_id=%s&_request_from=pc";

    //阅读榜
    private String READ_BILLBOARD_URL = "http://apiwap.vcomic.com/wbcomic/home/rank_read?_request_from=pc";
    //新作榜
    private String SHARE_BILLBOARD_URL = "http://apiwap.vcomic.com/wbcomic/home/rank_share?_request_from=pc";
    //综合榜
    private String TOTAL_BILLBOARD_URL = "http://apiwap.vcomic.com/wbcomic/home/rank?_request_from=pc";


    private Site site = SiteBuilder.builder()
            .setDomain("apiwap.vcomic.com")
            .build();

    private PageRule rules = PageRule.build()
            .add(".com$", page -> createTask(page))
            .add("/wbcomic/comic/filter_result", page -> findComic(page))
            .add("/wbcomic/home/rank", page -> billboard(page))
            .add("/wbcomic/home/page_recommend_list", page -> processHomeBanner(page))
            .add("/wbcomic/comic/comic_show", page -> comicDetail(page));

    private void processHomeBanner(Page page) {
        JSONObject jsonObject = JSONObject.parseObject(page.getRawText());

        List<ComicBanner> cbList = new ArrayList<>();
        String codePrefix = "wb-";
        Integer platformId = 51;
        Date day = DateUtil.getDayStartTime(new Date());
        String source = "HOME";

        if (1 != jsonObject.getInteger("code")) return;
        JSONObject data = jsonObject.getJSONObject("data");

        JSONArray leftList = data.getJSONArray("recommend_index_rotation_map");
        JSONArray rightList = data.getJSONArray("recommend_index_rotation_right");


        for (int i = 0; i < leftList.size(); i++) {
            JSONObject comic = leftList.getJSONObject(i);
            String code = codePrefix + comic.getString("object_id");
            String title = comic.getString("title");
            ComicBanner cb = new ComicBanner(code, platformId, day, title, source);
            cbList.add(cb);
        }

        for (int i = 0; i < rightList.size(); i++) {
            JSONObject comic = rightList.getJSONObject(i);
            String code = codePrefix + comic.getString("object_id");
            String title = comic.getString("title");
            ComicBanner cb = new ComicBanner(code, platformId, day, title, source);
            cbList.add(cb);
        }
        putModel(page, cbList);

    }

    private void billboard(Page page) {
        Job job = ((DelayRequest) page.getRequest()).getJob();

        String url = job.getUrl();
        String billboardTypePrefix = StringUtils.substring(url, StringUtils.lastIndexOf(url, "/") + 1, StringUtils.lastIndexOf(url, "?"));
        Date day = DateUtil.getDayStartTime(new Date());
        Integer platformId = 51;

        JSONObject jsonObject = JSONObject.parseObject(page.getRawText());
        JSONObject data = jsonObject.getJSONObject("data");

        List<ComicOriginalBillboard> list = new ArrayList<>();
        for (String billboardTypeSuffix : data.keySet()) {
            String billboardType = billboardTypePrefix + "_" + billboardTypeSuffix;

            JSONArray comicList = data.getJSONArray(billboardTypeSuffix);
            for (int i = 0; i < comicList.size(); i++) {
                JSONObject comic = (JSONObject) comicList.get(i);

                String comic_id = comic.getString("comic_id");
                Integer rank = i + 1;
                String code = "wb-" + comic_id;
                String name = comic.getString("name");


                ComicOriginalBillboard billboard = new ComicOriginalBillboard();
                billboard.setDay(day);
                billboard.setPlatformId(platformId);
                billboard.setBillboardType(billboardType);
                billboard.setRank(rank);
                billboard.setCode(code);
                billboard.setName(name);

                list.add(billboard);
            }
        }

        putModel(page, list);

    }

    public void createTask(Page page) {
        Job job = ((DelayRequest) page.getRequest()).getJob();

        for (int i = 1; i < 30; i++) {
            String url = String.format(HOT_NUM_RANK_URL, i);
            Job taskJob = new Job(url);
            DbEntityHelper.derive(job, taskJob);
            putModel(page, taskJob);
        }

        //创建榜单任务，共三个

        Job readBillboardJob = new Job(READ_BILLBOARD_URL);
        Job shareBillboardJob = new Job(SHARE_BILLBOARD_URL);
        Job totalBillboardJob = new Job(TOTAL_BILLBOARD_URL);
        DbEntityHelper.derive(job, readBillboardJob);
        DbEntityHelper.derive(job, shareBillboardJob);
        DbEntityHelper.derive(job, totalBillboardJob);
        putModel(page, readBillboardJob);
        putModel(page, shareBillboardJob);
        putModel(page, totalBillboardJob);

    }

    public void findComic(Page page) {
        Job oldJob = ((DelayRequest) page.getRequest()).getJob();
        JSONObject json = JSONObject.parseObject(page.getJson().get());
        JSONObject data = json.getJSONObject("data");
        JSONArray rankinglist = data.getJSONArray("data");
        for (int i = 0; i < rankinglist.size(); i++) {
            JSONObject jsonObject = rankinglist.getJSONObject(i);
            String comicName = jsonObject.getString("comic_name");
            Integer comicId = jsonObject.getIntValue("comic_id");
            String code = "wb-" + String.valueOf(comicId);

            Job comicWeiboJob = new Job(String.format(WEIBO_COMIC_DETAIL, comicId));
            comicWeiboJob.setCode(code);
            putModel(page, comicWeiboJob);
        }
    }

    public void comicDetail(Page page) {
        Job oldJob = ((DelayRequest) page.getRequest()).getJob();
        JSONObject json = JSONObject.parseObject(page.getJson().get());
        boolean noFinished = false;
        JSONObject data = json.getJSONObject("data");
        JSONObject comic = data.getJSONObject("comic");
        String description = comic.getString("description");
        String hcover = comic.getString("hcover");
        String cover = comic.getString("cover");
        String name = comic.getString("name");
        Integer episode = comic.getInteger("chapter_num");
        String headImage = "";
        if (!("").equals(hcover) && hcover != null) {
            headImage = "http://img.manhua.weibo.com/" + hcover;
        } else {
            headImage = cover;
        }
        String author = comic.getString("sina_nickname");
        String sina_user_id = comic.getString("sina_user_id");


        Integer isEnd = comic.getIntValue("is_end");
        JSONArray comicSubject = data.getJSONArray("comic_cate");
        StringBuilder subject = new StringBuilder();
        Date day = DateUtil.getDayStartTime(new Date());
        if (comicSubject != null) {
            for (int i = 0; i < comicSubject.size(); i++) {
                JSONObject obj = comicSubject.getJSONObject(i);
                if (i == comicSubject.size() - 1) {
                    subject.append(obj.getString("cate_name"));
                } else {
                    subject.append(obj.getString("cate_name"));
                    subject.append("/");
                }
            }
        }
        JSONArray chapterList = data.getJSONArray("chapter_list");
        JSONObject lastEpisode = chapterList.getJSONObject(chapterList.size() - 1);
        Long created_at = lastEpisode.getLong("create_time");
        Date endTime = new Date(created_at * 1000);
        Comic c = new Comic();
        c.setCode(oldJob.getCode());
        c.setName(name);
        c.setAuthor(author);
        c.setHeaderImg(hcover);
        c.setSubject(subject.toString());
        c.setPlatformId(51);
        if (isEnd.equals(0)) {
            c.setFinished(noFinished);
        } else {
            c.setFinished(!noFinished);
        }
        c.setIntro(TextUtils.removeEmoji(description));
        c.setHeaderImg(headImage);
        c.setEpisode(episode);
        c.setEndEpisodeTime(endTime);
        putModel(page, c);


        ComicAuthorRelation car = new ComicAuthorRelation();
        car.setPlatformId(51);
        car.setComicCode(oldJob.getCode());
        car.setAuthorName(author);
        car.setAuthorId(sina_user_id);
        putModel(page, car);

        String hotNumText = comic.getString("comic_hot_value_text");
        Long hotNum = returnLong(hotNumText);
        JSONObject comicExtra = data.getJSONObject("comic_extra");
        Long clickNum = comicExtra.getLongValue("click_num");
        Long favoriteNum = comicExtra.getLongValue("favorite_num");
        Long commentNum = comicExtra.getLongValue("comment_num");
        ComicWeibo comicWeibo = new ComicWeibo();
        comicWeibo.setCode(oldJob.getCode());
        comicWeibo.setClickNum(clickNum);
        comicWeibo.setCommentNum(commentNum);
        comicWeibo.setFavoriteNum(favoriteNum);
        comicWeibo.setHotNum(hotNum);
        comicWeibo.setDay(DateUtil.getDayStartTime(new Date()));
        putModel(page, comicWeibo);

        JSONObject isAllowRead = data.getJSONObject("is_allow_read");
        JSONObject comicInfo = isAllowRead.getJSONObject("comic");
        JSONArray tryReadChapters = comicInfo.getJSONArray("try_read_chapters");
        if (tryReadChapters.size() == 0) {
            Integer isVip = 0;
            List<ComicEpisodeInfo> episodeList = Lists.newArrayList();
            for (int i = 0; i < chapterList.size(); i++) {
                JSONObject jsonObject = (JSONObject) chapterList.get(i);
                ComicEpisodeInfo info = new ComicEpisodeInfo();
                info.setCode(oldJob.getCode());
                info.setPlatformId(51);
                info.setDay(day);
                info.setName(jsonObject.getString("chapter_name").trim());
                info.setEpisode(i + 1);
                info.setVipStatus(isVip);
                info.setComicCreatedTime(new Date(jsonObject.getLong("create_time") * 1000L));
                episodeList.add(info);
            }
            putModel(page, episodeList);
        } else {
            List<ComicEpisodeInfo> episodeList = Lists.newArrayList();
            List<Integer> list = tryReadChapters.toJavaList(Integer.class);
            for (int i = 0; i < chapterList.size(); i++) {
                JSONObject jsonObject = (JSONObject) chapterList.get(i);
                Integer chaptersId = jsonObject.getInteger("chapter_id");
                Integer isVip = null;
                if (list.contains(chaptersId)) {
                    isVip = 0;
                } else {
                    isVip = 1;
                }
                ComicEpisodeInfo info = new ComicEpisodeInfo();
                info.setCode(oldJob.getCode());
                info.setPlatformId(51);
                info.setDay(day);
                info.setName(jsonObject.getString("chapter_name").trim());
                info.setEpisode(i + 1);
                info.setVipStatus(isVip);
                info.setComicCreatedTime(new Date(jsonObject.getLong("create_time") * 1000L));
                episodeList.add(info);
            }
            putModel(page, episodeList);
        }


    }


    @Override
    public PageRule getPageRule() {
        return this.rules;
    }

    @Override
    public Site getSite() {
        return this.site;
    }

    public Long returnLong(String hotNumTest) {
        Long l = 0L;
        String reg = "[\u2E80-\u9FFF]";
        Pattern pat = Pattern.compile(reg);
        Matcher mat = pat.matcher(hotNumTest);
        String num = mat.replaceAll("");
        String unit = hotNumTest.replace(num, "").trim();
        switch (unit) {
            case "亿":
                Double yd = Double.valueOf(hotNumTest.replace("亿", "")) * 100000000;
                l = yd.longValue();
                break;
            case "千万":
                Double kd = Double.valueOf(hotNumTest.replace("千万", "")) * 10000000;
                l = kd.longValue();
                break;
            case "百万":
                Double hd = Double.valueOf(hotNumTest.replace("百万", "")) * 1000000;
                l = hd.longValue();
                break;
            case "万":
                Double wd = Double.valueOf(hotNumTest.replace("万", "")) * 10000;
                l = wd.longValue();
                break;
            default:
                l = Double.valueOf(hotNumTest).longValue();
                break;
        }
        return l;
    }


}
