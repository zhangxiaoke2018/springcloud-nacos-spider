package com.jinguduo.spider.spider.youku;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.jinguduo.spider.cluster.spider.listener.SpiderListener;
import com.jinguduo.spider.common.exception.AntiSpiderException;
import com.jinguduo.spider.common.util.Md5Util;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.Request;
import com.jinguduo.spider.webmagic.Task;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.Lists;
import sun.net.www.http.HttpClient;

import java.beans.Encoder;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Stream;

@Slf4j
public class YoukuAcsDomainDownLoaderListener implements SpiderListener {

    private Map<String, Map<String,String>> cookiePool = Maps.newConcurrentMap();

    private Map<String,String> newCookiePool = Maps.newConcurrentMap();

    private static final Object lock = new Object();

    @Override
    public void onStart(Task task) {
        Map<String, String> paramMap = Maps.newHashMap();
        // cookie
        paramMap.put("_m_h5_tk", "295649b43ef8fc4602127dd5926fa9f9_1598499084423");
        paramMap.put("_m_h5_tk_enc", "a8b25be61992d2c5a11b8b3b1c68bfd1");
        // param
        paramMap.put("t", null);
        paramMap.put("sign", null);
        cookiePool.put("XNDczNDc5MDcwOA==", paramMap);

        newCookiePool.put("_m_h5_tk","1d4abca80470a227f6eef991d6215fa7_1598526537486");
        newCookiePool.put("_m_h5_tk_enc","b60b7cfe821116dab4bafd2d256b88c2");
    }

    /**
     * 爬虫正式使用url请求数据时，先进入该方法，该方法对url进行重新构造
     * @param request
     * @param task
     */
    @Override
    public void onRequest(Request request, Task task) {

        try {
            String url = request.getUrl();
            if(url.contains("mtop.youku.haixing.play.h5.detail")){
                youkuHotCountOnRequest(url,request);
            }else if(url.contains("mopen.youku.danmu.list")){
                youkuDanmuOnRequest(url,request);
            }
        } catch (UnsupportedEncodingException e) {
            throw new AntiSpiderException(e.getMessage());
        }

    }

    public void youkuDanmuOnRequest(String url,Request request) throws UnsupportedEncodingException{
        Map<String,String> params = paramMap(url);
        String data = URLDecoder.decode(params.get("data"),"UTF-8");
        JSONObject dataJson = JSONObject.parseObject(data);
        String videoId = dataJson.getString("vid");
        String appKey = params.get("appKey");
        String token = newCookiePool.get("_m_h5_tk");
        token = token.substring(0, token.indexOf("_"));
        JSONObject jsonObject = JSONObject.parseObject(data);
        Set<String> strings = jsonObject.keySet();
        List<String> strings1 = Lists.newArrayList();
        strings.forEach(r->{ strings1.add(r); });
        //请求参数排序
        Collections.sort(strings1);
        Map map = Maps.newLinkedHashMap();
        for(String key:strings1){
            map.put(key,jsonObject.get(key));
        }
        JSONObject dataJsonMap = new JSONObject(map);
        String msg = Base64.getEncoder().encodeToString(dataJsonMap.toString().getBytes("utf-8"));
        //第一层签名
        String sign = Md5Util.getMd5(msg + "MkmC9SoIw6xCkSKHhJ7b5D2r51kBiREr");
        String newDatas = data.substring(0,data.length()-1)+",\"msg\":\""+msg+"\""+",\"sign\":\""+sign+"\"}";
        Long currTime = System.currentTimeMillis();
        String signFinal = Md5Util.getMd5(token+"&"+currTime+"&"+appKey+"&"+newDatas);
        String t = String.valueOf(currTime);


        url = url.substring(0,url.indexOf("&data=")+6)+URLEncoder.encode(newDatas)+"&t=" + t + "&sign=" + signFinal;
        request.setUrl(url);
        request.addCookie("_m_h5_tk", newCookiePool.get("_m_h5_tk"));
        request.addCookie("_m_h5_tk_enc", newCookiePool.get("_m_h5_tk_enc"));


    }

