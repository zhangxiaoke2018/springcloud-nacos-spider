package com.jinguduo.spider.data.table;

import org.apache.commons.lang3.StringUtils;

/**
 * Created by lc on 2017/6/8.
 */
public enum Classify {

    WECHAT_ARTICLE("微信文章"),
    HAOSOU_INDEX("360指数"),
    BAIDU_INDEX("百度指数"),
    TOUTIAO("头条"),
    WEIBO_INDEX("微博指数"),
    BAIDU_VIDEO("百度视频搜索"),
    HAOSOU_NEWS("360新闻搜索"),
    BAIDU_NEWS("百度新闻搜索"),
    BAIDU_TIEBA("贴吧"),
    WEIBO_SEARCH("微博搜索"),
    SOUGOUWECHAT_SEARCH("搜狗微信搜索"),
    BILIBILI_SEARCH("B站搜索")
    ;

    Classify(String desc) {
        this.desc = desc;
    }

    private final String desc;

    public String getDesc() {
        return desc;
    }

    public static String getDesc(Classify classify){
        for (Classify c : Classify.values()) {
            if(c.equals(classify)){
                return c.getDesc();
            }
        }
        return null;
    }

    public static Boolean exist(String classify){
        if(StringUtils.isBlank(classify)){
            return Boolean.FALSE;
        }
        for (Classify c : Classify.values()) {
            if(c.name().equals(classify)){
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }

    public static Classify include(String classify){

        if (StringUtils.isBlank(classify)) return null;

        for (Classify ca : Classify.values()) {
            if(ca.name().equals(classify)){
                return ca;
            }
        }
        return null;
    }
}
