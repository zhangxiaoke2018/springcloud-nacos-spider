package com.jinguduo.spider.spider.bodongComic;

import com.jinguduo.spider.cluster.spider.listener.SpiderListener;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.Request;
import com.jinguduo.spider.webmagic.Task;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;

/**
 * I'm a cute code dog
 * User: LingChen
 * Date:2017/11/8
 * Time:10:19
 */
@Slf4j
public class BodongComicDownLoaderListener implements SpiderListener {

    private static String COOKIE_KEY = "Cookie";
    private static String COOKIE_VALUE_PRE = "comic_login_type=0; comic_token_type=0;uin=";

    @Override
    public void onStart(Task task) {
    }

    @Override
    public void onRequest(Request request, Task task) {
        Long random = RandomUtils.nextLong(100000000000000L, 999999999999999L);

        request.addHeader(COOKIE_KEY, COOKIE_VALUE_PRE + random.toString());
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
