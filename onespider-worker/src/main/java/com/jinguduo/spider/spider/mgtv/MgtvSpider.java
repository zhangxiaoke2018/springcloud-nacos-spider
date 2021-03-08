package com.jinguduo.spider.spider.mgtv;

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
import com.jinguduo.spider.common.code.FetchCodeEnum;
import com.jinguduo.spider.common.constant.CommonEnum;
import com.jinguduo.spider.common.constant.FrequencyConstant;
import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.common.util.RegexUtil;
import com.jinguduo.spider.data.table.AutoFindLogs;
import com.jinguduo.spider.data.table.Category;
import com.jinguduo.spider.data.table.Show;
import com.jinguduo.spider.data.table.ShowCategoryCode;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.selector.Html;
import com.jinguduo.spider.webmagic.selector.Selectable;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.http.HttpStatus;

import java.util.List;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 16/6/24 下午2:06
 */
@Worker
@CommonsLog
public class MgtvSpider extends CrawlSpider {

    private Site sites = SiteBuilder.builder().setDomain("www.mgtv.com").build();

    private final String ep = "https://www.mgtv.com/v/1/%s/s/json.year.js";

    private final String epall = "http://v.api.hunantv.com/web/slist?callback=jQuery18208298748957556032_1467505228968&video_id=%s&collection_id=%s&_=1467505243405";

    private final String num = "http://videocenter-2039197532.cn-north-1.elb.amazonaws.com.cn//dynamicinfo?callback=jQuery18209559400354382939_1466759714593&vid=%s&_=1466759715614";

    private static final String TOTAL_PLAY_COUNT = "https://vc.mgtv.com/v2/dynamicinfo?cid=%s&vid=%s";

    private static final String EPI_JOB_URL = "http://pcweb.api.mgtv.com/episode/list?collection_id=%s&page=1";

    private static final String EPI_ZONGYI_JOB_URL = "http://pcweb.api.mgtv.com/variety/showlist?collection_id=%s";

    private static final String COMMENT_URL = "http://comment.mgtv.com/video_comment/list/?subject_id=%s&page=1";
    
    private static final String DETAIL_URL = "https://www.mgtv.com/h/%s.html";

    private PageRule rules = PageRule.build()
            .add("/v/\\d+/\\d+/f/\\d{7,10}.html", page -> prcoessDetailHtml(page))
            .add("/v/[0-9]/\\d{3,10}/$", page -> pageParentProcess(page))//网剧专题页处理逻辑
            .add("/json", page -> zongyiParamYear(page))
            .add("/h/", page -> processAnimeAndZongyi(page))//http://www.mgtv.com/h/168368.html 动漫专题页c 综艺
            .add("/b/", page -> prcoessDetailHtml(page))
            .add("/tv/", page -> processBannerDrama(page));

    private void processAnimeAndZongyi(Page page) {
        Job oldJob = ((DelayRequest) page.getRequest()).getJob();
//        //以家人之名
//        if(oldJob.getCode().equals("333900") && oldJob.getUrl().equals("https://www.mgtv.com/h/333900.html")){
//            log.info("Mgtv show`s code :"+ oldJob.getCode()+" 以家人之名 开始。");
//        }
        String chn = null;

        String html = page.getRawText();
        int start = html.indexOf("window.VIDEOINFO");
        if (start > 0) {
                String script = html.substring(start, html.indexOf("</script>", start));
                script = script.substring(script.indexOf("{"), script.lastIndexOf("wver:")) + "}";
                JSONObject jsonObject = JSONObject.parseObject(script);
                chn = jsonObject.getString("chn");
        }

        String code = FetchCodeEnum.getCode(page.getRequest().getUrl());
        Job pcJobs = MgtvSpider.totalPlayCount(page, code, "");
        DbEntityHelper.derive(oldJob, pcJobs);
        putModel(page, pcJobs);

        if (chn == null || !"综艺".equals(chn)) { //不是综艺
            Job epiJob = new Job(String.format(EPI_JOB_URL, code));
            DbEntityHelper.derive(oldJob, epiJob);
            putModel(page, epiJob);
        } else { //这里是综艺哦
            Job epiJob = new Job(String.format(EPI_ZONGYI_JOB_URL, code));
            DbEntityHelper.derive(oldJob, epiJob);
            putModel(page, epiJob);
        }
    }


