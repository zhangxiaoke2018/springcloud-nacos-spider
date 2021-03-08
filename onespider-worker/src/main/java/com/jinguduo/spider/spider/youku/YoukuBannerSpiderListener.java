package com.jinguduo.spider.spider.youku;

import com.jinguduo.spider.cluster.spider.listener.SpiderListener;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.Request;
import com.jinguduo.spider.webmagic.Task;

public class YoukuBannerSpiderListener implements SpiderListener{

	@Override
	public void onStart(Task task) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onRequest(Request request, Task task) {
		// TODO Auto-generated method stub
		if(request.getUrl().contains("#MOBILE_HOME_")||request.getUrl().contains("#MOBILE_CHANNEL_")) {
			request.addHeader("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 11_0 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/11.0 Mobile/15A372 Safari/604.1");
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
