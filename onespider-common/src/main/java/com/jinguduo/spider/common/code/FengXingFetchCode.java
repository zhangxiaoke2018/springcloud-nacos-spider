package com.jinguduo.spider.common.code;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @USER xiaoyun
 * @DATE 2016/10/9 11:45
 */
public class FengXingFetchCode implements FetchCode {
    @Override
    public String get(String url) {
        String furl = "";
        if (url.contains("?")) {
            furl = url.substring(0, url.lastIndexOf("?"));
        } else {
            furl = url;
        }
        return furl.split("/")[4];
    }
}
