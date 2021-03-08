package com.jinguduo.spider.common.constant;

public enum JobSchedulerCommand {
	Noop(0), Add(1), Delete(2), Update(4);
    
    private JobSchedulerCommand(int value) {
        this.value = value;
    }

    private int value;

    public int getValue() {
        return value;
    }

    public static JobSchedulerCommand valueOf(int value) {
        for (JobSchedulerCommand v : values()) {
            if (v.getValue() == value) {
                return v;
            }
        }
        throw new IllegalArgumentException();
    }
}
