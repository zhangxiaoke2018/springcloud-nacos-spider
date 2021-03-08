package com.jinguduo.spider.spider.kanmanhua;

import com.alibaba.fastjson.JSONObject;
import com.jinguduo.spider.cluster.spider.listener.SpiderListener;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.Request;
import com.jinguduo.spider.webmagic.Task;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;

import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 * Created by lc on 2019/5/5
 */
@Slf4j
public class ComicKanmanhuaApiMainDownloaderListener implements SpiderListener {

    private List<String> devices = new ArrayList<>();
    private final static long PERIOD = TimeUnit.MINUTES.toMillis(30);
    private Timer timer = null;

    private static final String GET_TOKEN_URL = "http://getuserinfo.321mh.com/app_api/v5/getuserinfo/?token=%s&type=device";

    @Override
    public void onStart(Task task) {
        timer = new Timer("kanmanhuaTokenLoader");
        timer.scheduleAtFixedRate(new KanmanhuaTokenTask(), 0, PERIOD);
    }

    @Override
    public void onRequest(Request request, Task task) {
        String url = request.getUrl();
        //如果是episodeLikes 任务，则拼接url
        if (StringUtils.contains(url, "getchapterlikelistbycomic")) {
            if (null == devices || devices.size() == 0) {
                try {
                    getKanmanhuaToken();
                } catch (Exception e) {
                    log.error("getuserinfo.321mh.com error ,this message is ->{},e is ->{}", e.getMessage(), e);
                }
            }
            String device = devices.get(RandomUtils.nextInt(0, devices.size()));
            request.setUrl(url + "&userauth=" + device);


        }

    }

    @Override
    public void onResponse(Request request, Page page, Task task) {

    }

    @Override
    public void onError(Request request, Exception e, Task task) {

    }

    @Override
    public void onExit(Task task) {

    }

    class KanmanhuaTokenTask extends TimerTask {
        @Override
        public void run() {
            //拿token
            //扔到devices 里
            try {
                getKanmanhuaToken();
            } catch (Exception e) {
                log.error("getuserinfo.321mh.com error ,this message is ->{},e is ->{}", e.getMessage(), e);
            }
        }
    }

    public void getKanmanhuaToken() throws Exception {
        //随机生成IMEI码
        Long imei = RandomUtils.nextLong(100000000000000L, 999999999999999L);
        String url = String.format(GET_TOKEN_URL, imei);
        HttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(url);
        HttpResponse response = httpClient.execute(httpGet);
        InputStream content = response.getEntity().getContent();
        String result = IOUtils.toString(content, "utf-8");
        JSONObject jsonObject = JSONObject.parseObject(result);
        JSONObject auth_data = jsonObject.getJSONObject("auth_data");
        String authcode = auth_data.getString("authcode");
        authcode = URLEncoder.encode(authcode, "UTF-8");
        devices.add(authcode);
        //log.info("getuserinfo.321mh.com --> add to devices ,this devices is ->{}",devices.toString());
        if (devices.size() > 4) {
            devices.remove(0);
        }

    }
}
