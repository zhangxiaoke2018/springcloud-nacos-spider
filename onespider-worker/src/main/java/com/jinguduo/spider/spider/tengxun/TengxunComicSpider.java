package com.jinguduo.spider.spider.tengxun;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.exception.AntiSpiderException;
import com.jinguduo.spider.common.util.DateUtil;
import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.common.util.TextUtils;
import com.jinguduo.spider.data.table.Comic;
import com.jinguduo.spider.data.table.ComicBanner;
import com.jinguduo.spider.data.table.ComicCommentText;
import com.jinguduo.spider.data.table.ComicTengxun;
import com.jinguduo.spider.spider.youku.HttpClientUtil;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.selector.Html;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.assertj.core.util.Lists;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * I'm a cute code dog
 * User: LingChen
 * Date:2017/7/31
 * Time:16:48
 */
@Slf4j
@Worker
public class TengxunComicSpider extends CrawlSpider {

    /**
     * 入口
     * http://ac.qq.com/Index/getWeekRankComic
     * 入口变更为 http://ac.qq.com/Comic/all/nation/1/search/hot/page/1
     * 详情页
     * http://ac.qq.com/Comic/comicInfo/id/551098
     * <p>
     * 标签 = "http://ac.qq.com/Comic/userComicInfo?comicId=551098";
     * 月票状况 = "http://ac.qq.com/Comic/getMonthTicketInfo/id/551098";
     * 打赏次数 = "http://ac.qq.com/Comic/getAwardInfo/id/551098";
     * 评论区 = "http://ac.qq.com/Community/topicList?targetId=551098&page=1";
     */
    private static final String QQ_SEARCH_PAGE_URL = "http://ac.qq.com/Comic/all/nation/1/search/hot/page/%s";
    private static final String QQ_BASE_URL = "http://ac.qq.com/";
    private static final String QQ_TAG_URL = "http://ac.qq.com/Comic/userComicInfo?comicId=%s?_=%s";
    private static final String QQ_TICKET_URL = "http://ac.qq.com/Comic/getMonthTicketInfo/id/%s?_=%s";
    private static final String QQ_DASHANG_URL = "http://ac.qq.com/Comic/getAwardInfo/id/%s?_=%s";
    private static final String QQ_TOPIC_URL = "http://ac.qq.com/Community/topicList?targetId=%s&page=1?_=%s";
    private static final String QQ_TOPIC_COMMENT_TEXT_URL = "https://ac.qq.com/Community/topicList?targetId=%s&page=%s?_=%s";

    private static final String QQ_EPISODE_TASK_URL = "https://android.ac.qq.com/7.21.3/Comic/comicChapterList/comic_id/%s";
    private static final String QQ_MOBILE_PLAY_COUNT_TASK_URL = "https://android.ac.qq.com/7.21.3/Comic/comicDetail/comic_id/%s";
    private static final String QQ_MOBILE_BILLBOARD_TASK_URL = "https://android.ac.qq.com/7.21.3/Rank/rankDetail/rank_id/%s/page/%s/user_qq/0/";

    public static final Map<Integer, String> TENGXUN_RANKINGID_MAP = new HashMap<Integer, String>() {{

        put(11, "飙升榜");
        put(12, "新作榜");
        put(13, "真相榜");
        put(14, "畅销榜");
        put(15, "月票榜");
        put(16, "TOP100");
        put(17, "男生榜");
        put(18, "女生榜");

    }};

    public Integer count = 0;

    private Site site = SiteBuilder.builder()
            .setDomain("ac.qq.com")
            .addHeader("Referer", "http://ac.qq.com/")
            .build();

    private PageRule rules = PageRule.build()
            .add("getWeekRankComic", page -> createTask(page))
            .add("ac.qq.com$", page -> processHome(page))
            .add("/Comic/all/nation/1/search/hot/page", page -> createTaskByAllComic(page))
            .add("/comicInfo", page -> analyzeDetail(page))
            .add("/userComicInfo", page -> analyzeTags(page))
            .add("/getMonthTicketInfo", page -> analyzeTicket(page))
            .add("/getAwardInfo", page -> analyzeDaShang(page))
            .add("/topicList", page -> analyzeTopic(page));

