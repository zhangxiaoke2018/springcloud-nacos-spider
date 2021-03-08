package com.jinguduo.spider.spider.kankan;

import java.util.List;

import lombok.extern.apachecommons.CommonsLog;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.google.common.collect.Lists;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.constant.FrequencyConstant;
import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.common.util.NumberHelper;
import com.jinguduo.spider.data.table.Show;
import com.jinguduo.spider.data.table.ShowLog;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.selector.Html;
import com.jinguduo.spider.webmagic.selector.Selectable;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @DATE 16/7/7 下午4:02
 */
@Worker
@CommonsLog
public class KanKanPlayCountSpider extends CrawlSpider {

    //评论量
    private final String COMMENT_COUNT_URL = "http://api.t.kankan.com/weibo_list_vod.json?jsobj=hotscomment&hot=1&movieid=%s";
    //评论文本
    private final String COMMENT_TEXT_URL = "http://api.t.kankan.com/weibo_list_vod.json?movieid=%s&perpage=25&page=1";
    //剧集列表
    private final String V_LIST_URL = "http://movie.kankan.com/down_js/%s/%s.js";
    //弹幕
    private final String danmu_url = "http://point.api.t.kankan.com/danmu.json?a=show&subid=%s&start=1&end=200&jsobj=danmuobj";

    private Site site = SiteBuilder.builder().setDomain("movie.kankan.com").build();

    private PageRule rule = PageRule.build()
            .add("movie.kankan.com/[^down_js]",page -> processMain(page))
            .add("movie.kankan.com/down_js/(.*?).js",page -> processList(page));

    /***
     * 响巢看看视频专题页(网大，网剧公用)
     * 1.抓取播放量
     * 2.抓取评论数（网剧：所有剧集评论都一样）随意生成一个分集
     * @param page
     */
    private void processMain(Page page) {
        Job oldJob = ((DelayRequest) page.getRequest()).getJob();
        String jobCode = oldJob.getCode();
        List<Job> jobList = Lists.newArrayList();
        Html html = page.getHtml();
        
        String showCode = null;//网剧或者综艺show code
        String catgory = html.regex("movieInfo.movie_type = \\'(.*?)\\'",1).get();
        
        getTotalPlayCount(page,oldJob);//总播放量

        /** 如果是网剧或者综艺生成show（随意生成一个show） */
        if(!StringUtils.contains(catgory, "movie")){//vmovie-网络电影 movie-电影
            Job showListJob = DbEntityHelper.deriveNewJob(oldJob, String.format(V_LIST_URL, jobCode.substring(0,2), jobCode));
            jobList.add(showListJob);
            
            String tt = "";
            try {
                Selectable selectable = html.$(".fenji_div").xpath("//ul/li[1]/a");
                showCode = selectable.xpath("///@subid").get();

                Show show = new Show();
                show.setDepth(2);
                show.setParentId(oldJob.getShowId());
                show.setCode(showCode);
                show.setParentCode(oldJob.getCode());
                show.setName(selectable.xpath("///@title").get());
                show.setPlatformId(oldJob.getPlatformId());
                tt = selectable.xpath("///@title").get();
                String episode = selectable.xpath("///@title").regex("[0-9]+",0).get();
                if(StringUtils.isNotBlank(episode) && NumberHelper.isNumeric(episode)){
                    show.setEpisode(Integer.valueOf(episode));
                }
                putModel(page,show);
            } catch (Exception e){
                log.error("KanKanPlayCountSpider processMain get epi error! url:"+oldJob.getUrl()+" title:"+tt);
            }
        }
        
        //因为kankan只有总评论量，而我们的评论是用分集加和的，为了保证kankan的评论量能有一个数暂这个处理
        //生成抓取评论量的任务
        Job commentCountJob = DbEntityHelper.deriveNewJob(oldJob, String.format(COMMENT_COUNT_URL,oldJob.getCode()));
        if(StringUtils.isNotBlank(showCode)){
            commentCountJob.setCode(showCode);//有分集用分集的code（网剧，综艺） 
        }
        jobList.add(commentCountJob);

        //生成抓取评论文本的任务
        Job commentTextJob = DbEntityHelper.deriveNewJob(oldJob,String.format(COMMENT_TEXT_URL,oldJob.getCode()));
        if(StringUtils.isNotBlank(showCode)){
            commentTextJob.setCode(showCode);//有分集用分集的code（网剧，综艺）
        }
        commentTextJob.setFrequency(FrequencyConstant.COMMENT_TEXT);
        jobList.add(commentTextJob);

        if (!jobList.isEmpty()){
            putModel(page,jobList);
        }
    }
    
