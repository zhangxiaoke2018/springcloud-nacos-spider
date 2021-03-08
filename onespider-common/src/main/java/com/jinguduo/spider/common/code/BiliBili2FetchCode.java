package com.jinguduo.spider.common.code;

import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 10/11/2016 9:44 AM
 */
@CommonsLog
public class BiliBili2FetchCode implements FetchCode {
    @Override
    public String get(String url) {

        try {
            Pattern p = Pattern.compile("www.bilibili.com/bangumi/media/(md\\d+)/");
            Matcher matcher = p.matcher(url);
            String code = "";
            if(matcher.find()){
                code = matcher.group(1);
            }
            if(StringUtils.isBlank(code)){
                return "";
            }

            return new URI(url).getHost() + code;
        } catch (URISyntaxException e) {
            log.error(e.getMessage(), e);
        }
        return "";
    }
}
