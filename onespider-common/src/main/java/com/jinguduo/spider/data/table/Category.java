package com.jinguduo.spider.data.table;

import org.apache.commons.lang3.StringUtils;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @DATE 16/7/8 下午4:23
 */
public enum Category {

    NETWORK_VARIETY("网络综艺"),
    NETWORK_MOVIE("网络大电影"),
    NETWORK_DRAMA("网络剧"),
    TV_VARIETY("电视综艺"),
    TV_DRAMA("电视剧"),
    ANIME("国漫"),
    JAPAN_ANIME("日漫"),
    KID_ANIME("少儿动漫"),
    MOVIE("院线电影"),
    DOCUMENTARY("纪录片"),
    FOREIGN_KID_ANIME("国外少儿动漫"),
    KID_ANIME_MOVIE("少儿动画电影"),
    FOREIGN_KID_ANIME_MOVIE("国外少儿动画电影"),


    MEDIA_DATA("媒体指数");

    Category(String desc) {
        this.desc = desc;
    }

    private final String desc;

    public String getDesc() {
        return desc;
    }

    public static String getDesc(Category category){
        for (Category c : Category.values()) {
            if(c.equals(category)){
                return c.getDesc();
            }
        }
        return null;
    }

    public static Boolean exist(String category){
        if(StringUtils.isBlank(category)){
            return Boolean.FALSE;
        }
        for (Category c : Category.values()) {
            if(c.name().equals(category)){
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }

    public static Category include(String category){

        if (StringUtils.isBlank(category)) return null;

        for (Category ca : Category.values()) {
            if(ca.name().equals(category)){
                return ca;
            }
        }
        return null;
    }
}
