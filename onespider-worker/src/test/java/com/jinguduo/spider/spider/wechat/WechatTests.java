package com.jinguduo.spider.spider.wechat;

import com.jinguduo.spider.common.util.IoResourceHelper;
import lombok.extern.apachecommons.CommonsLog;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 14/03/2017 2:48 PM
 */
@CommonsLog
public class WechatTests {

    final static String WECHAT_RAW_TEXT = IoResourceHelper.readResourceContent("/html/wechat.html");


    private static Pattern wechatAccountPattern = Pattern.compile("\\\">(\\S*)<");


    public void pickWechatAccount(){
        String profileMeta = pick(WECHAT_RAW_TEXT, "profile_meta_value", wechatAccountPattern, 1, "");

        log.debug("wa:" + profileMeta);

    }

    private static Pattern postUserPattern = Pattern.compile("\">(\\S*)<");


    public void pickPostUser(){

        String pick = pick(WECHAT_RAW_TEXT, "post-user", postUserPattern, 1, "");

        log.debug("wa:" + pick);
    }


    private String pick(String s, String prefix, Pattern pattern, int idx, String defaultValue) {
        final int i = s.indexOf(prefix);
        if (i >= 0) {
            String substring = s.substring(i, s.indexOf(';', i));
            Matcher matcher = pattern.matcher(substring);
            if (matcher.find()) {
                return matcher.group(idx);
            }
        } else {
            log.warn("The [" + prefix +"] is null.");
        }
        return defaultValue;
    }

    @Test
    public void pickContent(){
        org.jsoup.nodes.Document html = Jsoup.parse(WECHAT_RAW_TEXT);
        Element jsContent = html.getElementById("js_content");
        String text1 = html.text();
        String text = jsContent.text();
    }
}
