package com.jinguduo.spider.data.table;

import org.apache.commons.lang3.StringUtils;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 13/03/2017 10:44 AM
 */
public enum ExtractCategoryEnum {

    ACTOR,
    DIRECTOR,
    STORY,
    SPECIALEFFECT,
    SOUNDEFFECT;


    public static ExtractCategoryEnum include(String category){

        if (StringUtils.isBlank(category)) return null;

        for (ExtractCategoryEnum extractCategoryEnum : ExtractCategoryEnum.values()) {
            if(extractCategoryEnum.name().equals(category)){
                return extractCategoryEnum;
            }
        }
        return null;
    }
}
