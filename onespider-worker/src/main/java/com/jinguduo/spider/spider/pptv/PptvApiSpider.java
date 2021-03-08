package com.jinguduo.spider.spider.pptv;


import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import lombok.extern.apachecommons.CommonsLog;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.config.CookieSpecs;
import org.assertj.core.util.Lists;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.spider.listener.UserAgentSpiderListener;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.constant.FrequencyConstant;
import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.common.util.NumberHelper;
import com.jinguduo.spider.data.table.Show;
import com.jinguduo.spider.data.table.ShowCategoryCode;
import com.jinguduo.spider.data.table.ShowLog;
import com.jinguduo.spider.data.table.VipEpisode;
import com.jinguduo.spider.webmagic.Page;

@Worker
@CommonsLog
public class PptvApiSpider extends CrawlSpider {

    private Site site = SiteBuilder.builder()
            .setDomain("epg.api.pptv.com")
            .setCookieSpecs(CookieSpecs.IGNORE_COOKIES)
            // user-agent 动态化
            .addSpiderListener(new UserAgentSpiderListener())
            .build();
    
    //生成搜索页任务，抓取总播放量
    private final static String SEARCH_URL = "http://search.pptv.com/s_video?kw=%s";
    //评论量 这个接口获取的评论量是包含回复评论的量
    //private final static String COMMENT_COUNT_URL = "http://comment.pptv.com/api/v1/show.json/?ids=video_%s&pg=1&ps=20&tm=0&type=1";
    //评论量新的接口，v4版本以上v1版本已废弃
    private final static String COMMENT_COUNT_V4_URL = "http://apicdn.sc.pptv.com/sc/v4/pplive/ref/vod_%s/feed/count?appplt=web";
    //评论文本
    private final static String COMMENT_CONTENT_URL = "http://apicdn.sc.pptv.com/sc/v3/pplive/ref/vod_%s/feed/list?appplt=web&action=1&pn=0&ps=20";
    //弹幕
    private final static String DANMU_URL = "http://apicdn.danmu.pptv.com/danmu/v2/pplive/ref/vod_%s/danmu?pos=0";
    //特殊电影合集:如超能赌神->http://v.pptv.com/show/iaUkysRlic7y2QDnY.html?rcc_src=S1
    private final static List<String> SPECIAL_MOVIE_List = Lists.newArrayList("iaUkysRlic7y2QDnY");
    
    final static String PREFIX = "recDetailData(";
    final static String SUFFIX = ")";

    private PageRule rules = PageRule.build()
            .add("detail\\.api\\?cb=recDetailData", page -> videoInfoAnalysisProcess(page));

    private void videoInfoAnalysisProcess(Page page) {

        Job oldJob = ((DelayRequest) page.getRequest()).getJob();

        //json数据
        String rawText = page.getRawText();
        if(StringUtils.isBlank(rawText)){
            log.error("PptvApiSpider videoInfoAnalysisProcess get response json body empty! url :"+oldJob.getUrl());
            return;
        }
        //转换为标准的json格式并对象化
        JSONObject jsonV = JSONObject.parseObject(rawText.substring(rawText.indexOf(PREFIX) + PREFIX.length(), rawText.lastIndexOf(SUFFIX))).getJSONObject("v");
        if(jsonV==null){
            log.error("PptvApiSpider videoInfoAnalysisProcess get json v is empty! url:"+oldJob.getUrl());
            return;
        }
        //抓取总播放量
        getTotalPlayCount(page,jsonV);
        //解析视频列表 保存show以及分集播放量，生成分集评论和弹幕任务
        processShowListVideo(page,jsonV);
    }

    //从json object获取总播放量
    private void getTotalPlayCount(Page page,JSONObject o){
        Job oldJob = ((DelayRequest) page.getRequest()).getJob();
        //总播放量
        Long pv = o.getLong("pv");
        if (pv!=null && pv!=0) {
            putShowLog(page,oldJob,oldJob.getCode(),pv);
        }else{//未获取到播放量用备用方法从搜索页获取总播放量(此处不适用电影合集)
            String title = o.getString("title");
            //剧名编码
            try {
                title = URLEncoder.encode(title, "utf-8");
            } catch (UnsupportedEncodingException e) {
                log.error("PptvApiSpider getTotalPlayCount encode show name("+title+") error! url:"+oldJob.getUrl());
            }

            //生成总播放量搜索页任务
            Job newJob = new Job(String.format(SEARCH_URL, title));
            DbEntityHelper.derive(oldJob, newJob);
            newJob.setCode(oldJob.getCode());
            putModel(page, newJob);
        }
    }
    
