package com.jinguduo.spider.cluster.worker;

import com.jinguduo.spider.cluster.engine.SpiderEngine;
import com.jinguduo.spider.cluster.engine.SpiderEngineConfig;
import com.jinguduo.spider.cluster.scheduler.DistributedScheduler;
import com.jinguduo.spider.cluster.spider.Spider;
import com.jinguduo.spider.common.constant.SpiderStatus;
import com.jinguduo.spider.common.constant.WorkerCommand;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SpiderWorker {
    
    private Spider spider;
    private SpiderEngineConfig spiderEngineConfig;
    private SpiderEngine spiderEngine;
    private Heartbeat heartbeat;
    private DistributedScheduler scheduler;
    
    private String uuid; //RandomStringUtils.randomAlphanumeric(5);
    
    // 记录最后收到并执行过的指令
    private volatile WorkerCommand command = WorkerCommand.Noop;
    
    private volatile int syncVersion = 0;
    
    public SpiderStatus getStatus() {
        if (spiderEngine == null) {
            return null;
        }
        return spiderEngine.getStatus();
    }
}