    private void processHome(Page page) {
        Job job = ((DelayRequest) page.getRequest()).getJob();

        Integer platformId = 32;
        Date day = DateUtil.getDayStartTime(new Date());
        String codePrefix = "qq-";
        String source = "HOME";

        Document document = page.getHtml().getDocument();
        Element banner = document.getElementById("in-banner");
        Elements aTags = banner.getElementsByTag("a");

        for (Element aTag : aTags) {
            String title = aTag.attr("title");
            String href = aTag.attr("href");
            int start = StringUtils.indexOf(href, "/id/");
            if (start<=0)continue;
            String codeNum = StringUtils.substring(href, start + 4);
            String code = codePrefix + codeNum;

            ComicBanner cb = new ComicBanner(code, platformId, day, title, source);
            putModel(page,cb);
        }


    }

    /**
     * 创建任务
     */
    private void createTask(Page page) {
        //创建搜索页任务
        Job job = ((DelayRequest) page.getRequest()).getJob();
        for (int i = 1; i <= 300; i++) {
            //获取到url.模拟翻页
            String newUrl = String.format(QQ_SEARCH_PAGE_URL, i);
            Job newJob = new Job(newUrl);
            DbEntityHelper.derive(job, newJob);
            putModel(page, newJob);
        }
        //创建榜单任务(手机端)

        for (Integer rankingId : TENGXUN_RANKINGID_MAP.keySet()) {
            //每页有10条，如果是Top100，翻10页，剩下类型榜单，翻3页
            Integer maxPage = rankingId.equals(16) ? 10 : 3;
            for (int i = 1; i <= maxPage; i++) {
                //获取到url.模拟翻页
                String newUrl = String.format(QQ_MOBILE_BILLBOARD_TASK_URL, rankingId, i);
                Job newJob = new Job(newUrl);
                DbEntityHelper.derive(job, newJob);
                putModel(page, newJob);
            }
        }

    }

    public static void main(String[] args) {
        String QQ_SEARCH_PAGE_URL = "http://ac.qq.com/Comic/all/nation/1/search/hot/page/%s";
        JSONArray arrays = new JSONArray();
        for (int i = 1; i <= 304; i++) {
            //获取到url.模拟翻页
            String newUrl = String.format(QQ_SEARCH_PAGE_URL, i);
            String s = HttpClientUtil.sendGetRequest(newUrl);
            Html html = new Html(s);
            Document document = html.getDocument();
            Elements elementsByClass = document.getElementsByClass("ret-search-result").get(0).getElementsByClass("ret-search-item clearfix");
            int finalI = i;
            elementsByClass.forEach(r->{
                String href =r.getElementsByClass("ret-works-title clearfix").get(0).getElementsByTag("a").attr("href");
                String code = "qq-" + href.substring(href.lastIndexOf("/")+1,href.length());
                String name = r.getElementsByClass("ret-works-title clearfix").get(0).getElementsByTag("a").text();
                String author = r.getElementsByClass("ret-works-author").get(0).text();
                String renqi = r.getElementsByClass("ret-works-tags").get(0).text();


                JSONObject map = new JSONObject();
                map.put("code",code);
                map.put("name",name);
                map.put("author",author);
                map.put("renqi",renqi);
                map.put("page", finalI);
                arrays.add(map);
            });
        }
        System.out.println(arrays.toString());
    }


    private void createTaskByAllComic(Page page) {
        Job job = ((DelayRequest) page.getRequest()).getJob();
        Document document = page.getHtml().getDocument();
        Elements items = document.getElementsByClass("ret-search-item clearfix");
        for (Element item : items) {
            Elements startReadButton = item.getElementsByClass("ret-works-view ui-btn-pink");
            if (null != startReadButton && !startReadButton.isEmpty()) {
                String href = startReadButton.get(0).attr("href");

                Job job2 = new Job(QQ_BASE_URL + href);
                DbEntityHelper.derive(job, job2);
                putModel(page, job2);
            }
        }
    }

