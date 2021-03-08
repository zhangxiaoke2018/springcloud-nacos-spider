package com.jinguduo.spider.spider.sohu;

import java.net.URISyntaxException;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.http.client.utils.URIBuilder;

import com.jinguduo.spider.cluster.spider.listener.SpiderListener;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.Request;
import com.jinguduo.spider.webmagic.Task;

import lombok.extern.slf4j.Slf4j;

/**
 * 针对搜狐的反爬虫策略
 */
@Slf4j
public class SohuSpiderUrlRewriter implements SpiderListener {

	@Override
	public void onStart(Task task) {
		// no-op
	}

	@Override
	public void onRequest(Request request, Task task) {
		try {
			URIBuilder uriBuilder = new URIBuilder(request.getUrl());
			uriBuilder.setParameter("_t_", RandomStringUtils.randomNumeric(12));
			uriBuilder.setScheme("https");
			String newUrl = uriBuilder.toString();
			request.setUrl(newUrl);
			
		} catch (URISyntaxException e) {
			log.error(e.getMessage(), e);
		}
	}

	@Override
	public void onResponse(Request request, Page page, Task task) {
		// no-op
	}

	@Override
	public void onError(Request request, Exception e, Task task) {
		// no-op
	}

	@Override
	public void onExit(Task task) {
		// no-op
	}
}