    public void youkuHotCountOnRequest(String url,Request request) throws UnsupportedEncodingException {
        Map<String, String> params = paramMap(url);
        String data = URLDecoder.decode(params.get("data"), "UTF-8");
        JSONObject dataJson = JSONObject.parseObject(data);
        String videoId = dataJson.getString("video_id");
        String appKey = params.get("appKey");
        Map<String, String> paramMap = cookiePool.get(videoId);
        if (paramMap != null) {
            // token为cookie中_m_h5_tk的前半段，提取出来
            String token = paramMap.get("_m_h5_tk");
            token = token.substring(0, token.indexOf("_"));
            String t = String.valueOf(System.currentTimeMillis());
            // 签名
            String sign = Md5Util.getMd5(token + "&" + t + "&" + appKey + "&" + data);
            if (StringUtils.isNotBlank(paramMap.get("t"))) {
                t = paramMap.get("t");
            }
            if (StringUtils.isNotBlank(paramMap.get("sign"))) {
                sign = paramMap.get("sign");
            }
            // 重置cookie pool
            String finalT = t;
            String finalSign = sign;
            cookiePool.put(videoId, new HashMap<String, String>() {{
                put("t", finalT);
                put("sign", finalSign);
                put("_m_h5_tk", paramMap.get("_m_h5_tk"));
                put("_m_h5_tk_enc", paramMap.get("_m_h5_tk_enc"));
            }});
            url = url + "&t=" + t + "&sign=" + sign;
            request.setUrl(url);
            request.addCookie("_m_h5_tk", paramMap.get("_m_h5_tk"));
            request.addCookie("_m_h5_tk_enc", paramMap.get("_m_h5_tk_enc"));
        }
    }

