//package com.jinguduo.spider.spider.manmanmanhua;
//
//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.JSONObject;
//import com.jinguduo.spider.cluster.spider.listener.SpiderListener;
//import com.jinguduo.spider.common.util.tengxun.Base64;
//import com.jinguduo.spider.webmagic.Page;
//import com.jinguduo.spider.webmagic.Request;
//import com.jinguduo.spider.webmagic.Task;
//import com.jinguduo.spider.webmagic.model.HttpRequestBody;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.lang3.RandomUtils;
//import org.apache.commons.lang3.StringUtils;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.client.RestTemplate;
//
//import java.io.UnsupportedEncodingException;
//import java.net.URLDecoder;
//import java.util.*;
//import java.util.concurrent.TimeUnit;
//
///**
// * I'm a cute code dog
// * User: LingChen
// * Date:2017/11/8
// * Time:10:19
// */
//@Slf4j
//@SuppressWarnings("all")
//public class ComicMmmhDownLoaderListenerBak implements SpiderListener {
//
//    @Autowired(required = false)
//    private RestTemplate restTemplate;
//
//    public static List<String> tokens = new ArrayList<>();
//
//    private final static long PERIOD = TimeUnit.MINUTES.toMillis(30);
//
//    private Timer timer = null;
//
//
//    @Override
//    public void onStart(Task task) {
//        if (timer == null) {
//            timer = new Timer("mmmhTokenLoader");
//            timer.scheduleAtFixedRate(new mmmhGetTokenTask(), 0, PERIOD);
//        }
//    }
//
//    @Override
//    public void onRequest(Request request, Task task) {
//        String url = request.getUrl();
//        if (!StringUtils.contains(url, "?")) {
//            return;
//        }
//        String paramer = StringUtils.substring(url, url.indexOf("?") + 1, url.length());
//        try {
//            paramer = URLDecoder.decode(paramer, "utf-8");
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
//        String newUrl = StringUtils.substring(url, 0, url.indexOf("?"));
//        byte[] bodyBytes = paramerToBytes(paramer);
//        HttpRequestBody body = new HttpRequestBody(bodyBytes, "raw", "utf-8");
//        request.setMethod("POST");
//        request.setRequestBody(body);
//    }
//
//    @Override
//    public void onResponse(Request request, Page page, Task task) {
//    }
//
//    @Override
//    public void onError(Request request, Exception e, Task task) {
//    }
//
//    @Override
//    public void onExit(Task task) {
//    }
//
//
//    /**
//     * 定时获取token
//     */
//    class mmmhGetTokenTask extends TimerTask {
//        @Override
//        public void run() {
//            queryToken();
//        }
//    }
//
//    private byte[] paramerToBytes(String paramer) {
//        if (null == tokens || tokens.isEmpty()) {
//            queryToken();
//        }
//        //根据paramer拼接body
//        Map<String, Object> map = JSONObject.parseObject(paramer, Map.class);
//        if (map.containsKey("token")) {
//            String token = tokens.get(RandomUtils.nextInt(0, tokens.size()));
//            map.put("token", token);
//           // map.put("token","manmanDefaultToken");
//        }
//        return mapToBytes(map);
//    }
//
//
//    public void queryToken() {
//        Map map = new HashMap();
//        map.put("token", "manmanDefaultToken");
//        map.put("api", "start/index");
//        map.put("info", "5.0.9_OPPO R11_android_android_4.4.2_866174010386528_yingyongbao");
//        byte[] bytes = mapToBytes(map);
//
//        restTemplate = restTemplate == null ? new RestTemplate() : restTemplate;
//        String s = restTemplate.postForObject(ComicMmmhSpider.API_URL, bytes, String.class);
//        JSONObject jsonObject = JSONObject.parseObject(s);
//        if (200 != jsonObject.getInteger("code").intValue()) {
//            tokens.add("manmanDefaultToken");
//            return;
//        }
//        String token = jsonObject.getJSONObject("data").getString("token");
//        tokens.add(token);
//        if (tokens.size() > 10) {
//            tokens.remove(0);
//        }
//    }
//
//    public static byte[] mapToBytes(Map<String, Object> data) {
//        String map = JSON.toJSONString(data);
//
//        long time = (System.currentTimeMillis() / 1000);
//        long j = 1000000000;
//        if (time >= 1000000000) {
//            j = time;
//        }
//
//        StringBuilder stringBuilder = new StringBuilder(map);
//        stringBuilder.append(j);
//        byte[] b = stringBuilder.toString().getBytes();
//        char[] cArr = new char[b.length];
//        for (int i = 0; i < b.length; i++) {
//            cArr[i] = (char) (b[i] & 255);
//        }
//        int[] encr = new int[cArr.length];
//        int i2 = 0;
//        int i3 = 1;
//        for (int i4 = 0; i4 < cArr.length; i4++) {
//            int i5 = i4 % 5;
//            if (i5 == 0) {
//                i3 = i2 % 2 == 0 ? 1 : -1;
//                i2++;
//            }
//            if (cArr[i4] >= 128) {
//                encr[i4] = cArr[i4] - 125;
//            } else {
//                encr[i4] = (cArr[i4] + 110) + ((i5 + 1) * i3);
//            }
//        }
//        return convertToBase64(encr);
//    }
//
//    public static byte[] convertToBase64(int[] iArr) {
//        byte[] bArr = new byte[iArr.length];
//        for (int i = 0; i < iArr.length; i++) {
//            bArr[i] = (byte) iArr[i];
//        }
//        return Base64.encode(bArr, 2);
//    }
//}
