package com.jinguduo.spider.common.util;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by csonezp on 2016/8/25.
 */
public class RegexUtil {
	public static String getStringByPattern(String text, Pattern pattern, int group) {
		if (StringUtils.isBlank(text)) {
            return null;
        }
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            return matcher.group(group).trim();
        }
        return null;
	}
	
    public static String getDataByRegex(String target, String regex) {
    	return getStringByPattern(target, Pattern.compile(regex), 1);
    }

    public static String getDataByRegex(String target, String regex, int group) {
        return getStringByPattern(target, Pattern.compile(regex), group);
    }

    public static String getDataByRegex(String target, String regex, int group, int patternTag) {
        if (StringUtils.isBlank(target)) {
            return null;
        }
        Pattern pattern = Pattern.compile(regex, patternTag);
        Matcher matcher = pattern.matcher(target);
        while (matcher.find()) {
            return matcher.group(group).trim();
        }
        return null;
    }

    /**
     * 匹配第几个匹配项
     * @param target
     * @param regex
     * @param index 从0开始
     * @param group
     * @return
     */
    public static String getDataByRegexAndIndex(String target, String regex, int index, int group) {
        if (StringUtils.isBlank(target)) {
            return null;
        }
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(target);

        try {
            for (int i = 0; i < index; i++) {
                matcher.find();
            }
            matcher.find();
            return matcher.group(group).trim();
        } catch (Exception e) {
            return null;
        }

    }

    public static  String getLastIndexDataByRegex(String target,String regex,int group){
        if (StringUtils.isBlank(target)) {
            return null;
        }
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(target);
        String res = "";
        while (matcher.find()){
            res = matcher.group(group).trim();
        }
        return res;
    }

}