    private void zongyiParamYear(Page page) {
        if (page.getStatusCode() != HttpStatus.OK.value()) {
            return;
        }

        String url = page.getRequest().getUrl();

        if (url.contains("year")) {
            getYear(page);
        } else {
            getShow(page);
        }
    }

    /**
     * 专题页处理逻辑
     *
     * @param page
     */
    private void pageParentProcess(Page page) {
        /**
         * 非网剧专题页 ==》 return
         */
        Selectable url_select = page.getUrl();

        String catory = url_select.regex("[0-9]").get();

        if (StringUtils.isBlank(catory) || !ShowCategoryCode.MgTvCategoryEnum.TELEPLAY.getCode().equals(catory)) {
            log.warn("not match this spider process catory :[" + catory + "],url:[" + url_select.get() + "]");
            return;
        }

        Document document = page.getHtml().getDocument();
        Elements lis = document.getElementsByClass("v-list-inner").get(0).getElementsByTag("li");

        if (null == lis && lis.size() <= 0) {
            log.error("cannot analysis html error，no video itmes,url:[" + page.getUrl() + "];");
            return;
        }
        Job oldJob = ((DelayRequest) page.getRequest()).getJob();
        /** 自定义变量 */
        String title = null;
        String code = null;
        String v_url = null;
        Job newJob = null, newJob_show = null, newJob_comment = null, newJob_barrage;
        Show show = null;

        List<Show> shows = Lists.newArrayListWithCapacity(lis.size() + 1);
        List<Job> jobs = Lists.newArrayListWithCapacity(lis.size() * 3 + 1);

        for (Element li : lis) {
            title = li.getElementsByTag("a").get(0).attr("title").trim();
            code = li.attr("id").replace("video-id-", "");
            v_url = li.getElementsByTag("a").get(0).attr("href");

            /** 播放量Jobs */
            newJob = DbEntityHelper.derive(oldJob, new Job(String.format(num, code)));
            newJob.setCode(code);
            jobs.add(newJob);

            /** 详情页Jobs */
            newJob_show = DbEntityHelper.derive(newJob, new Job());
            newJob_show.setUrl(v_url);
            jobs.add(newJob_show);

            /** 评论Jobs */
            newJob_comment = DbEntityHelper.derive(newJob, new Job());
            newJob_comment.setUrl(String.format(COMMENT_URL, code));
            newJob_comment.setFrequency(FrequencyConstant.COMMENT_COUNT);
            jobs.add(newJob_comment);

            /** 弹幕Jobs */
            // todo 其他剧还没有弹幕
            /*Job barrageJob = MgExtend.barrageTextJob("video_id", "clip_id");
            DbEntityHelper.derive(oldJob, barrageJob);
            barrageJob.setCode("video_id");
            putModel(page, barrageJob);*/

            /** 生成Shows */
            show = new Show(title, code, oldJob.getPlatformId(), oldJob.getShowId());
            show.setDepth(2);
            show.setParentCode(oldJob.getCode());
            Integer epi = Integer.valueOf(title.replaceAll("[^0-9]", ""));
            show.setEpisode(epi);
            shows.add(show);
        }
        //fixme 这里待测试
        String html = page.getRawText().replace(" ", "");
        String cid = RegexUtil.getDataByRegex(html, "cid:(\\d+)", 1);
        String vid = RegexUtil.getDataByRegex(html, "vid:(\\d+)", 1);
        Job job = MgtvSpider.totalPlayCount(page, cid, vid);
        if (job != null)
            jobs.add(job);
        putModel(page, shows);
        putModel(page, jobs);
    }

