package com.jinguduo.spider.common.code;

import org.apache.commons.lang3.StringUtils;

import com.jinguduo.spider.common.util.HttpHelper;
import com.jinguduo.spider.common.util.RegexUtil;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @USER xiaoyun
 * @DATE 2016/10/9 11:23
 */
public class TengXunFetchCode implements FetchCode {
    @Override
    public String get(String url) {
        if(StringUtils.contains(url, "autoFind")){
            String resStr = HttpHelper.get(url, "UTF-8");
            String code = RegexUtil.getDataByRegex(resStr, "\"column_id\":(\\d+),", 1);
            if(StringUtils.isNotBlank(code)){
                return code;
            }else{
                return null;
            }
        }else{
            return url.substring(url.lastIndexOf("/") + 1, url.lastIndexOf(".html"));
        }
    }
}
