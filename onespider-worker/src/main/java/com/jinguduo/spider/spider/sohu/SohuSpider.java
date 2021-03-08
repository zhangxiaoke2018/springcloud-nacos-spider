package com.jinguduo.spider.spider.sohu;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.config.CookieSpecs;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.spider.listener.UserAgentSpiderListener;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.constant.FrequencyConstant;
import com.jinguduo.spider.common.exception.PageBeChangedException;
import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.data.table.Show;
import com.jinguduo.spider.data.table.ShowLog;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.selector.Html;

@Worker
@Slf4j
public class SohuSpider extends CrawlSpider{

    private Site sites = SiteBuilder.builder()
            .setDomain("tv.sohu.com")
            .setCookieSpecs(CookieSpecs.IGNORE_COOKIES)
            .addSpiderListener(new UserAgentSpiderListener())
            .addSpiderListener(new SohuSpiderUrlRewriter())
            .addDownloaderListener(new SohuSpiderCookiesGenerator())
            .addHeader("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
            .addHeader("accept-language", "zh-CN,zh;q=0.9")
            .addHeader("cache-control", "no-cache")
            .addHeader("referer", "https://tv.sohu.com/")
            .build();

    private final static String URL_ALL_DATA = "http://tv.sohu.com/item/VideoServlet?source=sohu&id=%s&year=%s&month=0&page=1";
    private final static String URL_DRAMA_PAGE = "http://pl.hd.sohu.com/videolist?playlistid=%s&order=0&cnt=1&callback=__get_videolist";
    //此url可获取到专辑总播放量
    private final static String TOTAL_PLAY_COUNT = "http://count.vrs.sohu.com/count/queryext.action?plids=%s&callback=playCountVrs";
    //comment count
    private final static String URL_COMMENT_COUNT = "http://changyan.sohu.com/api/2/topic/load?client_id=cyqyBluaj&topic_url=%s&topic_source_id=%s&topic_category_id=%s";
    private final static String URL_DANMU_1 = "http://api.danmu.tv.sohu.com/danmu?act=dmlist_v2&vid=%s&page=1&pct=2&request_from=sohu_vrs_player&o=1&aid=%s";
    private final static String URL_DANMU_2 = "http://api.danmu.tv.sohu.com/danmu?act=dmlist_v2&vid=%s&page=1&pct=2&request_from=sohu_vrs_player&o=4&aid=%s";

    private PageRule rules = PageRule.build()
            .add("\\.html", page -> processHtml(page))  //网综、网大专题页
            .add("VideoServlet", page -> processNetVarietyYears(page))  //网综分年获取
            .add("\\/s\\d*\\/\\w*\\/", page -> processNetDrama(page))  //网剧
            ;

    /**
     * 搜狐专题页处理
     * 1。生成播放量Job
     * 2. 生成评论量Job
     * @param page
     */
    private void processHtml(Page page){
        Job oldJob = ((DelayRequest) page.getRequest()).getJob();
        Html html = page.getHtml();

        String topicUrl = oldJob.getUrl();
        String playlistId = parseCode(page);
        String commentFlag = html.regex("comment_c_flag=\"(\\w*)", 1).get();
        String vid = html.regex("vid\\s*=\\s*\"(\\d*)",1).get();
        
        if (StringUtils.isBlank(playlistId)) {
        	throw new PageBeChangedException("The playlistId is null");
        }
        //播放量job
        //评论job
        //弹幕job
        String topicSourceId = commentFlag + playlistId;
        //playCount
        Job playJob = this.totalPlayCount(page, playlistId);
        putModel(page, playJob);
        //commentCount
        Job commentJob = DbEntityHelper.deriveNewJob(oldJob,String.format(URL_COMMENT_COUNT, topicUrl, topicSourceId, ""));
        putModel(page, commentJob);

        //danmu
        Job danmuJob1 = new Job(String.format(URL_DANMU_1,vid,playlistId));
        DbEntityHelper.derive(oldJob,danmuJob1);
        danmuJob1.setFrequency(FrequencyConstant.BARRAGE_TEXT);
        putModel(page, danmuJob1);
        
        Job danmuJob2 = new Job(String.format(URL_DANMU_2,vid,playlistId));
        DbEntityHelper.derive(oldJob,danmuJob2);
        danmuJob2.setFrequency(FrequencyConstant.BARRAGE_TEXT);
        putModel(page, danmuJob2);

        //有一些剧的新链接也进入到这里面了,先让他分出来集
        dramaItemsJob(page, oldJob,playlistId);
        // Element vYears 有的页面没有了
        try {
            //下面是之前的逻辑,保持原来吧。。
            //暂定页面取不到专辑为网络大电影。。。。
            //不仅是网络大电影了,动漫也是
            String albumId = html.$("em.vBox-warn", "data-plid").get();
            if (StringUtils.isBlank(albumId)) {
                return;
            }
            Set<String> years = Sets.newHashSet();
            //默认添加variety_year中的值
            String text = page.getRawText();
            int idx = text.indexOf("variety_year = \"");
            String sy = text.substring(idx + 16, idx + 20);
            years.add(sy);

            Document document = html.getDocument();
            Elements vYears = document.getElementsByClass("v-year");
            if (vYears != null && vYears.size() > 0) {
                Elements ems = vYears.get(0).getElementsByTag("em");
                for (Element em : ems) {
                    years.add(em.text());
                }
            }
            if (StringUtils.isNotBlank(albumId)) {
                //生成任务
                for (String year : years) {
                    Job newJob = null;
                    newJob = new Job(String.format(URL_ALL_DATA, albumId, year));
                    DbEntityHelper.derive(oldJob, newJob);
                    newJob.setCode(albumId);
                    putModel(page, newJob);
                }
                Job job = totalPlayCount(page, albumId);
                putModel(page, job);
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }


    }

    private void processNetVarietyYears(Page page){
        Job job = ((DelayRequest) page.getRequest()).getJob();

        JSONObject jsonObject = JSONObject.parseObject(page.getRawText());
        List<JSONObject> videos = (List<JSONObject>) jsonObject.get("videos");

        if( null == videos || videos.isEmpty()){
        	throw new PageBeChangedException("get variety list fail");
        }
        List<ShowLog> showLogs = Lists.newArrayListWithCapacity(videos.size());
        List<Show> shows = Lists.newArrayListWithCapacity(videos.size());
        List<Job> jobList = Lists.newArrayListWithCapacity(videos.size());

        //评论参数
        String topicUrl = "", topicSourceId = "", topicCategoryId = "";

        for (JSONObject video : videos) {

            Long playCount = video.getLong("playCount");
            String videoName = video.getString("title");
            String vid = video.getString("videoTvId");
            Integer episode = video.getInteger("showDate");

            topicUrl = video.getString("url");
            topicSourceId = video.getString("id");
            topicCategoryId = video.getString("albumCategoryId");

            Date tvYear = null;
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                tvYear = sdf.parse(video.getString("showDate"));
            } catch (ParseException e) {
                log.error(e.getMessage(), e);
            }

            Show show = new Show(videoName,vid,job.getPlatformId(),job.getShowId());
            show.setReleaseDate(tvYear);
            show.setDepth(2);
            show.setEpisode(episode);
            show.setParentCode(job.getCode());
            shows.add(show);

            //综艺生成评论Job
            Job commentJob = new Job(String.format(URL_COMMENT_COUNT,topicUrl,topicSourceId,topicCategoryId));
            DbEntityHelper.derive(job,commentJob);
            commentJob.setCode(vid);
            commentJob.setFrequency(FrequencyConstant.COMMENT_COUNT);

            jobList.add(commentJob);

            ShowLog showLog = new ShowLog();
            DbEntityHelper.derive(job,showLog);
            showLog.setPlayCount(playCount);
            showLog.setCode(vid);
            showLogs.add(showLog);

        }
        putModel(page,showLogs);
        putModel(page,shows);
        putModel(page,jobList);
    }

    private void processNetDrama(Page page){
        String playlistId = parseCode(page);
        
        Job oldJob = ((DelayRequest) page.getRequest()).getJob();
        dramaItemsJob(page, oldJob,playlistId);
        putModel(page, totalPlayCount(page, playlistId));
    }
    
    /**
     * 覆盖三种情况：
     *   <li> var playlistId="6824976";
     *   <li> var playlistId = "9485021";
     *   <li> var PLAYLIST_ID = "9457553";
     * @param page
     * @return
     */
    private String parseCode(Page page) {
    	Job oldJob = ((DelayRequest) page.getRequest()).getJob();
        String playlistId = page.getHtml().regex("(?:playlistId|PLAYLIST_ID)\\s*=\\s*\"(\\d*)", 1).get();
        if (StringUtils.isBlank(playlistId) || "0".equals(playlistId)) {
            playlistId = oldJob.getCode();
        }
    	return playlistId;
    }

    /** 获取专栏所有的剧集列表 */
    private void dramaItemsJob(Page page, Job oldJob, String playlistId) {
        Job newJob = new Job(String.format(URL_DRAMA_PAGE,playlistId));
        DbEntityHelper.derive(oldJob,newJob);
        putModel(page, newJob);
    }

    private Job totalPlayCount(Page page,String plid) {
    	if (StringUtils.isBlank(plid) || "0".equals(plid)) {
			throw new PageBeChangedException("The plid is error");
		}
        Job job = ((DelayRequest) page.getRequest()).getJob();
        Job newJob = new Job(String.format(TOTAL_PLAY_COUNT, plid));
        DbEntityHelper.derive(job, newJob);
        newJob.setCode(job.getCode());
        return newJob;
    }

    @Override
    public PageRule getPageRule() {
        return this.rules;
    }

    @Override
    public Site getSite() {
        return sites;
    }

}
