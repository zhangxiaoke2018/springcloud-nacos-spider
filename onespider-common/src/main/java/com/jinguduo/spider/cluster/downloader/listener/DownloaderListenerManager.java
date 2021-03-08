package com.jinguduo.spider.cluster.downloader.listener;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.http.HttpResponse;

import com.jinguduo.spider.cluster.downloader.HttpClientRequestContext;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.common.util.OrderedComponent;
import com.jinguduo.spider.webmagic.Request;
import com.jinguduo.spider.webmagic.Task;

/**
 * 管理DownloaderListener
 *   
 */
public class DownloaderListenerManager {
	
	private static Map<String, DownloaderListenerConfig> configs = new ConcurrentHashMap<>();
	
	private DownloaderListenerManager() {
	}

	public static void processRequest(HttpClientRequestContext requestContext, Request req, Task task) {
	    getConfig(task).processRequest(requestContext, req, task);
	}

	public static void processResponse(HttpClientRequestContext requestContext, Request req, HttpResponse resp, Task task) {
	    getConfig(task).processResponse(requestContext, req, resp, task);
	}

	public static void processError(HttpClientRequestContext requestContext, Request req, Exception e, Task task) {
	    getConfig(task).processError(requestContext, req, e, task);
	}
	
	public static void addDownloaderListener(String domain, List<OrderedComponent<DownloaderListener>> Listeners) {
	    DownloaderListenerConfig conf = configs.get(domain);
	    if (conf == null) {
	        conf = new DownloaderListenerConfig(new DefaultDownloaderListenerConfig().getConfig());
	        configs.put(domain, conf);
        }
	    conf.addListeners(Listeners);
	}
	
	private static DownloaderListenerConfig getConfig(Task task) {
	    DownloaderListenerConfig conf = configs.get(getDomain(task));
	    if (conf == null) {
            conf = new DefaultDownloaderListenerConfig().getConfig();
        }
	    return conf;
	}

    private static String getDomain(Task task) {
	    if (task != null) {
	        Site site = (Site)task.getSite();
	        if (site != null) {
	            String domain = site.getDomain();
	            return domain == null ? "" : domain;
	        }
	    }
	    return "";
	}
}
