package com.jinguduo.spider.common.code;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @USER xiaoyun
 * @DATE 2016/10/9 11:48
 */
public class DouBanFetchCode implements FetchCode {
    @Override
    public String get(String url) {
        String code = "";

        //String doubanurl = "https://movie.douban.com/subject/26614088/";
        Pattern pattern = Pattern.compile("https://movie.douban.com/\\w+/(\\d+)/", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(url);
        if (matcher.find()) {
            code = matcher.group(1);
        }
        return code;
    }
}
