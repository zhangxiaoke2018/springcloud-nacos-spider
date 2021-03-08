package com.jinguduo.spider.spider.youku;

import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.http.client.utils.URIBuilder;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.jinguduo.spider.cluster.spider.listener.SpiderListener;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.Request;
import com.jinguduo.spider.webmagic.Task;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class YoukuMobileAppUrlSpiderListener implements SpiderListener {
	
	@SuppressWarnings("deprecation")
    private final HashFunction md5 = Hashing.md5();
	
	private final static String PREFIX = "GET:";
	private final static String SEPARATOR = ":";
	private final static String SALT = ":631l1i1x3fv5vs2dxlj5v8x81jqfs2om";

	@Override
	public void onStart(Task task) {
		// no-op
	}

	@Override
	public void onRequest(Request request, Task task) {
		try {
			URIBuilder uriBuilder = new URIBuilder(request.getUrl());
			
			String ts = String.valueOf((int)(System.currentTimeMillis() / 1000L));
			String signature = md5.newHasher()
					.putString(PREFIX, StandardCharsets.UTF_8)  // prefix
					.putString(uriBuilder.getPath(), StandardCharsets.UTF_8)  // path
					.putString(SEPARATOR, StandardCharsets.UTF_8)  // separator
					.putString(ts, StandardCharsets.UTF_8)  // unix timestamp
					.putString(SALT, StandardCharsets.UTF_8)  // salt
					.hash()
					.toString();
			// replace id
			uriBuilder.setParameter("pid", RandomStringUtils.randomAlphanumeric(16).toLowerCase());
			uriBuilder.setParameter("guid", RandomStringUtils.randomAlphanumeric(32).toLowerCase());
			uriBuilder.setParameter("imei", RandomStringUtils.randomNumeric(15));
			// replace timestamp & signature
			uriBuilder.setParameter("_t_", ts);
			uriBuilder.setParameter("_s_", signature);
			
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
