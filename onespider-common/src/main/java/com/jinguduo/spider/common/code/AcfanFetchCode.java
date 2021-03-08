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
public class AcfanFetchCode implements FetchCode {
    @Override
    public String get(String url) {//http://www.acfun.tv/v/ab1470468_1

        try {
            return new URI(url).getHost() + url.substring(url.lastIndexOf("/ab")+3,url.lastIndexOf("_"));
        } catch (URISyntaxException e) {
            log.error(e.getMessage(), e);
        }
        return "";
    }
}
