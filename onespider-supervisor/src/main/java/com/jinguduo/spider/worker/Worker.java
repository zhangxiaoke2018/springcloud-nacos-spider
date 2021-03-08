package com.jinguduo.spider.worker;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jinguduo.spider.common.constant.SpiderStatus;
import com.jinguduo.spider.common.constant.WorkerCommand;

public class Worker implements Serializable {
	
	private static final long serialVersionUID = -2519458295450668612L;
	
	private final String hostname;
	private final String uuid;
	private final String domain;
	private volatile int ringIndex = -1;
	private volatile SpiderStatus status = SpiderStatus.Init;
	private volatile WorkerCommand command = WorkerCommand.Noop;
	private volatile long timestamp = System.currentTimeMillis();
	
	public Worker(String hostname, String uuid, String domain) {
		super();
		this.hostname = hostname;
		this.uuid = uuid;
		this.domain = domain;
	}
	
	@JsonIgnore
	public synchronized WorkerCommand touch(SpiderStatus status, WorkerCommand command) {
		this.status =  status;
		this.timestamp = System.currentTimeMillis();

		// 指令已执行
        if (this.command == command) {
            this.command = WorkerCommand.Noop;
        }
		return this.command;
	}
	
	@JsonIgnore
	public synchronized void disconnected() {
		this.status = SpiderStatus.Stopped;
	}
	
	@JsonIgnore
	public boolean isDown() {
		return this.status == SpiderStatus.Stopped;
	}
	
	public String getHostname() {
	    return hostname;
	}

	public String getUuid() {
		return uuid;
	}

	public String getDomain() {
		return domain;
	}

	public SpiderStatus getStatus() {
		return status;
	}

	public WorkerCommand getCommand() {
		return command;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public int getRingIndex() {
		return ringIndex;
	}

	public void setRingIndex(int ringIndex) {
		this.ringIndex = ringIndex;
	}

    public void setCommand(WorkerCommand command) {
        this.command = command;
    }
}