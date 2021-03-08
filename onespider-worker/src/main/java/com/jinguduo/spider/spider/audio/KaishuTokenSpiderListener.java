package com.jinguduo.spider.spider.audio;

import com.jinguduo.spider.cluster.spider.listener.SpiderListener;
import com.jinguduo.spider.common.util.RegexUtil;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.Request;
import com.jinguduo.spider.webmagic.Task;

public class KaishuTokenSpiderListener implements SpiderListener {

	private static final String initialToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJhdWQiOiI5OTIwOTkwMDEiLCJpbCI6dHJ1ZSwiaXNzIjoia2Fpc2h1c3RvcnkiLCJleHAiOjU2NzgxMjM0MTcsInVkIjowfQ.M-rFkjVJZgG2RdLmore_Z4YsO7CoAok6fpD397dt9pk";

	@Override
	public void onStart(Task task) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onRequest(Request request, Task task) {
		// TODO Auto-generated method stub
		String url = request.getUrl();
		if (url.contains("initialize")) {
			request.addHeader("token", initialToken);
		} else {
			String appendedToken = RegexUtil.getDataByRegex(url, "&token=([^&]+)");
			if (null != appendedToken) {
				String newUrl = url.replace("&token=" + appendedToken, "");
				request.setUrl(newUrl).addHeader("token", appendedToken);
			}
		}

	}

	@Override
	public void onResponse(Request request, Page page, Task task) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onError(Request request, Exception e, Task task) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onExit(Task task) {
		// TODO Auto-generated method stub

	}

}
