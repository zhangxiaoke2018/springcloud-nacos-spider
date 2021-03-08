package com.jinguduo.spider.common.code;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @USER xiaoyun
 * @DATE 2016/10/9 11:47
 */
public class PPTVFetchCode implements FetchCode {
    @Override
    public String get(String url) {
        return url.substring(url.lastIndexOf("/")+1, url.lastIndexOf(".html"));
    }
}
