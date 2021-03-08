package com.jinguduo.spider.spider.sogou;

import com.jinguduo.spider.cluster.spider.listener.SpiderListener;
import com.jinguduo.spider.common.exception.QuickException;
import com.jinguduo.spider.data.loader.SogouWechatCookieStoreLoader;
import com.jinguduo.spider.data.table.WechatSogouCookie;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.Request;
import com.jinguduo.spider.webmagic.Task;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 * 保存并重用Cookie
 */
@Slf4j
public class WechatCookieDownloaderListener implements SpiderListener {

    @Autowired
    private SogouWechatCookieStoreLoader loader;
    private List<WechatSogouCookie> devices = null;

    private final static int LOAD_SIZE = 2000;

    private final static long PERIOD = TimeUnit.MINUTES.toMillis(1);

    private Timer timer = null;

    @Override
    public void onStart(Task task) {
        if (loader != null && timer == null) {
            timer = new Timer("sogouWechatCookieStoreLoader");
            timer.scheduleAtFixedRate(new SogouWechatCookieStoreLoaderTask(), 0, PERIOD);
        }
    }

    @Override
    public void onRequest(Request request, Task task) {
        try {
            String url = request.getUrl();
            if (StringUtils.contains(url, "share")) {
                return;
            }


            if (devices == null) {
                throw new QuickException("The SogouWechatCookie is empty");
            }
            WechatSogouCookie device = devices.get(RandomUtils.nextInt(0, devices.size()));

//            log.info("www.sogouwechat.log => get cookie ,this cookie  is =>" + device.getCookie());

            if (device == null || StringUtils.isBlank(device.getCookie())) {
                return;
            }

           // request.addHeader("Cookie",device.getCookie());
            for (String c : device.getCookie().split(";")) {
                if (StringUtils.isNotBlank(c)) {
                    String[] ck = c.split("=");
                    request.addCookie(ck[0], ck[1]);
                }
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
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

    class SogouWechatCookieStoreLoaderTask extends TimerTask {
        @Override
        public void run() {
            try {
                devices = loader.load(LOAD_SIZE);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

}