    /**
     * 评论页
     */
    private void analyzeTopic(Page page) throws ParseException {
        Job job = ((DelayRequest) page.getRequest()).getJob();
        String url = job.getUrl();
        Integer pageNum = Integer.valueOf(url.substring(url.indexOf("page"), url.lastIndexOf("?")).replaceAll("[^(0-9)]", ""));
        String comicId = url.substring(url.indexOf("targetId=") + 9, url.indexOf("&page"));
        Date day = getTengxunComicDayByUrl(url);
        Document document = page.getHtml().getDocument();
        if (pageNum == 1) {
            Elements elements = document.getElementsByClass("commen-ft-ts");
            String count = elements.text();
            ComicTengxun tx = new ComicTengxun();
            tx.setComicId(comicId);
            tx.setCode("qq-" + comicId);
            tx.setDay(day);
            tx.setCommentNum(StringUtils.isBlank(count) ? 0 : Integer.valueOf(count.replace(",", "")));
            putModel(page, tx);
            commentText(page, url);
        } else {
            commentText(page, url);
        }
    }

    /**
     * 评论文本
     */
    private void commentText(Page page, String url) {
        Document document = page.getHtml().getDocument();
        String comicId = url.substring(url.indexOf("targetId=") + 9, url.indexOf("&page"));
        String code = "qq-" + comicId;
        Elements comments = document.getElementsByClass("comment-list-content-wr clearfix");
        for (Element e : comments) {
            String attr = e.attr("data-c");
            Long commentId = Long.valueOf(attr);
            String userCode = e.attr("data-u");
            String userName = TextUtils.removeEmoji(e.attr("data-nick"));
            Element contentA = e.getElementsByClass("comment-content-detail").get(0);
            String content = TextUtils.removeEmoji(contentA.text());
            if (content.equals("")) {
                continue;
            }
            Elements huiZan = e.getElementsByClass("comment-zan-num");
            Integer revertCount = Integer.valueOf(huiZan.get(0).text());
            Long supportCount = Long.valueOf(huiZan.get(1).text());
            String date = e.getElementsByClass("comment-time").get(1).text();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            Date commentCreateDate = null;
            try {
                commentCreateDate = sdf.parse(date);
            } catch (ParseException e1) {
                e1.printStackTrace();
            }
            ComicCommentText c = new ComicCommentText();
            c.setCommentId(commentId);
            c.setUserName(userName);
            c.setPlatformId(32);
            c.setCommentCreateTime(commentCreateDate);
            c.setCode(code);
            c.setDay(DateUtil.getDayStartTime(new Date()));
            c.setRevertCount(revertCount);
            c.setSupportCount(supportCount);
            c.setContent(content);
            c.setUserId(userCode);
            putModel(page, c);

        }
    }

    /**
     * 打赏页
     */
    private void analyzeDaShang(Page page) {
        Job job = ((DelayRequest) page.getRequest()).getJob();
        String url = job.getUrl();
        JSONObject json = null;
        try {
            json = JSONObject.parseObject(page.getJson().get());
        } catch (Exception e) {
            return;
        }
        if (null == json || json.size() == 0) return;
        String comicId = url.substring(url.indexOf("id/") + 3, url.indexOf("?_="));
        Date day = getTengxunComicDayByUrl(url);
        String count = json.getString("count");
        ComicTengxun tx = new ComicTengxun();
        tx.setCode("qq-" + comicId);
        tx.setComicId(comicId);
        tx.setDay(day);
        tx.setDashangNum(StringUtils.isBlank(count) ? 0 : Integer.valueOf(count.replace(",", "")));
        putModel(page, tx);
    }


