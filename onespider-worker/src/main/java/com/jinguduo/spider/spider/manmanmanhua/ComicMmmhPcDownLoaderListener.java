package com.jinguduo.spider.spider.manmanmanhua;

import com.jinguduo.spider.cluster.spider.listener.SpiderListener;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.Request;
import com.jinguduo.spider.webmagic.Task;
import com.jinguduo.spider.webmagic.model.HttpRequestBody;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * I'm a cute code dog
 * User: LingChen
 * Date:2017/11/8
 * Time:10:19
 */
@Slf4j
public class ComicMmmhPcDownLoaderListener implements SpiderListener {
    @Override
    public void onStart(Task task) {
    }

    @Override
    public void onRequest(Request request, Task task) {
        String url = request.getUrl();
        String body = StringUtils.substring(url, StringUtils.indexOf(url, "?") + 1);
        String newUrl = StringUtils.replace(url, "?"+body, "");
        HttpRequestBody hrb = new HttpRequestBody();
        hrb.setBody(body.getBytes());
        request.setUrl(newUrl);
        request.setRequestBody(hrb);
        request.setMethod("POST");

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
