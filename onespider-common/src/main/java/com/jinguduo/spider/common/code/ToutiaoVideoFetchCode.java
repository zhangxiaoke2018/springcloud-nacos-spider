package com.jinguduo.spider.common.code;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * I'm a cute code dog
 * User: LingChen
 * Date:2018/3/2
 * Time:14:05
 */
public class ToutiaoVideoFetchCode implements FetchCode {
    @Override
    public String get(String url) {
        return DigestUtils.md5Hex(url);
    }
}
