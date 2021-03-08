package com.jinguduo.spider.spider.bilibili;



import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.code.FetchCodeEnum;
import com.jinguduo.spider.common.constant.CommonEnum;
import com.jinguduo.spider.common.constant.FrequencyConstant;
import com.jinguduo.spider.common.util.DateUtil;
import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.common.util.RegexUtil;
import com.jinguduo.spider.data.table.BilibiliFansCount;
import com.jinguduo.spider.data.table.BilibiliVideoScore;
import com.jinguduo.spider.data.table.Category;
import com.jinguduo.spider.data.table.Show;
import com.jinguduo.spider.webmagic.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Slf4j
@Worker
public class BiliBiliPlayPageSpider extends CrawlSpider {

    private final String REPLY_URL = "https://api.bilibili.com/x/v2/reply?pn=%d&type=1&oid=%s&sort=0";//评论
    private final String PLAY_COUNT_URL = "https://bangumi.bilibili.com/ext/web_api/season_count?season_id=%s&season_type=4";// 播放量

    //播放量和分集
    private static final String OLD_PLAY_COUNT_URL = "http://bangumi.bilibili.com/jsonp/seasoninfo/%s.ver?callback=seasonListCallback&jsonp=jsonp";

    //剧集链接（新 2019.11.8）
    private static final String NEW_SHOWS_LIST = "https://api.bilibili.com/pgc/web/season/section?season_id=%s";

    // 旧版url
    //http://bangumi.bilibili.com/anime/22059
    private static final String old_v1_common_url = "http://bangumi.bilibili.com/anime/%s";

    //评论人数
    private final String COMMENT_NUMBER_URL="https://api.bilibili.com/x/v2/reply?&pn=1&type=1&oid=%s";

    //弹幕
    private static final String COMMENT_URL = "https://comment.bilibili.com/%s.xml";

    //自动发现的番剧 https://www.bilibili.com/bangumi/play/ss24588
    private Site site = SiteBuilder.builder().setDomain("www.bilibili.com").build(); //播放页 https://www.bilibili.com/video/av12965661/
    private PageRule rules = PageRule.build()
            .add("/video/*",page -> getContent(page))
            .add("/bangumi/play/",page -> processAutoFindJapanAnime(page))
            .add("/bangumi/media/",page -> processAnime(page));

    //处理自动发现的番剧
    private void processAutoFindJapanAnime(Page page) {
        Job oldJob = ((DelayRequest) page.getRequest()).getJob();
        Document document = page.getHtml().getDocument();
        Element media_module = document.getElementById("media_module");
        Element titleElement = media_module.getElementsByClass("media-title").first();

        String url = titleElement.attr("href");
        String name = titleElement.attr("title");
        String code = FetchCodeEnum.getCode(url);

        if(name.contains("DVD版")||name.contains("网络版")||name.endsWith("CUT")){
            return;
        }
        if(StringUtils.isBlank(code)){
            return;
        }
        Show show = new Show(name,code,CommonEnum.Platform.BILI_BILI.getCode(),0);
        show.setUrl(url);
        show.setSource(3);//3-代表自动发现的剧
        show.setCategory(Category.JAPAN_ANIME.name());

        putModel(page,show);
    }


