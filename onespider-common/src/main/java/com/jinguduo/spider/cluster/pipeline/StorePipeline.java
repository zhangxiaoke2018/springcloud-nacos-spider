package com.jinguduo.spider.cluster.pipeline;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;

import com.jinguduo.spider.webmagic.ResultItems;
import com.jinguduo.spider.webmagic.Task;
import com.jinguduo.spider.webmagic.pipeline.Pipeline;

import lombok.extern.apachecommons.CommonsLog;

@CommonsLog
public class StorePipeline implements Pipeline {

	private final String key;

	private final String storeUrl;
	
	@Autowired
	private final RestTemplate simpleHttp;
	
	public StorePipeline(RestTemplate simpleHttp, final Class<?> clazz, final String storeUrl) {
		this(simpleHttp, clazz.getSimpleName(), storeUrl);
	}
	
	public StorePipeline(RestTemplate simpleHttp, final String key, final String storeUrl) {
		this.key = key;
		this.storeUrl = storeUrl;
		this.simpleHttp = simpleHttp;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void process(ResultItems resultItems, Task task) {
		
		Object object = resultItems.get(getKey());
		if (object != null) {
			if (log.isDebugEnabled()) {
				log.debug(object.toString());
			}
			if (object.getClass().isArray()) {
				for (Object o : (Object[]) object) {
					send(o);
				}
			} else if (object instanceof Collection) {
				for (Object o : (Collection<Object>) object) {
				    send(o);
				}
			} else {
			    send(object);
			}
		}
	}
	
	private void send(Object object) {
        simpleHttp.postForObject(storeUrl, object, String.class);
	}

	public String getKey() {
		return key;
	}
}