    /**
     * 月票页
     */
    private void analyzeTicket(Page page) {
        Job job = ((DelayRequest) page.getRequest()).getJob();
        String url = job.getUrl();
        JSONObject json = null;
        try {
            json = JSONObject.parseObject(page.getJson().get());
        } catch (Exception e) {
            return;
        }
        if (null == json || json.size() == 0) return;
        String comicId = url.substring(url.indexOf("id/") + 3, url.indexOf("?_="));
        Date day = getTengxunComicDayByUrl(url);
        JSONObject monthTicket = json.getJSONObject("monthTicket");
    /*    String monthTotal = monthTicket.getString("monthTotal").replace(",","");
        String dayTotal = monthTicket.getString("dayTotal").replace(",","");*/
        Integer weekTicketNum = monthTicket.getInteger("mtNum");
        JSONObject rank = monthTicket.getJSONObject("rank");
        Integer rankNo = null;
        if (null != rank) {
            rankNo = rank.getInteger("rankNo");
        }
        ComicTengxun tx = new ComicTengxun();
        tx.setCode("qq-" + comicId);
        tx.setComicId(comicId);
        tx.setDay(day);
        tx.setWeeklyTicketNum(Integer.valueOf(weekTicketNum));
        tx.setWeeklyTicketRank(Integer.valueOf(rankNo));
        putModel(page, tx);
    }

    /**
     * 标签页
     */
    private void analyzeTags(Page page) {
        Job job = ((DelayRequest) page.getRequest()).getJob();
        String url = job.getUrl();
        JSONObject json = JSONObject.parseObject(page.getJson().get());
        if (!json.getInteger("status").equals(3)) {
            log.error("ac.qq.com error ,this url have error result -->{},this result is -->{}", url, json);
            return;
        }
        String comicId = url.substring(url.indexOf("comicId=") + 8, url.indexOf("?_="));
        JSONArray tags = json.getJSONArray("tag");
        List<String> tagList = new ArrayList<>();
        for (Object o : tags) {
            JSONObject data = (JSONObject) o;
            String tag = data.getString("name");
            tagList.add(tag);
        }
        String txTags = StringUtils.join(tagList, "/");
        Comic comic = new Comic();
        comic.setCode("qq-" + comicId);
        comic.setTags(txTags);
        putModel(page, comic);
    }


