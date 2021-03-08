package com.jinguduo.spider.cluster.downloader;

import lombok.Data;

@Data
public class HttpClientConfig {

	private int maxConnectionPerRoute = 4;
	private int maxConnection = 200;
	private int maxIdleTime = 2000;  // milliseconds
	private int timeToLive = 60;  // minutes
}
