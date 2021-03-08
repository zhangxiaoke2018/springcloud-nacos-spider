package com.jinguduo.spider.spider.so360;

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
import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.data.table.Customer360Logs;
import com.jinguduo.spider.data.table.Index360Logs;
import com.jinguduo.spider.data.table.Media360Logs;
import com.jinguduo.spider.webmagic.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.jinguduo.spider.spider.so360.ImageUtils.GenerateImage;

/**
 * 定向抓取360指数
 * Created by lc on 2017/5/4.
 * cookie有问题，每天刷新
 */
@SuppressWarnings("all")
@Worker
@Slf4j
public class Index360Spider extends CrawlSpider {
    private static final String INDEX_360_URL = "http://trends.so.com/index/csssprite?q=%s&area=全国&from=%s&to=%s&click=%s&t=index";
    private static final String MEDIA_360_URL = "http://trends.so.com/index/csssprite?q=%s&area=全国&from=%s&to=%s&click=%s&t=media";
    private static final String CUSTOMER_360_URL = "http://trends.so.com/index/indexquerygraph?t=30&area=%s&q=%s";

    /**
     * 登录失效则更换此cookie
     */
    //private static final String LOGIN_COOKIE = "Q=; T=;";
    private static final String Q_COOKIE = "u%3Dwvathqhb704%26n%3D%26le%3D%26m%3DZGZkWGWOWGWOWGWOWGWOWGWOAmRk%26qid%3D2994190117%26im%3D1_t01923d359dad425928%26src%3Dpcw_360index%26t%3D1";
    private static final String T_COOKIE = "s%3Db268e6797c11b29e33c94f6b5f3c3099%26t%3D1605234190%26lm%3D%26lf%3D4%26sk%3D5e84d953f3334f6d6c30924d680873d7%26mt%3D1605234190%26rc%3D%26v%3D2.0%26a%3D1";
    /**
     * 0-9图片信息
     */
    private static Map<String, Integer> numMap = new HashMap<>();
    /**
     * 省份信息，用做360用户画像任务生成
     */
    private static List<String> provinceList = Lists.newArrayList();

    /**
     * 把0-9的md5图片加载到内存
     * */
    static {
        numMap.put("80ec4e6a0364972e827c1f260b565bda", 0);
        numMap.put("0d8238b1ef81ae8bf9037021aa2e6db3", 1);
        numMap.put("98d7916a92bd0103edc1248992361357", 2);
        numMap.put("06f4b113d8e426d0bc351ee776800aac", 3);
        numMap.put("52a0f1ff83be519e584168207b892f41", 4);
        numMap.put("0111a734c45d918de7494ad501cb118d", 5);
        numMap.put("1214672e8f771ce0a6a0d7ec55a602e3", 6);
        numMap.put("65c8b0891f8cd41d3f0dc7a665ae3fa8", 7);
        numMap.put("3fc084669f53691fbdb900f527255e84", 8);
        numMap.put("763f46e4d13f63cb3daac339b963732d", 9);

        //北京、上海、广东、天津、重庆、江苏、浙江、四川、湖南、山东
        provinceList.add("北京");
        provinceList.add("上海");
        provinceList.add("广东");
        provinceList.add("天津");
        provinceList.add("重庆");
        provinceList.add("江苏");
        provinceList.add("浙江");
        provinceList.add("四川");
        provinceList.add("湖南");
        provinceList.add("山东");
    }

    //添加cookie
    private Site site = SiteBuilder.builder().setDomain("trends.so.com")
            .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.102 Safari/537.36 QIHU 360EE")
            .addHeader("Host", "trends.so.com")
            .addHeader("Connection", "keep-alive")
            .addHeader("Cache-Control", "max-age=0")
            .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
            .addHeader("Upgrade-Insecure-Requests", "1")
            .addHeader("Accept-Encoding", "gzip, deflate, sdch")
            .addHeader("Accept-Language", "zh-CN,zh;q=0.8,en;q=0.6")
            //     .addHeader("Cookie", LOGIN_COOKIE)
            .addCookie("Q", Q_COOKIE)
            .addCookie("T", T_COOKIE)
            .build();

    private PageRule rules = PageRule.build()
            .add("t=index", page -> disposeIndex360Data(page))
            .add("t=media", page -> disposeMedia360Data(page))
            .add("/indexquerygraph", page -> disposeCustomer360Data(page));

