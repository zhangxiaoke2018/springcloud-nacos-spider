package com.jinguduo.spider.service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.common.constant.FrequencyConstant;
import com.jinguduo.spider.common.constant.JobKind;
import com.jinguduo.spider.common.util.DateUtil;
import com.jinguduo.spider.data.table.Alias;
import com.jinguduo.spider.data.table.Classify;
import com.jinguduo.spider.data.table.WechatNewText;
import com.jinguduo.spider.db.repo.CoreKeywordRepo;
import com.jinguduo.spider.db.repo.WechatNewTextRepo;
import com.sun.xml.bind.v2.TODO;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by lc on 2017/6/15.
 */
@SuppressWarnings("all")
@Service
@Slf4j
public class CreateTaskByAliasService {

    @Autowired
    private AliasService aliasService;

    @Autowired
    private CoreKeywordRepo coreKeywordRepo;

    @Autowired
    private CoreKeywordService coreKeywordService;

    @Autowired
    private WechatNewTextRepo wechatNewTextRepo;

    //360指数
    private static final String INDEX_360_URL = "https://trends.so.com/index/csssprite?q=%s&area=全国&from=%s&to=%s&click=1&t=index";
    //360媒体指数
    private static final String MEDIA_360_URL = "https://trends.so.com/index/csssprite?q=%s&area=全国&from=%s&to=%s&click=1&t=media";
    //360用户画像任务
    private static final String CUSTOMER_360_URL = "https://trends.so.com/index/indexquerygraph?t=30&area=全国&q=%s";
    //今日头条新闻
    //private static final String NEWS_TOUTIAO_URL = "http://www.toutiao.com/search_content/?offset=0&format=json&keyword=%s&autoload=true&count=20&cur_tab=1";
    private static final String NEWS_TOUTIAO_URL = "https://www.toutiao.com/api/search/content/?aid=24&offset=0&format=json&keyword=%s&autoload=true&count=20&cur_tab=1&app_name=web_search";
    //微博指数
//    private static final String WEIBO_INDEX_URL = "http://data.weibo.com/index/ajax/hotword?flag=nolike&word=%s";
    private static final String WEIBO_INDEX_URL = "http://data.weibo.com/index/ajax/newindex/searchword?word=%s";
    //百度视频搜索
    private static final String BAIDU_VIDEO_URL = "http://v.baidu.com/v?word=%s&ie=utf-8";
    //360新闻
    private static final String NEWS_360_URL = "http://news.so.com/ns?j=0&rank=pdate&src=srp&tn=newstitle&scq=&q=%s&pn=1";
    //百度贴吧
    private static final String BAIDU_TIEBA_URL = "https://tieba.baidu.com/f?ie=utf-8&kw=%s";
    //百度新闻
    private static final String BAIDU_NEWS_URL = "http://news.baidu.com/ns?ct=1&rn=20&ie=utf-8&rsv_bp=1&sr=0&cl=2&f=8&prevct=no&tn=newstitle&word=%s";
    //搜狗微信搜索
//    private static final String SOUGOUWECHAT_SEARCH_URL = "http://weixin.sogou.com/weixin?type=2&ie=utf8&query=%s&tsn=1&ft=&et=&interation=&wxid=&usip=";
    private static final String SOUGOUWECHAT_SEARCH_URL = "https://weixin.sogou.com/weixin?type=2&query=%s&ie=utf8&s_from=input&_sug_=n&_sug_type_=1";
    //微博搜索
    private static final String WEIBO_SEARCH_URL = "https://s.weibo.com/weibo/%s?topnav=1&wvr=6&b=1";
    //BILIBILI
    //最多点击
    private static final String BILIBILI_SEARCH_CLICK_URL = "https://search.bilibili.com/video?keyword=%s&page=1&order=click";
    //最多弹幕
    private static final String BILIBILI_SEARCH_DM_URL = "https://search.bilibili.com/video?keyword=%s&page=1&order=dm";
    //最多收藏
    private static final String BILIBILI_SEARCH_STOW_URL = "https://search.bilibili.com/video?keyword=%s&page=1&order=stow";
    //视频数任务
    private static final String BILIBILI_SEARCH_VIDEO_COUNT_URL = "https://search.bilibili.com/video?keyword=%s&page=1&order=totalrank%s";

    //微博搜索手机端前缀
    private static final String WEIBO_SEARCH_MOBILE_URL_PREFIX = "https://m.weibo.cn/api/container/getIndex?containerid=100103";
    //微博搜索手机端后缀
    private static final String WEIBO_SEARCH_MOBILE_URL_SUFFIX = "type=%s&q=%s";

