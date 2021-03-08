package com.jinguduo.spider.cluster.engine;

import java.util.List;

import com.jinguduo.spider.cluster.downloader.ImprovedDownloader;
import com.jinguduo.spider.cluster.scheduler.DistributedScheduler;
import com.jinguduo.spider.cluster.spider.SpiderSettingLoader;
import com.jinguduo.spider.cluster.spider.listener.SpiderListener;
import com.jinguduo.spider.cluster.worker.Heartbeat;
import com.jinguduo.spider.common.proxy.ProxyPoolManager;
import com.jinguduo.spider.webmagic.pipeline.Pipeline;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SpiderEngineConfig {

    private SpiderSettingLoader spiderSettingLoader;

    private Heartbeat heartbeat;

    private DistributedScheduler scheduler;
    
    private ImprovedDownloader downloader;

    private List<Pipeline> pipelines;
    
    private List<SpiderListener> spiderListeneres;
    
    private ProxyPoolManager proxyPoolManager;
}
