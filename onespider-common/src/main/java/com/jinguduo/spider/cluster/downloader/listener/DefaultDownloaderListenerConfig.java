package com.jinguduo.spider.cluster.downloader.listener;

class DefaultDownloaderListenerConfig {
    private static DownloaderListenerConfig conf = new DownloaderListenerConfig();
    
    static {
        //conf.addListener(new ExceptionSpiderListener(), 0);
    }
    
    public DownloaderListenerConfig getConfig() {
        return conf;
    }
}