    /**
     * 创建任务的基础方法
     *
     * @param aliases 需要的生成任务的别名list
     * @param initUrl 初始url，别名应放在第一位
     * @param params  除了别名之外，需要添加的参数(以此排列在alias之后)
     */
    public List<Job> createTaskBase(List<Alias> aliases, String initUrl, JobKind jobKind, String... params) {
        //创建需要发送的任务list
        List<Job> jobs = Lists.newArrayList();
        //遍历别名信息
        for (Alias alias : aliases) {
            String url = null;
            try {
                List<String> strings = new ArrayList<>();
                strings.add(URLEncoder.encode(alias.getAlias().trim()));
                if (null != params && params.length != 0) {
                    for (String s : params) {
                        strings.add(s);
                    }
                }
                url = String.format(initUrl, strings.toArray());
            } catch (Exception e) {
                log.error("createTesk error,this task is -->{}", initUrl, e);
                continue;
            }
            if (StringUtils.isBlank(url)) {
                continue;
            }
            Job jobModel = new Job();
            jobModel.setCode(alias.getCode());
            jobModel.setUrl(url);
            jobModel.setKind(null == jobKind ? JobKind.Forever : jobKind);
            jobModel.setFrequency(FrequencyConstant.BLANK);
            jobs.add(jobModel);
        }
        return jobs;
    }


    //360指数任务
    public List<Job> createIndex360Task() {
        List<Alias> allByClassify = aliasService.getAliasByClassify(Classify.HAOSOU_INDEX.name());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        //昨天
        Date yesDate = DateUtil.getYesterdayDate();
        //昨天的String
        String yesDay = sdf.format(yesDate);
        //获取前三天第一天
        Date monOne = DateUtil.plusHours(yesDate, -72);
        String monStr = sdf.format(monOne);
        //过滤别名信息
        List<Alias> needList = allByClassify.stream()
                .filter(p -> (!StringUtils.equals(p.getCategory(), "NETWORK_MOVIE")))
                .collect(Collectors.toList());
        if (null == needList || needList.size() == 0) {
            return null;
        }
        //除了别名以外的参数
        List<Job> jobs = this.createTaskBase(needList, INDEX_360_URL, JobKind.Once, monStr, yesDay);
        return jobs;
    }

    //360用户画像任务
    //todo 暂时取消。等待改版
    public List<Job> createCustomer360Task() {
//        List<Alias> allByClassify = aliasService.getAliasByClassify(Classify.HAOSOU_INDEX.name());
//        List<Alias> needList = allByClassify.stream()
//                .filter(p -> p.getType() == 0)
//                .filter(p -> (!StringUtils.equals(p.getCategory(), "NETWORK_MOVIE")))
//                .collect(Collectors.toList());
//        if (null == needList || needList.size() == 0) {
//            return null;
//        }
//        List<Job> jobs = this.createTaskBase(needList, CUSTOMER_360_URL, JobKind.Once);
//        return jobs;
        return null;
    }

    //360媒体指数任务
    public List<Job> createMedia360Task() {
        List<Alias> allByClassify = aliasService.getAliasByClassify(Classify.HAOSOU_INDEX.name());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        //昨天
        Date yesDate = DateUtil.getYesterdayDate();
        //昨天的String
        String yesDay = sdf.format(yesDate);
        //获取前三天第一天
        Date monOne = DateUtil.plusHours(yesDate, -72);
        String monStr = sdf.format(monOne);
        //过滤别名信息
        List<Alias> needList = allByClassify.stream()
                .filter(p -> (!StringUtils.equals(p.getCategory(), "NETWORK_MOVIE")))
                .collect(Collectors.toList());
        if (null == needList || needList.size() == 0) {
            return null;
        }
        //除了别名以外的参数
        List<Job> jobs = this.createTaskBase(needList, MEDIA_360_URL, JobKind.Once, monStr, yesDay);
        return jobs;
    }

    //头条任务
    public List<Job> createToutiaoTask() {
        List<Alias> allByClassify = aliasService.getAliasByClassify(Classify.TOUTIAO.name());
        //过滤别名信息
        List<Alias> needList = allByClassify.stream()
                .filter(p -> (!StringUtils.equals(p.getCategory(), "NETWORK_MOVIE")))
                .collect(Collectors.toList());
        List<Job> jobs = this.createTaskBase(needList, NEWS_TOUTIAO_URL, JobKind.Forever, null);
        return jobs;
    }


    //微博指数任务
    public List<Job> createWeiboIndexTask() {
        List<Alias> allByClassify = aliasService.findWeiboIndexKeywords();
        List<Job> jobs = this.createTaskBase(allByClassify, WEIBO_INDEX_URL, JobKind.Forever, null);
        return jobs;
    }