    /**
     * 解析详情页
     */
    private void analyzeDetail(Page page) {
        Job job = ((DelayRequest) page.getRequest()).getJob();
        Document document = page.getHtml().getDocument();

        //标题
        String title = document.getElementsByClass("works-intro-title ui-left").text();
        if (StringUtils.isBlank(title)) {
            //log.debug("ac.qq.com ,this url -->{},该url漫画需特殊处理", job.getUrl());
            return;
        }
        //漫画封面
        String headerImg = document.getElementsByClass("works-cover ui-left").get(0).getElementsByTag("a").get(0).getElementsByTag("img").get(0).attr("src");

        ComicTengxun tx = new ComicTengxun();
        Comic comic = new Comic();
        comic.setPlatformId(32);
        //基础信息
        Date day = DateUtil.getDayStartTime(new Date());
        String comicId = job.getUrl().substring(job.getUrl().indexOf("id/") + 3);
        comic.setCode("qq-" + comicId);
        tx.setCode("qq-" + comicId);
        tx.setComicId(comicId);
        tx.setDay(day);
        //设置标题
        comic.setName(title);
        comic.setHeaderImg(headerImg);

        //作者、人气、收藏数
        Elements authAndHotAndColl = document.getElementsByClass("works-intro-digi");
        if (null == authAndHotAndColl) {
            return;
        }
        Elements spans = authAndHotAndColl.get(0).getElementsByTag("span");
        for (Element span : spans) {
            String spanText = span.text();
            String em = span.getElementsByTag("em").text().trim();
            if (spanText.contains("作者：")) {
                comic.setAuthor(em);
            }
            if (spanText.contains("人气：")) {
                //  tx.setNum(str2Long(em));
            }
            if (spanText.contains("收藏数：")) {
                tx.setCollectNum(Integer.valueOf(em));
            }

        }
        //签约？、独家？、完结？
        String sign = document.getElementsByClass("ui-icon-sign").text();
        comic.setSigned(StringUtils.isBlank(sign) ? Boolean.FALSE : Boolean.TRUE);
        String exclusive = document.getElementsByClass("ui-icon-exclusive").text();
        comic.setExclusive(StringUtils.isBlank(exclusive) ? Boolean.FALSE : Boolean.TRUE);
        String isOver = document.getElementsByClass("works-intro-status").text();
        comic.setFinished(!StringUtils.equals(isOver, "已完结") ? Boolean.FALSE : Boolean.TRUE);

        //简介（详情）
        String detail = document.getElementsByClass("works-intro-short ui-text-gray9").text();
        comic.setIntro(detail);

        //红票(赞)
        String redcount = document.getElementById("redcount").text();
        tx.setPraiseNum(Integer.valueOf(redcount));

        //评分和评分人数
        Element score = document.getElementsByClass("works-score clearfix").get(0);
        String scoreNum = score.getElementsByTag("strong").text().trim();
        String scoreCount = score.getElementsByTag("span").text().trim();
        tx.setScoreNum(StringUtils.isBlank(scoreNum) ? 0 : Double.valueOf(scoreNum));
        tx.setScoreCount(StringUtils.isBlank(scoreCount) ? 0 : Integer.valueOf(scoreCount));

        //分集

        Integer episode = 0;
        //分集
        try {
            Element element = document.getElementsByClass("chapter-page-all works-chapter-list").get(0);
            Elements cls = element.getElementsByClass("works-chapter-item");
            episode = cls.size();

            Elements endTimeElements = document.getElementsByClass("ui-pl10 ui-text-gray6");

            //经常性为空
            if (null != endTimeElements && !endTimeElements.isEmpty()) {
                String endUpdateTimeStr = endTimeElements.get(0).text();
                Date endTime = DateUtils.parseDate(endUpdateTimeStr, "yyyy.MM.dd");
                //2019.02.19
                comic.setEndEpisodeTime(endTime);
            }
        } catch (Exception e) {
            throw new AntiSpiderException(e.getMessage() + page.getUrl().get(), e);
        }
        comic.setEpisode(episode);

        putModel(page, comic);
        putModel(page, tx);

        //分发二级任务
        Job job1 = new Job(String.format(QQ_TAG_URL, comicId, day.getTime()));
        Job job2 = new Job(String.format(QQ_TICKET_URL, comicId, day.getTime()));
        Job job3 = new Job(String.format(QQ_DASHANG_URL, comicId, day.getTime()));
//        Job job4 = new Job(String.format(QQ_TOPIC_URL, comicId, day.getTime()));
        //评论文本 + 评论数 2018-12-24
        try {
            for (int i = 1; i < 5; i++) {
                Job job4 = new Job(String.format(QQ_TOPIC_COMMENT_TEXT_URL, comicId, i, day.getTime()));
                DbEntityHelper.derive(job, job4);
                putModel(page, job4);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Job job5 = new Job(String.format(QQ_EPISODE_TASK_URL, comicId));
        Job job6 = new Job(String.format(QQ_MOBILE_PLAY_COUNT_TASK_URL, comicId));

        DbEntityHelper.derive(job, job1);
        DbEntityHelper.derive(job, job2);
        DbEntityHelper.derive(job, job3);
        DbEntityHelper.derive(job, job5);
        DbEntityHelper.derive(job, job6);
        putModel(page, job1);
        putModel(page, job2);
        putModel(page, job3);
        putModel(page, job5);
        putModel(page, job6);

    }


    /**
     * 反格式化数字
     */
  /*  public static Long str2Long(String num) {
        try {
            int index = num.indexOf("亿");
            if (index != -1) {
                String numFormat = num.substring(0, index);
                Double aDouble = Double.valueOf(numFormat);
                long number = (long) (aDouble * (double) 100000000);
                return number;
            }
            index = num.indexOf("万");
            if (index != -1) {
                String numFormat = num.substring(0, index);
                long number = (long) (Double.valueOf(numFormat) * (double) 10000);
                return number;
            }
            if (StringUtils.isBlank(num)) {
                return 0L;
            }
            return Long.valueOf(num);
        } catch (Exception e) {
            log.error("ac.qq.com error  数字格式化错误-->{}", num);
            return 0L;
        }
    }*/

    /**
     * 获取时间
     */
    private static Date getTengxunComicDayByUrl(String url) {
        Long aLong = Long.valueOf(url.substring(url.indexOf("?_=") + 3));
        Date date = new Date(aLong);
        return date;
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
