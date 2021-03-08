package com.jinguduo.spider.common.constant;

public enum SpiderStatus {
    Init(0), Running(1), Stopped(2);

    private SpiderStatus(int value) {
        this.value = value;
    }

    private int value;

    int getValue() {
        return value;
    }

    public static SpiderStatus fromValue(int value) {
        for (SpiderStatus spiderStatus : SpiderStatus.values()) {
            if (spiderStatus.getValue() == value) {
                return spiderStatus;
            }
        }
        //default value
        return Init;
    }
}