    /**
     * 爬虫下载完页面后进入此方法
     * @param request
     * @param page
     * @param task
     */
    @Override
    public void onResponse(Request request, Page page, Task task) {

        if(request.getUrl().contains("mtop.youku.haixing.play.h5.detail")){
            youkuHotCountOnResponse(request, page,  task);
        }else if(request.getUrl().contains("mopen.youku.danmu.list")){
            try {
                youkuDanmuOnResponse(request, page,  task);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void youkuDanmuOnResponse(Request request, Page page, Task task) throws UnsupportedEncodingException {
        Map<String, List<String>> headers = page.getHeaders();
        if(headers == null){
            log.error(request.getUrl() + " " + page);
        }
        // 如果返回结果的Header中包含Set-Cookie，意味着Cookie失效了，优酷的服务端重新返回了Cookie，保存下来
        List<String> cookies = headers.get("Set-Cookie");
        if (cookies != null && cookies.size() > 0) {
            //log.info("youku barrage cookie is expired ! Ready to get new Cookie! new Cookies : " + cookies.toString());
            Map<String, String> params = paramMap(request.getUrl());
            String data = URLDecoder.decode(params.get("data"), "UTF-8");
            JSONObject dataJson = JSONObject.parseObject(data);
            String videoId = dataJson.getString("vid");
            Map<String,String> cookieMap = Maps.newConcurrentMap();
            for (String c : cookies) {
                c = c.substring(0, c.indexOf(";") + 1);
                cookieMap.put(c.substring(0, c.indexOf("=")), c.substring(c.indexOf("=") + 1, c.lastIndexOf(";")));
            }
            synchronized (lock) {
                newCookiePool.put("_m_h5_tk", cookieMap.get("_m_h5_tk"));
                newCookiePool.put("_m_h5_tk_enc", cookieMap.get("_m_h5_tk_enc"));
                log.info("Sync codes newCookiePool:"+newCookiePool.toString());
            }
            //log.info("Youku new barrage "+videoId + " got Cookies : " + cookieMap.toString());
            throw new AntiSpiderException("youku_barrage crawl retry " + page.getRawText());
        }
    }

    public void youkuHotCountOnResponse(Request request, Page page, Task task){
        try {
            Map<String, List<String>> headers = page.getHeaders();
            if(headers == null){
                log.error(request.getUrl() + " " + page);
            }
            // 如果返回结果的Header中包含Set-Cookie，意味着Cookie失效了，优酷的服务端重新返回了Cookie，保存下来
            List<String> cookies = headers.get("Set-Cookie");
            if (cookies != null && cookies.size() > 0) {
                Map<String, String> params = paramMap(request.getUrl());
                String data = URLDecoder.decode(params.get("data"), "UTF-8");
                String t = params.get("t");
                String sign = params.get("sign");
                JSONObject dataJson = JSONObject.parseObject(data);
                String videoId = dataJson.getString("vid");
                Map<String,String> cookieMap = Maps.newHashMap();
                for (String c : cookies) {
                    c = c.substring(0, c.indexOf(";") + 1);
                    cookieMap.put(c.substring(0, c.indexOf("=")), c.substring(c.indexOf("=") + 1, c.lastIndexOf(";")));
                }
                // 重置cookie pool
//                String finalT = t;
//                String finalSign = sign;
                cookiePool.put(videoId, new HashMap<String, String>(){{
//                    put("t", finalT);
//                    put("sign", finalSign);
                    put("_m_h5_tk", cookieMap.get("_m_h5_tk"));
                    put("_m_h5_tk_enc", cookieMap.get("_m_h5_tk_enc"));
                }});

                log.info(videoId + "------------" + cookieMap.toString());
                throw new AntiSpiderException("youku_hot_count crawl retry " + page.getRawText());
            }
        } catch (UnsupportedEncodingException e) {
            throw new AntiSpiderException(e.getMessage());
        }
    }

    @Override
    public void onError(Request request, Exception e, Task task) {

    }

    @Override
    public void onExit(Task task) {

    }

    public static Map<String, String> paramMap(String url) {
        Map<String, String> map = null;
        if (url != null && url.indexOf("&") > -1 && url.indexOf("=") > -1) {
            map = new HashMap<String, String>();
            String[] arrTemp = url.split("&");
            for (String str : arrTemp) {
                if (StringUtils.isBlank(str)) continue;
                String[] qs = str.split("=");
                map.put(qs[0], qs[1]);
            }
        }
        return map;
    }

    public static void main (String[] args) throws UnsupportedEncodingException {
        Long time = System.currentTimeMillis();
        System.out.println("初始时间:" + time);
        String appkey = "24679788";
        String token = "295649b43ef8fc4602127dd5926fa9f9";
        String vid = "XNDczNDc5MDcwOA==";
        String data1 = "{\"pid\":0,\"ctype\":10004,\"sver\":\"3.1.0\",\"cver\":\"v1.0\",\"ctime\":" + time + ",\"guid\":\"EtW3F0kASSgCAXLz3WlAIEkl\",\"vid\":\"" + vid + "\",\"mat\":1,\"mcount\":1,\"type\":1}";
        System.out.println(data1);
        JSONObject jsonObject = JSONObject.parseObject(data1);
        System.out.println(jsonObject.toString());
        Set<String> strings = jsonObject.keySet();
        List<String> strings1 = Lists.newArrayList();
        strings.forEach(r -> {
            strings1.add(r);
        });
        Collections.sort(strings1);
        Map map = Maps.newLinkedHashMap();
        for (String key : strings1) {
            map.put(key, jsonObject.get(key));
        }
        JSONObject dataJson = new JSONObject(map);
        System.out.println(dataJson.toString());
        String msg = Base64.getEncoder().encodeToString(dataJson.toString().getBytes("utf-8"));
        String sign = Md5Util.getMd5(msg + "MkmC9SoIw6xCkSKHhJ7b5D2r51kBiREr");
        System.out.println("msg:" + msg);
        System.out.println("第一次签名:" + sign);
        String dataString1 = data1.substring(0, data1.length() - 1) + ",\"msg\":\"" + msg + "\"" + ",\"sign\":\"" + sign + "\"}";
        Long currTime = System.currentTimeMillis();
        System.out.println("第二次时间:" + currTime);
        String sign2 = Md5Util.getMd5(token + "&" + currTime + "&" + appkey + "&" + dataString1);
        System.out.println("最终签名:" + sign2);
        //String url = "https://acs.youku.com/h5/mopen.youku.danmu.list/1.0/?jsv=2.5.1&appKey=" + appkey + "&t=" + currTime + "&sign=" + sign2 + "&api=mopen.youku.danmu.list&v=1.0&type=originaljson&dataType=jsonp&timeout=20000&jsonpIncPrefix=utility&data=";
        String url = "https://acs.youku.com/h5/mtop.youku.ycp.comment.mainpage.get/1.0/?jsv=2.5.1&appKey="+appkey+"&t="+currTime+"&sign="+sign2+"&api=mtop.youku.ycp.comment.mainpage.get&type=jsonp&v=1.0&dataType=jsonp&jsonpIncPrefix=playpage_comment&callback=mtopjsonpplaypage_comment4&data=";
        Map headerMap = Maps.newHashMap();
        headerMap.put("Cookie", "_m_h5_tk=295649b43ef8fc4602127dd5926fa9f9_1598499084423; _m_h5_tk_enc=a8b25be61992d2c5a11b8b3b1c68bfd1;");
        headerMap.put("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/84.0.4147.125 Safari/537.36");
        System.out.println(url + URLEncoder.encode(dataString1));
        String s = HttpClientUtil.sendPostRequest(url + URLEncoder.encode(dataString1), dataString1, headerMap);
        System.out.println(s);
    }

}