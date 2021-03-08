package com.jinguduo.spider.spider.youku;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jinguduo.spider.common.constant.CommonEnum;
import com.jinguduo.spider.data.table.Show;
import com.jinguduo.spider.data.table.ShowPopularLogs;
import com.jinguduo.spider.webmagic.selector.Html;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.config.CookieSpecs;

import com.google.common.collect.Maps;
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
import com.jinguduo.spider.common.util.RegexUtil;
import com.jinguduo.spider.webmagic.Page;


/**
 * <p>优酷的v.youku.com域名有访问频率限制
 */
@Worker
@CommonsLog
public class YoukuDetailSpider extends CrawlSpider {

    private Site site = SiteBuilder.builder()
            .setDomain("v.youku.com")
            .setCookieSpecs(CookieSpecs.IGNORE_COOKIES)
            .addSpiderListener(new UserAgentSpiderListener())
            .build();
    
    // 播放量 APP 接口
    private final static String PLAYCOUNT_MOBILE_DETAIL_URL = "http://detail.mobile.youku.com/shows/%s/reverse/videos?pid=4e21c9dc68a77970&guid=462adfc4ccbcb0b514b979afe8da1f79&imei=860123456789012&_t_=1526458086&mac=02:00:00:00:00:00&ver=6.4.7&e=md5&_s_=a52c553c9b668ea42fc2ad233b5f3728&operator=%%E4%%B8%%AD%%E5%%9B%%BD%%E7%%A7%%BB%%E5%%8A%%A8_46000&network=WIFI&fields=vid%%7Ctitl%%7Clim%%7Cis_new%%7Cpv%%7Cimg&pg=1&pz=200&area_code=1";

    //评论量
    private final static String COMMENT_COUNT_URL = "http://p.comments.youku.com/ycp/comment/pc/commentList?app=100-DDwODVkv&objectId=%s&objectType=1&listType=0&currentPage=1&pageSize=1&sign=3df5f4567f169972380e1ee9e070d593&time=1484114120";
    //评论文本
    private final static String COMMENT_TEXT_URL = "http://api.mobile.youku.com/video/comment/list/new?vid=%s&pl=30&pg=0";
    //弹幕
    private final static String DANMU_URL= "http://service.danmu.youku.com/list?iid=%s&ct=1001&type=1&mcount=5&mat=1";
    //类型     96-电影，172-原创，171-微电影  97-网剧  85-网综 100-动漫
    //private static final List<String> categoryList = Lists.newArrayList("96","97","171","172","85","100");
    //热度
    private final static String HOT_COUNT_URL = "https://acs.youku.com/h5/mtop.youku.haixing.play.h5.detail/1.0/?jsv=2.5.0&appKey=24679788&&v=1.0&type=originaljson&dataType=json&api=mtop.youku.haixing.play.h5.detail";
    private final static String data = "{\"device\":\"H5\",\"layout_ver\":\"100000\",\"system_info\":\"{\\\"device\\\":\\\"H5\\\",\\\"pid\\\":\\\"0d7c3ff41d42fcd9\\\",\\\"guid\\\":\\\"1547803393171S2M\\\",\\\"utdid\\\":\\\"1547803393171S2M\\\",\\\"ver\\\":\\\"1.0.0.0\\\",\\\"userAgent\\\":\\\"Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.98 Mobile Safari/537.36\\\"}\",\"video_id\":\"%s\"}";

    private final static String NEW_DANMU_DATA = "{\"pid\":0,\"ctype\":10004,\"sver\":\"3.1.0\",\"cver\":\"v1.0\",\"ctime\":%s,\"guid\":\"EtW3F0kASSgCAXLz3WlAIEkl\",\"vid\":\"%s\",\"mat\":1,\"mcount\":1,\"type\":1}";
    private final static String NEW_DANMU_URL="https://acs.youku.com/h5/mopen.youku.danmu.list/1.0/?jsv=2.5.1&appKey=24679788&api=mopen.youku.danmu.list&v=1.0&type=originaljson&dataType=jsonp&timeout=20000&jsonpIncPrefix=utility";



    private PageRule rules = PageRule.build()
            .add(".*\\/v_show\\/.*$",page -> processMain(page));


