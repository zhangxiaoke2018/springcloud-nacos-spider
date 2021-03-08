package com.jinguduo.spider.common.code;

import java.net.URI;
import java.net.URISyntaxException;

import lombok.extern.apachecommons.CommonsLog;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 10/11/2016 10:49 AM
 */
@CommonsLog
public class KumiFetchCode implements FetchCode {
    @Override
    public String get(String url) {//http://www.kumi.cn/donghua/85063.html
        try {
            return new URI(url).getHost() + url.substring(url.lastIndexOf("/")+1,url.lastIndexOf("."));
        } catch (URISyntaxException e) {
            log.error(e.getMessage(), e);
        }
        return "";
    }
}