    /**
     * 视频详情页
     *
     * @param page
     */
    private void prcoessDetailHtml(Page page) {
        if (page.getStatusCode() != HttpStatus.OK.value()) {
            return;
        }
        Job oldJob = ((DelayRequest) page.getRequest()).getJob();
        if (oldJob == null) {
            return;
        }

        List<Job> jobs = Lists.newArrayList();
        Html html = page.getHtml();
        String url = page.getRequest().getUrl();
        String type = null;
            if (url.contains("/v/6/")) {//香蕉打卡 网综
                Job newJob = new Job(String.format(epall, url.split("/")[7], url.split("/")[5]));
                DbEntityHelper.derive(oldJob, newJob);
                jobs.add(newJob);
            } else if (url.contains("/v/1/")) {//网综
                Job newJob = new Job(String.format(ep, url.split("/")[5]));
                DbEntityHelper.derive(oldJob, newJob);
                jobs.add(newJob);
            } else if (url.contains("/v/2/")) {//网剧直接生成评论数jobs
                Job newJob = new Job(String.format(COMMENT_URL, oldJob.getCode()));
                DbEntityHelper.derive(oldJob, newJob);
                newJob.setFrequency(FrequencyConstant.COMMENT_COUNT);
                jobs.add(newJob);
            } else if (url.contains("/v/3/")) {//网大
                String vid = page.getUrl().regex("f/(\\d*).html", 1).get();
                //生成评论Job
                Job newJob = new Job(String.format(COMMENT_URL, vid));
                DbEntityHelper.derive(oldJob, newJob);//code 和网大show保持一致
                newJob.setFrequency(FrequencyConstant.COMMENT_COUNT);
                jobs.add(newJob);
            }
            /***新加***/
            else if(url.contains("/b/")) {
                String vidComment = page.getUrl().regex("/(\\d*).html", 1).get();
                //生成评论Job
                Job newJob = new Job(String.format(COMMENT_URL, vidComment));
                DbEntityHelper.derive(oldJob, newJob);//code 和网大show保持一致
                newJob.setFrequency(FrequencyConstant.COMMENT_COUNT);
                jobs.add(newJob);
            }
        String html_str = page.getRawText().replace(" ", "");

        String cid = RegexUtil.getDataByRegex(html_str, "cid:(\\d+)", 1);
        String vid = page.getUrl().regex("/(\\d*).html", 1).get();
//        String vid = RegexUtil.getDataByRegex(html_str, "vid:(\"\\d+\")", 1).replace("\"","");
        Job job = MgtvSpider.totalPlayCount(page, "", vid);
        if (job != null)
            jobs.add(job);
        putModel(page, jobs);
    }

    /**
     * 获取到年份,并生成对应年份的Job
     */
    private void getYear(Page page) {
            Job oldJob = ((DelayRequest) page.getRequest()).getJob();
            if (oldJob == null) {
                return;
            }
            String content = page.getRawText().replace("\"", "");
            List<String> years = Lists
                    .newArrayList(content.substring(content.indexOf("[") + 1, content.length() - 2).split(","));

            List<Job> jobs = Lists.newArrayList();
            for (String year : years) {
                Job newJob = new Job(String.format(page.getUrl().toString().replace("year", year)));
                DbEntityHelper.derive(oldJob, newJob);
                jobs.add(newJob);
            }
            putModel(page, jobs);
    }