    //从页面获取总播放量，这里有2种结构的页面(1.http://movie.kankan.com/movie/85337 2.http://movie.kankan.com/movie/68810)
    private void getTotalPlayCount(Page page,Job oldJob){
        Html html = page.getHtml();
        /** 抓取播放量 */
        Long playCount = -1L;
        String span = html.xpath("//*[@id=\"wrapper_box\"]/div[@class=\"side\"]/div[@class=\"box_sidetop\"]/ul[2]/li[1]/span").get();
        if (StringUtils.isBlank(span)) {
            playCount = NumberHelper.parseLong(html.$("#movie_basic_info").regex("<li>播放次数：<span>(.*?)</span></li>",1).replace(",","").regex("([0-9]+)",1).get(),-1);
        } else {
            playCount = NumberHelper.parseLong(span.substring(span.indexOf(">") + 1, span.lastIndexOf("<")).replace(",",""), -1);
        }
        ShowLog showLog = new ShowLog();
        DbEntityHelper.derive(oldJob, showLog);
        showLog.setPlayCount(playCount);
        putModel(page,showLog);
    }

    private void processList(Page page) {
        Job oldJob = ((DelayRequest) page.getRequest()).getJob();
        String rawText = page.getRawText();

        List<Show> listShow = Lists.newArrayList();
        List<Job> jobList = Lists.newArrayList();

        if (StringUtils.isBlank(rawText)) {
            log.error("KanKanPlayCountSpider processList response body is null! url:"+oldJob.getUrl());
            return;
        }

        rawText = rawText.substring(rawText.indexOf("=") + 1);

        List<List> listP = JSONArray.parseArray(rawText, List.class);

        if(null==listP || listP.isEmpty()){
            log.error("KanKanPlayCountSpider processList no video list! url:"+page.getUrl());
            return;
        }
        List<List> list1 = listP.get(1);
        if(null==list1 || list1.isEmpty()){
            log.error("KanKanPlayCountSpider processList video list one is empty! url:"+page.getUrl());
            return;
        }
        for (List l : list1) {
            List<String> ls = l;
            String name = ls.get(0);
            String vid = ls.get(5);
            try {
                Show show = new Show();
                show.setDepth(2);
                show.setParentId(oldJob.getShowId());
                show.setCode(vid);
                show.setParentCode(oldJob.getCode());
                show.setName(name);
                show.setPlatformId(oldJob.getPlatformId());
                String episode = name.replaceAll("[^0-9]", "");//TODO http://movie.kankan.com/movie/68810  有9&10这种分集的情况
                if (StringUtils.isNotBlank(episode) && NumberHelper.isNumeric(episode)) {
                    show.setEpisode(Integer.valueOf(episode));
                }
                listShow.add(show);
            } catch (Exception e) {
                log.error("KanKanPlayCountSpider processList create show error! url:"+oldJob.getUrl()+" error:"+e.getMessage(), e);
            }
            //弹幕任务
            createBarrageJob(jobList, oldJob, vid);
        }

        if (CollectionUtils.isNotEmpty(listShow)) {
            putModel(page,listShow);
        }
        if (CollectionUtils.isNotEmpty(jobList)) {
            putModel(page,jobList);
        }
    }

    private void createBarrageJob(List<Job> jobList, Job oldJob, String code) {
        Job barrageJob = DbEntityHelper.deriveNewJob(oldJob, String.format(danmu_url, code));
        barrageJob.setCode(code);
        jobList.add(barrageJob);
    }

    @Override
    public Site getSite() {
        return this.site;
    }

    @Override
    public PageRule getPageRule() {
        return rule;
    }

}
