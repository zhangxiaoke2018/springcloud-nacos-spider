package com.jinguduo.spider.common.constant;
/**
 * 
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @author liuxinglong
 * @DATE 2017年3月31日 下午4:24:07
 *
 */
public enum StatusEnum {
    STATUS_OK(0,"正常"),
    STATUS_DEL(-1,"删除");

    private int value;
    private String desc;
    
    private StatusEnum(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }
    
    
    public int getValue() {
        return value;
    }

    public String getDesc() {
        return desc;
    }
}