    //从json object获取分集相关的信息以及生成其他分集任务
    private void processShowListVideo(Page page,JSONObject o){
        Job oldJob = ((DelayRequest) page.getRequest()).getJob();
        
        String url = oldJob.getUrl();
        String type = o.getString("type");
        String title = o.getString("title");

        JSONObject jsonVlistObj = o.getJSONObject("video_list");
        if(jsonVlistObj==null||jsonVlistObj.isEmpty()){
            log.error("PptvApiSpider videoInfoAnalysisProcess get json video_list is empty! url:"+url);
            return;
        }
        Object json = jsonVlistObj.get("video");
        if (json == null) {
            log.error("PptvApiSpider videoInfoAnalysisProcess get json video_list sub video is null! url:"+url);
            return;
        }

        /** 网大 */
        if (json instanceof JSONObject) {
            JSONObject videoJson = (JSONObject) json;
            JSONObject videoObj = videoJson.getJSONObject("_attributes");

            Long playCount = videoObj.getLong("pv");//电影播放量
            Integer vip = videoObj.getInteger("vip");//vip标记
            if(vip!=null&&vip==1){
                putVip(page,oldJob,oldJob.getCode());
            }
            //网大关联一级code
            putShowLog(page,oldJob,oldJob.getCode(),playCount);
            putCommentJob(page,oldJob,oldJob.getCode());
            putBarrageJob(page,oldJob,oldJob.getCode());
        } else if (json instanceof JSONArray && (ShowCategoryCode.PPTvCategoryEnum.FILM.getCode().equals(type)||SPECIAL_MOVIE_List.contains(oldJob.getCode()))) {// 电影合集
            // 从电影合集中取得当前code对应的电影的播放量
            // 人工的向请求API的url末尾添加了subId以便从合集中定位(方法不好，暂时使用)
            JSONArray vList = (JSONArray) json;
            String subId = "";
            if (url.contains("subId")) {
                // 手置参数放置url末尾
                subId = url.substring(url.indexOf("subId=") + 6);
            }
            if (StringUtils.isBlank(subId)) {
                log.error("PptvApiSpider videoInfoAnalysisProcess movie list get subId is null! url:"+url);
                return;
            }
            for (Object v : vList) {
                JSONObject videoJson = (JSONObject) v;
                JSONObject videoObj = videoJson.getJSONObject("_attributes");

                String vid = videoObj.getString("id");// 合集子电影VID
                // 若能匹配上subId则正确定位，生成该电影的其他任务和保存播放量
                if (StringUtils.isNotBlank(vid) && StringUtils.equals(vid, subId)) {
                    Long playCount = videoObj.getLong("pv");// 分集播放量
                    Integer vip = videoObj.getInteger("vip");//vip标记
                    if(vip!=null&&vip==1){
                        putVip(page,oldJob,oldJob.getCode());
                    }
                    //网大关联一级code
                    putShowLog(page,oldJob,oldJob.getCode(),playCount);
                    putCommentJob(page,oldJob,oldJob.getCode());
                    putBarrageJob(page,oldJob,oldJob.getCode());
                }
            }
        } else if (json instanceof JSONArray) {
            // 过滤掉之前的网大和电影合集 剩下的例如网剧，动漫，网络综艺均走这个逻辑
            JSONArray vList = (JSONArray) json;

            // create show , showLog, commentJob
            for (Object v : vList) {
                JSONObject obj = (JSONObject) v;
                JSONObject videoObj = obj.getJSONObject("_attributes");

                String vid = videoObj.getString("id");// 分集视频Id
                Long playCount = videoObj.getLong("pv");// 分集播放量
                Integer vip = videoObj.getInteger("vip");//vip标记
                String showName = videoObj.getString("title");
                Integer episode = 0;
                if (NumberHelper.isNumeric(showName)) {
                    episode = Integer.valueOf(showName);
                } else if (NumberHelper.isDouble(showName)) {
                    continue;
                } else {
                    if (showName.contains("预")) {
                        continue;
                    } else {
                        // 去除中文
                        String s = showName.replaceAll("[^0-9]", "");
                        //范围不超出最大值或不为空的
                        if (StringUtils.isNotBlank(s)) {
                            //pptv 超过Integer最大值的一般都是以yyyyMMdd xxxx组合的，这时截取前面8位即可,Integer最大值为10位数
                            if(s.length()>=10){
                                s = s.substring(0, 8);
                            }
                            episode = Integer.valueOf(s);
                        }
                    }
                }
                showName = new StringBuilder(title).append(showName).toString();

                /** 生成show */
                Show show = new Show(showName, vid, oldJob.getPlatformId(), oldJob.getShowId());
                show.setDepth(2);
                show.setEpisode(episode);
                show.setParentCode(oldJob.getCode());
                putModel(page, show);
                
                if(vip!=null&&vip==1){
                    putVip(page,oldJob,oldJob.getCode());
                }
                //网剧关联二级code
                putShowLog(page,oldJob,vid,playCount);
                putCommentJob(page,oldJob,vid);
                putBarrageJob(page,oldJob,vid);
            }
        }
    }
    
    private void putShowLog(Page page,Job oldJob,String code,Long pc){
        // 播放量
        ShowLog showLog = new ShowLog();
        DbEntityHelper.derive(oldJob, showLog);
        showLog.setCode(code);
        showLog.setPlayCount(pc);
        putModel(page, showLog);
    }
    
    private void putCommentJob(Page page,Job oldJob,String code){
        // 评论量
        Job commentJob = DbEntityHelper.deriveNewJob(oldJob,String.format(COMMENT_COUNT_V4_URL, code));
        commentJob.setCode(code);
        commentJob.setFrequency(FrequencyConstant.COMMENT_COUNT);
        putModel(page, commentJob);
        
        //评论文本
        Job commentTextJob = DbEntityHelper.deriveNewJob(oldJob,String.format(COMMENT_CONTENT_URL, code));
        commentTextJob.setCode(code);
        commentTextJob.setFrequency(FrequencyConstant.COMMENT_TEXT);
        putModel(page, commentTextJob);
    }
    
    private void putBarrageJob(Page page,Job oldJob,String code){
        // 评论量
        Job danmuJob = DbEntityHelper.deriveNewJob(oldJob, String.format(DANMU_URL, code));
        danmuJob.setCode(code);
        danmuJob.setFrequency(FrequencyConstant.BARRAGE_TEXT);
        putModel(page, danmuJob);
    }
    
    private void putVip(Page page,Job oldJob,String code){
        // vip标志
        VipEpisode vip = new VipEpisode();
        vip.setCode(code);
        vip.setPlatformId(oldJob.getPlatformId());
        putModel(page, vip);
    }
    
    @Override
    public Site getSite() {
        return site;
    }

    @Override
    public PageRule getPageRule() {
        return rules;
    }

}
