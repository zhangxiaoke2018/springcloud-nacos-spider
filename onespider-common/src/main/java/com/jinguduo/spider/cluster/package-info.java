package com.jinguduo.spider.cluster;

/**
 * 在WebMagic基础上的改进：分布式调度和Scrapy化
 * <p>增加分布式调度(Supervisor, Scheduler, Worker)
 * <p>改进Downloader，改进DownloaderListener
 * <p>增加SpiderListener
 * <p>增加SpiderSetting存储Site及爬虫相关配置
 * <p>参考Scrapy，增加CrawlSpider类（依据URL路由调用不同方法处理下载的页面）
 * 
 */