    /**
     * 360用户画像
     */
    //和其他两个不一样，单独处理，返回json格式，目测360早晚统一
    private void disposeCustomer360Data(Page page) {
        Job job = ((DelayRequest) page.getRequest()).getJob();
        String url = job.getUrl();
        //获取json,将json转换为map
        JSONObject map = null;
        try {
            map = JSONObject.parseObject(page.getJson().get());
        } catch (Exception e) {
            log.error("trends.so.com --> 360服务器异常：抱歉，服务器忙不过来了... this data need json,but error ,this url is -->{}", job.getUrl());
            return;
        }
        //判断status
        Integer status = map.getInteger("status");
        switch (status) {
            case 0:
                break;
            case 7001:
                //未收录
                return;
            case 7011:
                //登录信息失效
                log.error("trends.so.com , dispose 360 data error ,登录信息失效！，this result is -->{},url-->{}", page.getJson().get(), url);
                return;
            case 7005:
                //服务异常。
                log.error("trends.so.com , dispose 360 data error ,服务异常！，this result is -->{},url-->{}", page.getJson().get(), url);
                return;
            default:
                //不知道什么情况
                log.warn("trends.so.com ,dispose 360 data error ,this data json is -->{},url -->{}", page.getJson().get(), url);
                return;
        }

        //需要的数据data
        JSONObject data = map.getJSONObject("data");
        if (null == data) {
            log.error("trends.so.com ,dispose 360 data error ,this data json is -->{},url -->{}", page.getJson().get(), url);
            return;
        }
        //创建pojo
        Customer360Logs logs = new Customer360Logs();
        //性别信息
        JSONArray sexs = data.getJSONArray("sex");
        for (Object initSex : sexs) {
            JSONObject sex = (JSONObject) initSex;
            //01男；02女
            Integer entity = sex.getInteger("entity");
            //比例格式例如10%
            String percent = sex.getString("percent");
            //去除%，保存byte类型数据
            Byte sexRatio = this.getRatioNum(percent);
            switch (entity) {
                case 1:
                    logs.setMaleRatio(sexRatio);
                    break;
                case 2:
                    logs.setFemaleRatio(sexRatio);
                    break;
                default:
                    log.error("trends.so.com , dispose 360 data error ,性别信息出错,this data json is -->{},url -->{}", page.getJson().get(), url);
                    return;
            }
        }
        //年龄信息
        JSONArray ageDatas = data.getJSONArray("age");
        for (Object initAge : ageDatas) {
            JSONObject age = (JSONObject) initAge;
            /**
             * 01 18以下
             * 02 19-24
             * 03 25-34
             * 04 34-49
             * 05 50以上
             * */
            Integer entity = age.getInteger("entity");
            //比例格式例如10%
            Byte percent = this.getRatioNum(age.getString("percent"));
            JSONObject ageSex = age.getJSONObject("sex");
            Byte male = this.getRatioNum(ageSex.getString("male"));
            Byte female = this.getRatioNum(ageSex.getString("female"));
            switch (entity) {
                case 1:
                    logs.setAge18ratio(percent);
                    logs.setAge18male(male);
                    logs.setAge18female(female);
                    break;
                case 2:
                    logs.setAge24ratio(percent);
                    logs.setAge24male(male);
                    logs.setAge24female(female);
                    break;
                case 3:
                    logs.setAge34ratio(percent);
                    logs.setAge34male(male);
                    logs.setAge34female(female);
                    break;
                case 4:
                    logs.setAge49ratio(percent);
                    logs.setAge49male(male);
                    logs.setAge49female(female);
                    break;
                case 5:
                    logs.setAge50ratio(percent);
                    logs.setAge50male(male);
                    logs.setAge50female(female);
                    break;
                default:
                    log.error("trends.so.com , dispose 360 data error ,年龄信息出错,this data json is -->{},url -->{}", page.getJson().get(), url);
                    break;
            }
        }
        logs.setCode(job.getCode());
        //省份
        String province = StringUtils.substringBefore(
                StringUtils.substringAfterLast(url, "&area=")
                , "&q=");
        logs.setProvince(URLDecoder.decode(province));
        logs.setDay(new Date());
        putModel(page, logs);


        if (StringUtils.contains(url, "&area=全国")) {
            //生成其他10个省的信息
            for (String pro : provinceList) {
                String newUrl = url.replaceFirst("全国", pro);
                Job job2 = new Job(newUrl);
                DbEntityHelper.derive(job, job2);
                putModel(page, job2);
            }
        }

    }

    private void disposeIndex360Data(Page page) {
        log.debug("###begin dispose Index360 data###，this url is ->{}", page.getUrl());

        Map<String, Object> DataMap = this.disposeData(page, INDEX_360_URL);
        if (null == DataMap || DataMap.size() == 0) {
            return;
        }
        //创建model
        Index360Logs logs = new Index360Logs();
        logs.setIndexCount((Integer) DataMap.get("indexCount"));
        logs.setCode(String.valueOf(DataMap.get("code")));
        logs.setIndexDay((Date) DataMap.get("indexDay"));
        putModel(page, logs);
        log.debug("###finish index360Spider###");
    }

    private void disposeMedia360Data(Page page) {
        log.debug("###begin dispose media360 data###，this url is ->{}", page.getUrl());

        Map<String, Object> DataMap = this.disposeData(page, MEDIA_360_URL);
        if (null == DataMap || DataMap.size() == 0) {
            return;

        }
        //创建model
        Media360Logs logs = new Media360Logs();
        logs.setCode(String.valueOf(DataMap.get("code")));
        logs.setMediaCount((Integer) DataMap.get("indexCount"));
        logs.setMediaDay((Date) DataMap.get("indexDay"));
        putModel(page, logs);
        log.debug("###finish media360Spider###");
    }

