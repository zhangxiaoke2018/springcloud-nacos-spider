package com.jinguduo.spider.common.code;

import org.apache.commons.codec.digest.DigestUtils;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 16/11/2016 3:27 PM
 */
public class SoNewsFetchCode implements FetchCode {
    @Override
    public String get(String url) {
        return DigestUtils.md5Hex(url);
    }
}
