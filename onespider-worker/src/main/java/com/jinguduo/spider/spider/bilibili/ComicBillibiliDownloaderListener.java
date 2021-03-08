package com.jinguduo.spider.spider.bilibili;

import com.jinguduo.spider.cluster.spider.listener.SpiderListener;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.Request;
import com.jinguduo.spider.webmagic.Task;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by lc on 2019/5/5
 */
@Slf4j
public class ComicBillibiliDownloaderListener implements SpiderListener {


    @Override
    public void onStart(Task task) {
        //
    }

    @Override
    public void onRequest(Request request, Task task) {
        String url = request.getUrl();
        if (url.contains("Comic/GetEntranceForRank")){
            request.addHeader("Content-Type","application/x-www-form-urlencoded");
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


}