	/***
     * 播放页处理逻辑
     * 1. 生成评论Job
     * 2. 生成弹幕Job
     * 3. 生成播放量Job
     * @param page
	 * @throws PageBeChangedException 
     */
    private void processMain(Page page) throws PageBeChangedException {

        Job oldJob = ((DelayRequest) page.getRequest()).getJob();
        Map<String, String> pageConfig = getPageConfig(page);
        if (pageConfig == null) {
            // 也许是404页面 ,大多数是 少儿/日漫
        	throw new PageBeChangedException("PageConfig is null");
        }

        String videoId = pageConfig.get("videoId");
        String code = pageConfig.get("videoId2");//修复一个bug--如果某集被下架了，但是链接上带有reload标志，会在链接不变的情况下请求同一专辑内其他集的内容videoId2 对不上。
        // 优酷code改了，前面没有z了，set进job的code从oldJob里取parentCode
        String showIdEn = pageConfig.get("showid_en");

        try {
            String hotCountInfo = pageConfig.get("hotCountInfo");
            if (hotCountInfo == null) {
                log.info("No detailInfo Script , code :" + oldJob.getCode());
            }
            JSONObject parse = JSONObject.parseObject(hotCountInfo);
            JSONObject dataJSONObject = parse.getJSONObject("data").getJSONObject("data");
            JSONArray array = dataJSONObject.getJSONArray("nodes").getJSONObject(0).getJSONArray("nodes");
            //showVideoStage = episode
            Integer showVideoStage = dataJSONObject.getJSONObject("data").getJSONObject("extra").getInteger("showVideoStage");
            //综艺episode 为 yyyyMMdd
            if (showVideoStage < 2 || showVideoStage > 19900101) {
                for (int i = 0; i < array.size(); i++) {
                    JSONObject jsonObject = array.getJSONObject(i);
                    String typeName = jsonObject.getString("typeName");
                    if (typeName.equals("播放页简介组件")) {
                        JSONObject nodes = jsonObject.getJSONArray("nodes").getJSONObject(0);
                        JSONObject data = nodes.getJSONObject("data");
                        String heat = data.getString("heat");
                        Long hotCount = Long.valueOf(heat.trim().replaceAll("\\D", ""));
                        ShowPopularLogs s = new ShowPopularLogs();
                        if (("电影").equals(pageConfig.get("catName"))) {
                            s.setCode(oldJob.getCode());
                        } else {
                            s.setCode(oldJob.getParentCode());
                        }
                        s.setPlatformId(3);
                        s.setHotCount(hotCount);
                        putModel(page, s);
                    }
                    // 剧集：Web播放页选集组件 其余：播放页选集组件
                    else if (typeName.contains("播放页选集组件")) {
                        JSONArray episodes = jsonObject.getJSONArray("nodes");
                        for (int j = 0; j < episodes.size(); j++) {
                            JSONObject ep = episodes.getJSONObject(j);
                            String epId = ep.getString("id");
                            String epCode = ep.getJSONObject("data").getJSONObject("action").getString("value");
                            String value = ep.getJSONObject("data").getString("videoType");
                            String catName = pageConfig.get("catName");
                            if (value == null && ("电影").equals(pageConfig.get("catName"))) {
                                //电影没有videoType key
                                createCommentAndBarrageJob(page, oldJob, epId, epCode);
                                continue;
                            }
                            if (!value.equals("正片")) continue;
                            //生成评论以及弹幕相关的任务
                            createCommentAndBarrageJob(page, oldJob, epId, epCode);
                        }
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
//        // 电影进入热度抓取，避免热度重复抓取
//        if ("电影".equals(pageConfig.get("catName"))) {
//            String vid = oldJob.getCode().replace("==", "");
//            try {
//                String d = URLEncoder.encode(String.format(data, vid), "UTF-8");
//                Job hotCountJob = new Job(HOT_COUNT_URL + "&data=" + d);
//                hotCountJob.setCode(oldJob.getCode());
//                putModel(page, hotCountJob);
//            } catch (UnsupportedEncodingException e) {
//                e.printStackTrace();
//            }
//        }

        if (StringUtils.isNotBlank(showIdEn) && !StringUtils.equals(showIdEn, "null")) {
            String mobileDetailUrl = String.format(PLAYCOUNT_MOBILE_DETAIL_URL, showIdEn);
            Job nj = DbEntityHelper.deriveNewJob(oldJob, mobileDetailUrl);
            nj.setCode(oldJob.getParentCode());
            putModel(page, nj);
        } else {
            throw new PageBeChangedException("The showid_en is null");
        }

        //自动发现少儿相关动漫
        if(oldJob.getCode().contains("_autoMark")){
            Html html = page.getHtml();
            String s = html.xpath("//code[@id=\"bpmodule-playpage-lefttitle-code\"]/div/div/h1/span[1]/a/@href").get();
            if(s == null || s.equals("")){
                return;
            }
            String codeString="";
            if(s.contains("=")) {
                codeString = s.substring(s.indexOf("id_") + 3, s.indexOf("=")+2);
            }else{
                codeString = s.substring(s.indexOf("id_") + 3,s.indexOf(".html"));
            }
            Show show = new Show();
            String title = delHTMLTag(html.xpath("//code[@id=\"bpmodule-playpage-lefttitle-code\"]/div/div/h1/span[1]").get());
            String url = fixUrl(s);
            show.setCategory(oldJob.getCode().substring(0,oldJob.getCode().indexOf("_autoMark")));
            show.setSource(3);
            show.setUrl(url);
            show.setParentId(0);
            show.setPlatformId(CommonEnum.Platform.YOU_KU.getCode());
            show.setName(title);
            show.setCode(codeString);
            putModel(page,show);
            return;
        }

    }

    private Map<String, String> getPageConfig(Page page) {
        List<String> scripts = page.getHtml().xpath("//script").all();
        Map<String, String> pageInfo = Maps.newHashMap();

        for (String sc:scripts) {
            if (sc.lastIndexOf("PageConfig") > 0 && sc.indexOf("videoId") > 0 && sc.indexOf("showid_en") > 0) {
                sc = StringEscapeUtils.unescapeHtml4(sc).replace("'", "\"").replace("\\\"", "\"");
                pageInfo.put("catId", RegexUtil.getDataByRegex(sc, "catId:\\s?\\\"(\\d+)\\\"", 1));//分类
                pageInfo.put("catName", RegexUtil.getDataByRegex(sc, "catName:\\s?\\\"(.*?)\\\"", 1));
                pageInfo.put("videoId", RegexUtil.getDataByRegex(sc, "videoId:\\s?\\\"(\\d+)\\\"", 1));
                pageInfo.put("videoId2", RegexUtil.getDataByRegex(sc, "videoId2:\\s?\\\"(.*?)\\\"", 1));
                pageInfo.put("showid", RegexUtil.getDataByRegex(sc, "showid:\\s?\\\"(.*?)\\\"", 1));
                pageInfo.put("seconds", RegexUtil.getDataByRegex(sc, "seconds:\\s?\\\"(.*?)\\\"", 1));
                pageInfo.put("showid_en", RegexUtil.getDataByRegex(sc, "showid_en:\\s?\\\"(.*?)\\\"", 1));
            }
            if(sc.contains("window.__USE_SSR__=true;")){
                try {
                    pageInfo.put("hotCountInfo", sc.substring(sc.indexOf("DATA__ =") + 8, sc.indexOf("};</script>") + 1));
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
        return pageInfo;
    }
    
    private void createCommentAndBarrageJob(Page page, Job oldJob, String videoId,String code) throws UnsupportedEncodingException {
        //评论量
        Job commentCountJob = DbEntityHelper.deriveNewJob(oldJob, String.format(COMMENT_COUNT_URL,videoId));
        commentCountJob.setCode(code);
        commentCountJob.setFrequency(FrequencyConstant.COMMENT_COUNT);
        putModel(page, commentCountJob);
        
//        //评论文本
//        Job commentContentJob = DbEntityHelper.deriveNewJob(oldJob, String.format(COMMENT_TEXT_URL,videoId));
//        commentContentJob.setCode(code);
//        commentContentJob.setFrequency(FrequencyConstant.COMMENT_TEXT);
//        putModel(page, commentContentJob);
        
//        //弹幕
//        Job barrageJob = DbEntityHelper.deriveNewJob(oldJob, String.format(DANMU_URL, videoId));
//        barrageJob.setCode(code);
//        barrageJob.setFrequency(FrequencyConstant.BARRAGE_TEXT);
//        putModel(page, barrageJob);

        //弹幕新
        String danmuDatas = URLEncoder.encode(String.format(NEW_DANMU_DATA, System.currentTimeMillis(),code), "UTF-8");
        Job newDanmuJob = new Job(NEW_DANMU_URL + "&data=" + danmuDatas);
        newDanmuJob.setCode(code);
        newDanmuJob.setFrequency(FrequencyConstant.BARRAGE_TEXT);
        putModel(page, newDanmuJob);

    }

    private String delHTMLTag(String htmlStr) {
        String regEx_script = "<script[^>]*?>[\\s\\S]*?<\\/script>"; //定义script的正则表达式
        String regEx_style = "<style[^>]*?>[\\s\\S]*?<\\/style>"; //定义style的正则表达式
        String regEx_html = "<[^>]+>"; //定义HTML标签的正则表达式

        Pattern p_script = Pattern.compile(regEx_script, Pattern.CASE_INSENSITIVE);
        Matcher m_script = p_script.matcher(htmlStr);
        htmlStr = m_script.replaceAll(""); //过滤script标签

        Pattern p_style = Pattern.compile(regEx_style, Pattern.CASE_INSENSITIVE);
        Matcher m_style = p_style.matcher(htmlStr);
        htmlStr = m_style.replaceAll(""); //过滤style标签

        Pattern p_html = Pattern.compile(regEx_html, Pattern.CASE_INSENSITIVE);
        Matcher m_html = p_html.matcher(htmlStr);
        htmlStr = m_html.replaceAll(""); //过滤html标签

        return htmlStr.trim(); //返回文本字符串
    }



    private String fixUrl(String href) {
        if (StringUtils.isNotBlank(href)&&!href.startsWith("http:")) {
            href = "http:"+href;
        }
        return href;
    }

    @Override
    public PageRule getPageRule() {
        return this.rules;
    }

    @Override
    public Site getSite() {
        return this.site;
    }
}