    private void getShow(Page page) {
        Job oldJob = ((DelayRequest) page.getRequest()).getJob();
        if (oldJob == null) {
            return;
        }

        try {
            String content = page.getRawText();
            List<JSONObject> resShows = JSONArray
                    .parseArray(content.substring(content.indexOf("["), content.length() - 1), JSONObject.class);

            String vid = "";
            Integer episode = 0;
            List<Job> jobs = null;
            List<Show> shows = null;

            if (null != resShows && !resShows.isEmpty()) {
                jobs = Lists.newArrayListWithCapacity(2 * resShows.size());
                shows = Lists.newArrayListWithCapacity(resShows.size());
            }

            for (JSONObject resShow : resShows) {

                vid = resShow.getString("id");
                try {
                    episode = Integer.valueOf(resShow.getString("stitle").replaceAll("[^0-9]", ""));
                } catch (Exception e) {
                    episode = 0;
                    log.error(e.getMessage(), e);
                }

                try {
                    Job newJob = new Job(String.format(num, vid));
                    DbEntityHelper.derive(oldJob, newJob);
                    newJob.setCode(vid);
                    jobs.add(newJob);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }

                Show show = new Show(resShow.getString("title"), vid, oldJob.getPlatformId(),
                        oldJob.getShowId());
                show.setDepth(2);
                show.setEpisode(episode);
                shows.add(show);
                

                    /** 生成综艺期刊评论数job */
                    Job newJob_comment = new Job(String.format(COMMENT_URL, vid));
                    DbEntityHelper.derive(oldJob, newJob_comment);
                    newJob_comment.setCode(vid);
                    newJob_comment.setFrequency(FrequencyConstant.COMMENT_COUNT);

                    jobs.add(newJob_comment);
            }

            putModel(page, shows);
            putModel(page, jobs);

        } catch (Exception e) {
            log.error(page.getRequest().getUrl(), e);
        }
    }

    public static Job totalPlayCount(Page page, String cid,String vid) {
        Job job = ((DelayRequest) page.getRequest()).getJob();
        Job newJob = new Job(String.format(TOTAL_PLAY_COUNT, cid, vid));
        DbEntityHelper.derive(job, newJob);
        newJob.setCode(job.getCode());
        return newJob;
    }

    private void processBannerDrama(Page page){
        Html html = page.getHtml();
        
        List<Selectable> nodes = html.$("#honey-focus-list li").nodes();
        
        if(CollectionUtils.isEmpty(nodes)){
            log.error("MgtvAutoFindSpider result is empty!");
            return;
        }
        
        Job oldJob = ((DelayRequest)page.getRequest()).getJob();
        
        List<Show> shows = Lists.newArrayList();
        List<Job> jobs = Lists.newArrayList();
        List<AutoFindLogs> findLogs = Lists.newArrayList();
         
        nodes.stream().forEach(n->save(n,oldJob,jobs,findLogs,shows));

        if(CollectionUtils.isNotEmpty(jobs)){
            putModel(page, jobs);
        }
        if(CollectionUtils.isNotEmpty(findLogs)){
            putModel(page,findLogs);
        }
        if(CollectionUtils.isNotEmpty(shows)){
            putModel(page, shows);
        }
    }
    
    private void save(Selectable n, Job oldJob, List<Job> jobs, List<AutoFindLogs> findLogs, List<Show> shows) {
        String url = n.xpath("a//@href").get();
        String title = n.xpath("a//p[@class='til']//text()").get();
        String code = RegexUtil.getDataByRegex(url, "/b/(\\d+)/",1);
        
        if(StringUtils.isBlank(code)){
            return;
        }
        
        url = String.format(DETAIL_URL, code);
        Show show = new Show(title,code,CommonEnum.Platform.MG_TV.getCode(),0);
        show.setCategory(Category.TV_DRAMA.name());
        show.setUrl(url);
        show.setSource(3);//3-代表自动发现的剧
        shows.add(show);
        findLogs.add(new AutoFindLogs(title,Category.TV_DRAMA.name(),CommonEnum.Platform.MG_TV.getCode(),url,code));
        
        Job newJob = DbEntityHelper.deriveNewJob(oldJob, url);
        newJob.setCode(code);
        jobs.add(newJob);
    }

    @Override
    public PageRule getPageRule() {
        return this.rules;
    }

    @Override
    public Site getSite() {
        return this.sites;
    }


}