    public List<Job> createBaiduVideoTask() {
        List<Alias> allByClassify = aliasService.getAliasByClassify(Classify.BAIDU_VIDEO.name());
        List<Job> jobs = this.createTaskBase(allByClassify, BAIDU_VIDEO_URL, JobKind.Forever, null);
        return jobs;
    }

    public List<Job> createNews360Task() {
        List<Alias> allByClassify = aliasService.getAliasByClassify(Classify.HAOSOU_NEWS.name());
        List<Job> jobs = this.createTaskBase(allByClassify, NEWS_360_URL, JobKind.Forever, null);
        return jobs;
    }

    public List<Job> createBaiduTiebaTask() {
        List<Alias> allByClassify = aliasService.getAliasByClassify(Classify.BAIDU_TIEBA.name());
        List<Job> jobs = this.createTaskBase(allByClassify, BAIDU_TIEBA_URL, JobKind.Forever, null);
        return jobs;
    }

    public List<Job> createBaiduNewsTask() {
        List<Alias> allByClassify = aliasService.getAliasByClassify(Classify.BAIDU_NEWS.name());
        List<Job> jobs = this.createTaskBase(allByClassify, BAIDU_NEWS_URL, JobKind.Forever, null);
        return jobs;
    }

    public List<Job> createSougouWechatSearchTask() {
        List<Alias> allByClassify = aliasService.getAliasByClassify(Classify.SOUGOUWECHAT_SEARCH.name()).stream().filter(a -> a.getCategory() != "NETWORK_MOVIE").collect(Collectors.toList());
        List<Job> jobs = this.createTaskBase(allByClassify, SOUGOUWECHAT_SEARCH_URL, JobKind.Forever, null);
        return jobs;
    }

    public List<Job> createWeiboSearchTask() {
        List<Alias> allByClassify = aliasService.getAliasByClassify(Classify.WEIBO_SEARCH.name()).stream().filter(a -> a.getCategory() != "NETWORK_MOVIE").collect(Collectors.toList());

        List<Job> jobs = this.createTaskBase(allByClassify, WEIBO_SEARCH_URL, JobKind.Forever, null);
        return jobs;
    }

    public List<Job> createBilibiliSearchTask() {
        List<Alias> allByClassify = aliasService.getAliasByClassify(Classify.BILIBILI_SEARCH.name());
        List<Alias> aliasList = allByClassify.stream().filter(a -> !"NETWORK_MOVIE".equals(a.getCategory()) && a.getType() != 1).collect(Collectors.toList());
        List<Job> clickJobs = this.createTaskBase(aliasList, BILIBILI_SEARCH_CLICK_URL, JobKind.Forever, null);
        List<Job> dmJobs = this.createTaskBase(aliasList, BILIBILI_SEARCH_DM_URL, JobKind.Forever, null);
        List<Job> stowJobs = this.createTaskBase(aliasList, BILIBILI_SEARCH_STOW_URL, JobKind.Forever, null);


        List<String> typeList = new ArrayList<>();
        typeList.add("");
        typeList.add("&tids_1=1");
        typeList.add("&tids_1=13");
        typeList.add("&tids_1=167");
        typeList.add("&tids_1=3");
        typeList.add("&tids_1=129");
        typeList.add("&tids_1=4");
        typeList.add("&tids_1=36");
        typeList.add("&tids_1=160");
        typeList.add("&tids_1=119");
        typeList.add("&tids_1=155");
        typeList.add("&tids_1=165");
        typeList.add("&tids_1=5");
        typeList.add("&tids_1=23");
        typeList.add("&tids_1=11");

        List jobs = Lists.newArrayList();
        jobs.addAll(clickJobs);
        jobs.addAll(dmJobs);
        jobs.addAll(stowJobs);

        for (String type : typeList) {
            List<Job> videoCountJobs = this.createTaskBase(aliasList, BILIBILI_SEARCH_VIDEO_COUNT_URL, JobKind.Forever, type);
            jobs.addAll(videoCountJobs);
        }

        return jobs;
    }