    //上边俩东西方法一样。。抽出来拉倒。
    private Map<String, Object> disposeData(Page page, String replaceUrl) {

        Job job = ((DelayRequest) page.getRequest()).getJob();
        if (job == null) {
            log.debug("job is null ,this job is -->{}", job.getId());
            return null;
        }
        //获取json,将json转换为map
        JSONObject map = null;
        //5xx、4xx状态时，返回不是json
        //如：抱歉，服务器忙不过来了...、稍后重试或...
        try {
            map = JSONObject.parseObject(page.getJson().get());
        } catch (Exception e) {
            return null;
        }
        Integer status = map.getInteger("status");
        switch (status) {
            case 0:
                break;
            case 7001:
                //未收录
                return null;
            case 7011:
                //登录信息失效
                log.error("trends.so.com , dispose 360 data error ,登录信息失效！，this result is -->{},url-->{}", page.getJson().get(), job.getUrl());
                return null;
            case 7005:
                //服务异常。
                log.error("trends.so.com , dispose 360 data error ,服务异常！，this result is -->{},url-->{}", page.getJson().get(), job.getUrl());
                return null;
            default:
                //不知道什么情况
                log.error("trends.so.com ,dispose 360 data error ,this data json is -->{},url -->{}", page.getJson().get(), job.getUrl());
                return null;
        }
        //获取需要的数据
        JSONObject data = null;
        try {
            data = map.getJSONObject("data");
        } catch (Exception e) {
            return null;
        }
        if (null == data || data.size() == 0) {
            return null;
        }
        String movieName = null;
        for (String s : data.keySet()) {
            movieName = s;
        }
        //获取图片信息
        String css = null;
        String img = null;
        JSONObject movie = data.getJSONObject(movieName);
        css = movie.getString("css");
        img = movie.getString("img");
        img = StringUtils.replace(img, "data:image/png;base64,", "");

        //css的偏移数据存储
        List<HaoSouNumberModel> cssList = new ArrayList();
        String[] split = css.split("</span>");
        for (int i = 0; i < split.length; i++) {
            String s = split[i];
            int i2 = s.indexOf("px;background-position:-");
            int i3 = s.indexOf(".000000px 6px'>");
            HaoSouNumberModel mo = new HaoSouNumberModel();
            mo.setWidth(6);
            mo.setHight(6);
            mo.setPosition(Integer.valueOf(s.substring(i2 + 24, i3)));
            cssList.add(mo);
        }
        //分析图片获得最后的指数countStr
        InputStream imageStreams = GenerateImage(img);

        String countStr = ImageUtils.getCountStr(imageStreams, cssList, numMap);
        if (StringUtils.isBlank(countStr)) {
            log.debug("图片无法截取到对应数字，请尽快调整map中内容。url-->{}", job.getUrl());
        }

        //抓取时间相关
        //开始的时间
        String fromStr = StringUtils.substringBefore(
                StringUtils.substringAfterLast(job.getUrl(), "from=")
                , "&to=");
        //结束的时间
        String toStr = StringUtils.substringBefore(
                StringUtils.substringAfterLast(job.getUrl(), "&to=")
                , "&click=");
        //点击的次序
        String clickStr = StringUtils.substringBefore(
                StringUtils.substringAfterLast(job.getUrl(), "click=")
                , "&t=");
        Integer click = Integer.valueOf(clickStr);

        //from的时间往后推迟click的天数
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        Date from = new Date(0);
        Date to = new Date(0);
        Date indexDay = new Date(0);
        try {
            from = sdf.parse(fromStr);
            to = sdf.parse(toStr);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(from);
            calendar.add(calendar.DAY_OF_MONTH, click - 1);
            indexDay = calendar.getTime();
        } catch (ParseException e) {
            log.error(e.getMessage(), e);
        }
        Map<String, Object> dataMap = new HashedMap();

        try {
            dataMap.put("indexCount", Integer.valueOf(countStr));
            dataMap.put("indexDay", indexDay);
            dataMap.put("code", job.getCode());
        } catch (NumberFormatException e) {
            log.error("数据处理完毕，存放错误，数据为  code-->{},indexcount-->{},indexday-->{}", job.getCode(), countStr, indexDay);
        }

        //迭代下一个点击
        Integer allClick = Integer.valueOf(String.valueOf((to.getTime() - from.getTime()) / (1000 * 3600 * 24)));
        if (click <= allClick) {
            String url = String.format(replaceUrl, URLEncoder.encode(movieName), fromStr, toStr, click + 1);
            Job job2 = new Job(url);
            DbEntityHelper.derive(job, job2);
            putModel(page, job2);
        }
        return dataMap;
    }


    //去掉%号，转为byte类型，无的话返回0
    private Byte getRatioNum(String percent) {
        if (StringUtils.isBlank(percent)) {
            return 0;
        }
        Byte num = null;
        try {
            String replace = StringUtils.replace(percent, "%", "");
            num = Byte.valueOf(replace);
        } catch (NumberFormatException e) {
            return 0;
        }
        return num;
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
