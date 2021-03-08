package com.jinguduo.spider.spider.tengxun;

import com.jinguduo.spider.cluster.spider.listener.SpiderListener;
import com.jinguduo.spider.common.util.tengxun.CryptUtils;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.Request;
import com.jinguduo.spider.webmagic.Task;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * I'm a cute code dog
 * User: LingChen
 * Date:2017/11/8
 * Time:10:19
 */
@Slf4j
public class TengxunAndroidComicDownLoaderListener implements SpiderListener {
    private static final String prefix = "android.ac.qq.com";

    @Override
    public void onStart(Task task) {
    }

    @Override
    public void onRequest(Request request, Task task) {
        String url = request.getUrl();
        String sc = sendRequestGetSc(url);
        request.addHeader("sc", sc);
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


    private String sendRequestGetSc(String url) {
        String suffix = StringUtils.substring(url, url.indexOf(prefix) + prefix.length());
        String scInit = suffix + prefix;
        return DigestUtils.md5Hex(scInit);

    }

    public static void main(String[] args) {
        String url = "https://android.ac.qq.com/7.21.3/Rank/rankDetail/rank_id/11/page/1/user_qq/0/";
        String suffix = StringUtils.substring(url, url.indexOf(prefix) + prefix.length());
        String scInit = suffix + prefix;
        System.out.println(DigestUtils.md5Hex(scInit));

    }



}
