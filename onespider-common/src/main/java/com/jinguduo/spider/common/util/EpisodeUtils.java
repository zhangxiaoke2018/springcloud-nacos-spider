package com.jinguduo.spider.common.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;


public class EpisodeUtils {

    //将优酷的某些综艺分集统一至yyyyMMdd格式
    public static Integer youkuEpisodeHandle(String episode){
        if(StringUtils.isBlank(episode)){
            return 0;
        }
        String strEpi = String.valueOf(episode);
        Pattern regex = Pattern.compile("^[0|1][0-9][0|1][0-9][0-3][0-9]");
        Pattern regexSpecial = Pattern.compile("^[0-9][0|1][0-9][0-3][0-9]");
        Matcher match = regex.matcher(strEpi);
        if(match.find()){
            return Integer.valueOf("20"+episode);
        }else if(regexSpecial.matcher(strEpi).find()){
            return Integer.valueOf("201"+episode);
        }
        return Integer.valueOf(episode);
    }
    
    public static void main(String[] args) {
        System.out.println(youkuEpisodeHandle("162124"));
        System.out.println(youkuEpisodeHandle("161124"));
        System.out.println(youkuEpisodeHandle("22"));
        System.out.println(youkuEpisodeHandle("20170201"));
        System.out.println(youkuEpisodeHandle("20201"));
    }
}
