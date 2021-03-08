package com.jinguduo.spider.common.code;

import com.jinguduo.spider.common.util.HttpHelper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @USER xiaoyun
 * @DATE 2016/10/9 11:43
 */
public class MgtvFetchCode implements FetchCode {
    @Override
    public String get(String url) {
        if (url.contains("/v/2/")) {//网剧
            return url.split("/")[5];
        }else if(url.contains("/h/")){//动漫
            return url.substring(url.lastIndexOf("/h/")+3,url.lastIndexOf("."));
        } else {//网综
            String html = HttpHelper.get(url, "UTF-8").replace(" ", "");

            Pattern pattern = Pattern.compile("cid:\"(\\d+)\"", Pattern.DOTALL);
            Matcher matcher = pattern.matcher(html);
            if (matcher.find()) {
                return matcher.group(1).replace("\"","");
            }
        }
        return "";
    }
}
