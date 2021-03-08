package com.jinguduo.spider.spider.toutiao;

import com.jinguduo.spider.cluster.spider.listener.SpiderListener;
import com.jinguduo.spider.common.util.Md5Util;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.Request;
import com.jinguduo.spider.webmagic.Task;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;

/**
 * 保存并重用Cookie
 */
@Slf4j
public class ToutiaoDownloaderListener implements SpiderListener {


    String[] WEB_IDS = null;

    int idsSize = 100;

    @Override
    public void onStart(Task task) {
        WEB_IDS = new String[idsSize];
        //随机扔100个md5进入
        for (int i = 0; i < idsSize; i++) {

            WEB_IDS[i] = Md5Util.getMd5(String.valueOf(RandomUtils.nextDouble()));
        }

    }

    @Override
    public void onRequest(Request request, Task task) {

        request.addCookie("s_v_web_id", WEB_IDS[RandomUtils.nextInt(0, idsSize)]);

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

}
