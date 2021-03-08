package com.jinguduo.spider.common.constant;

public enum JobKind {
	Once, Forever;

    //@JsonValue
    public int getValue() {
        return ordinal();
    }
}