    private void processAnime(Page page) throws ParseException {
        Job oldJob = ((DelayRequest) page.getRequest()).getJob();
        Pattern p = Pattern.compile("season_id\":(\\d+),", Pattern.MULTILINE);
        Matcher matcher = p.matcher(page.getRawText());
        String seasonId = "";
        if(matcher.find()){
            seasonId = matcher.group(1);
        }
        Job newJob = new Job(String.format(PLAY_COUNT_URL, seasonId));
        DbEntityHelper.derive(oldJob, newJob);
        putModel(page, newJob);

        //适配旧任务，拿到ssid 创建任务
        Job oldCreateUrlJob = new Job(String.format(OLD_PLAY_COUNT_URL, seasonId));
        DbEntityHelper.derive(oldJob, oldCreateUrlJob);
        putModel(page, oldCreateUrlJob);
        Double score = 0d;
        Integer scoreNumber = 0;
        Document document=page.getHtml().getDocument();
        //b站评分 评分人数
        try {
            Elements scoreContents = document.getElementsByClass("media-info-score-content");
            score = Double.parseDouble(scoreContents.get(0).text());
            Elements sorceNumberElements = document.getElementsByClass("media-info-review-times");
            String scoreNumberString = sorceNumberElements.text();
            scoreNumber = Integer.parseInt(scoreNumberString.replace("人评", ""));
        }catch (Exception e){
            log.error("no score and scoreNumber . code : " + oldJob.getCode());
        }
        Job showListJob =new Job(String.format(NEW_SHOWS_LIST,seasonId));
        DbEntityHelper.derive(oldJob, showListJob);
        putModel(page, showListJob);

        BilibiliVideoScore b=new BilibiliVideoScore();
        b.setCode(oldJob.getCode());
        b.setScore(score);
        b.setScoreNumber(scoreNumber);
        b.setDay(DateUtil.getDayStartTime(new Date()));
        putModel(page,b);

        /***
         * 总播放 、追番人数、总弹幕
         */
        String PlayCount = page.getHtml().xpath("//div[@class=\"media-info-count\"]/span[@class=\"media-info-count-item media-info-count-item-play\"]/em/text()").get();
        String BanCount = page.getHtml().xpath("//div[@class=\"media-info-count\"]/span[@class=\"media-info-count-item media-info-count-item-fans\"]/em/text()").get();
        String DanmuCount = page.getHtml().xpath("//div[@class=\"media-info-count\"]/span[@class=\"media-info-count-item media-info-count-item-review\"]/em/text()").get();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
        String dates = df.format(new Date());
        BilibiliFansCount bilibiliFansCount = new BilibiliFansCount();
        bilibiliFansCount.setCode(oldJob.getCode());
        bilibiliFansCount.setScore(score);
        bilibiliFansCount.setScoreNumber(scoreNumber);
        bilibiliFansCount.setTotalPlayCount(numberFormat(PlayCount));
        bilibiliFansCount.setTotalFansCount(numberFormat(BanCount));
        bilibiliFansCount.setTotalDanmuCount(numberFormat(DanmuCount));
        bilibiliFansCount.setDay(df.parse(dates));
        //System.out.println("追番人数: "+BanCount);
        //log.info("B站追番人数抓取,code："+oldJob.getCode() );
        putModel(page,bilibiliFansCount);
    }


    /**
     * 抓取播放页，根据播放页面去biliBiliApiSpider生成爬取评论的任务
     * @param page
     */
    private void getContent(Page page) {
        Job oldJob = ((DelayRequest) page.getRequest()).getJob();
        String aid = RegexUtil.getDataByRegex(oldJob.getUrl(), "/video/av(\\d*)");
        String cid = RegexUtil.getDataByRegex(page.getRawText(), "cid=(\\d*)");

        //生成评论爬取任务
        String replayUrl = String.format(REPLY_URL, 1, aid);
        log.debug("create bilibili replay job url:" + replayUrl + " platformId:" + oldJob.getPlatformId());
        Job replyJob = new Job(replayUrl);
        DbEntityHelper.derive(oldJob, replyJob);
        replyJob.setFrequency(FrequencyConstant.COMMENT_TEXT);
        putModel(page, replyJob);

    }


    public static Integer numberFormat(String Count){
        Integer count;
        if(Count.contains("万")){
            Double aDouble = Double.valueOf(Count.replace("万", ""));
            count = (int)(aDouble * 10000);
            return count;
        }else if(Count.contains("亿")){
            Double aDouble = Double.valueOf(Count.replace("亿", ""));
            count = (int)(aDouble * 100000000);
            return count;
        }
        else {
            count = Integer.valueOf(Count);
            return count;
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
