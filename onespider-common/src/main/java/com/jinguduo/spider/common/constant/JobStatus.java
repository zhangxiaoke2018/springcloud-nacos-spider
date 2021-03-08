package com.jinguduo.spider.common.constant;

public enum JobStatus {
	Unallocated(0), Allocated(1);

	private JobStatus(int value) {
        this.value = value;
    }

    private int value;

    public int getValue() {
        return value;
    }

    public static JobStatus valueOf(int value) {
        for (JobStatus status : values()) {
            if (status.getValue() == value) {
                return status;
            }
        }
        throw new IllegalArgumentException();
    }
}
