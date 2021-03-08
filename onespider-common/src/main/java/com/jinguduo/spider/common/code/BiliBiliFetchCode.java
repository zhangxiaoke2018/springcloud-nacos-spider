package com.jinguduo.spider.common.code;

import java.net.URI;
import java.net.URISyntaxException;

import lombok.extern.apachecommons.CommonsLog;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 10/11/2016 9:44 AM
 */
@CommonsLog
public class BiliBiliFetchCode implements FetchCode {
    @Override
    public String get(String url) {//http://bangumi.bilibili.com/anime/5626

        try {
            return new URI(url).getHost() + url.substring(url.lastIndexOf("/")+1,url.length());
        } catch (URISyntaxException e) {
            log.error(e.getMessage(), e);
        }
        return "";
    }

    public static void main(String[] args) {
        BiliBiliFetchCode biliBiliFetchCode = new BiliBiliFetchCode();
        System.out.println(biliBiliFetchCode.get("http://bangumi.bilibili.com/anime/5104"));
    }
}
