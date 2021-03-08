package com.jinguduo.spider.spider.fiction;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.jinguduo.spider.cluster.spider.listener.SpiderListener;
import com.jinguduo.spider.common.util.Md5Util;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.Request;
import com.jinguduo.spider.webmagic.Task;

public class QQReaderHeaderListener implements SpiderListener{

	@Override
	public void onStart(Task task) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onRequest(Request request, Task task) {
		// TODO Auto-generated method stub
		headers().forEach((k,v)->request.addHeader(k, v));
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

	private Map<String,String> headers(){
		String qrem = "0";
		String qrtm = String.valueOf((int) (System.currentTimeMillis()/1000));
		String c_version ="qqreader_6.6.6.0888_android";
		String channel = "10031939";
		String qrsy= Md5Util.getMd5(String.join("|", c_version,channel,"0",").#@!U_*#@DxL09V",qrtm,qrem)).toUpperCase();
		return ImmutableMap.of("qrem",qrem,"qrtm",qrtm,"c_version",c_version,"channel",channel,"qrsy",qrsy);
	}
}
