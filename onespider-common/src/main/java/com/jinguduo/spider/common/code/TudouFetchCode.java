package com.jinguduo.spider.common.code;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @USER xiaoyun
 * @DATE 2016/10/9 11:40
 */
public class TudouFetchCode implements FetchCode {
    @Override
    public String get(String url) {
        String code = "";
        if (url.contains("albumplay")) {
            Pattern pattern = Pattern.compile("albumplay/(\\S*)(/|\\.html)");
            Matcher matcher = pattern.matcher(url);
            if (matcher.find()) {
                String c = matcher.group(1);
                if (c.contains("/"))
                    code = c.substring(0,c.indexOf("/"));
                else
                    code = c;
            }
        }else if(url.contains("listplay")){//自媒体
            code = url.substring(url.lastIndexOf("/") + 1, url.lastIndexOf(".html"));
        }else if(url.contains("programs")){
            code = url.split("/")[5];
        }
        else {
            code = url.substring(url.lastIndexOf("/") + 1, url.lastIndexOf(".html"));
        }
        return code;
    }
}
