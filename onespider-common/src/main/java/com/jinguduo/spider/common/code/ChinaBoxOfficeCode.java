package com.jinguduo.spider.common.code;


import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChinaBoxOfficeCode implements  FetchCode {
    @Override
    public String get(String url) {
        String code=null;
        try {
            Pattern p= Pattern.compile("(\\d+)");
            Matcher m=p.matcher(url);
            if(m.find()) {
                code = new URI(url).getHost().toString() + ":" +m.group(1);
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return  "" ;
        };
        return code;
    }



}