    public List<Job> createCoreTask() {

        Set<Pair<String, String>> keywords = coreKeywordService.findAliasAll();

        List<Job> jobs = Lists.newArrayList();
        for (Pair<String, String> keyword : keywords) {

            String sogouUrl = String.format(SOUGOUWECHAT_SEARCH_URL + "#core", URLEncoder.encode(keyword.getValue().trim()));
            Job wechatJob = new Job(sogouUrl);
            wechatJob.setFrequency(1800);
            wechatJob.setKind(JobKind.Forever);
            wechatJob.setCode(keyword.getKey());

            String weiboUrl = String.format(WEIBO_SEARCH_URL + "#core", URLEncoder.encode(keyword.getValue().trim()));
            Job weiboJob = new Job(weiboUrl);
            weiboJob.setFrequency(600);
            weiboJob.setKind(JobKind.Forever);
            weiboJob.setCode(keyword.getKey());

            String weiboMobileUrl = WEIBO_SEARCH_MOBILE_URL_PREFIX + URLEncoder.encode(String.format(WEIBO_SEARCH_MOBILE_URL_SUFFIX, "60", keyword.getValue().trim()));
            Job weiboMobileJob = new Job(weiboMobileUrl);
            weiboMobileJob.setFrequency(300);
            weiboMobileJob.setKind(JobKind.Forever);
            weiboMobileJob.setCode(keyword.getKey());

            String weiboMobileUrl2 = WEIBO_SEARCH_MOBILE_URL_PREFIX + URLEncoder.encode(String.format(WEIBO_SEARCH_MOBILE_URL_SUFFIX, "1", keyword.getValue().trim()));
            Job weiboMobileJob2 = new Job(weiboMobileUrl2);
            weiboMobileJob2.setFrequency(300);
            weiboMobileJob2.setKind(JobKind.Forever);
            weiboMobileJob2.setCode(keyword.getKey());

            jobs.add(wechatJob);
            jobs.add(weiboJob);
            jobs.add(weiboMobileJob);
            jobs.add(weiboMobileJob2);
        }
        //微博艺人关键词特殊处理
        //0->code,1->alias
        List<Object[]> weiboActorKeyWords = aliasService.findWeiboActorKeyWords();
        for (Object[] objs : weiboActorKeyWords) {
            String code = (String) objs[0];
            String alias = (String) objs[1];

            String weiboMobileUrl = WEIBO_SEARCH_MOBILE_URL_PREFIX + URLEncoder.encode(String.format(WEIBO_SEARCH_MOBILE_URL_SUFFIX, "60", alias.trim()));
            Job weiboMobileJob = new Job(weiboMobileUrl);
            weiboMobileJob.setFrequency(300);
            weiboMobileJob.setKind(JobKind.Forever);
            weiboMobileJob.setCode(code);

            String weiboMobileUrl2 = WEIBO_SEARCH_MOBILE_URL_PREFIX + URLEncoder.encode(String.format(WEIBO_SEARCH_MOBILE_URL_SUFFIX, "1", alias.trim()));
            Job weiboMobileJob2 = new Job(weiboMobileUrl2);
            weiboMobileJob2.setFrequency(300);
            weiboMobileJob2.setKind(JobKind.Forever);
            weiboMobileJob2.setCode(code);

            jobs.add(weiboMobileJob);
            jobs.add(weiboMobileJob2);
        }


        return jobs;
    }

    public List<Job> createNewBaiduNewsTask(){
        Map<String,String> map = Maps.newHashMap();
        map.put("bilibili","B站");
        map.put("yuewen","阅文");
        map.put("kuaikan","快看");
        map.put("tengxunshipin","腾讯视频");
        map.put("tengxundongman","腾讯动漫");
        map.put("kuaikanmanhua","快看漫画");
        map.put("youku","优酷");
        map.put("aiqiyi","爱奇艺");
        map.put("guangxianchuanmei","光线传媒");
        map.put("xuniouxiang","虚拟偶像");
        map.put("paopaomate","泡泡玛特");
        map.put("xuanjikeji","玄机科技");
        map.put("huimengdonghua","绘梦动画");
        map.put("zhangyuekeji","掌阅科技");
        map.put("jinjiangwenxue","晋江文学");
        map.put("fanfandongman","翻翻动漫");
        List<Job> jobs = Lists.newArrayList();
        for(String code : map.keySet()){
            String url  = String.format(BAIDU_NEWS_URL, map.get(code));
            Job job = new Job(url);
            job.setKind(JobKind.Forever);
            job.setCode(code);
            job.setFrequency(FrequencyConstant.BLANK);
            jobs.add(job);
        }
        return jobs;
    }


    public List<Job> createWechatArticleTask(){
        List<WechatNewText> texts = wechatNewTextRepo.findAllOrderByCrawledAt();
        log.info("start wechat article job,date is ->{}, time is ->{}",new Date().getTime(),System.currentTimeMillis());
        List<Job> jobs = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(texts)) {
            for (WechatNewText text : texts) {
                String url = text.getUrl();
                Job job = new Job(url);
                job.setKind(JobKind.Forever);
                job.setCode(text.getCode());
                job.setFrequency(FrequencyConstant.BLANK);
                jobs.add(job);
            }
            return jobs;
        }else {
            return null;
        }
    }
}
