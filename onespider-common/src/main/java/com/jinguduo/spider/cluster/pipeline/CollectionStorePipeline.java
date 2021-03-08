package com.jinguduo.spider.cluster.pipeline;

import java.util.Collection;

import org.springframework.web.client.RestTemplate;

import com.jinguduo.spider.webmagic.ResultItems;
import com.jinguduo.spider.webmagic.Task;
import com.jinguduo.spider.webmagic.pipeline.Pipeline;

public class CollectionStorePipeline implements Pipeline {

	private final String key;

	private final String storeUrl;
	
	private final RestTemplate simpleHttp;
	
	public CollectionStorePipeline(RestTemplate simpleHttp, final Class<?> clazz, final String storeUrl) {
		this(simpleHttp, clazz.getSimpleName(), storeUrl);
	}
	
	public CollectionStorePipeline(RestTemplate simpleHttp, final String key, final String storeUrl) {
		this.key = key;
		this.storeUrl = storeUrl;
		this.simpleHttp = simpleHttp;
	}

	@Override
	public void process(ResultItems resultItems, Task task) {
		Object object = resultItems.get(getKey());
		if (object != null) {
            if (object.getClass().isArray() || object instanceof Collection) {
                send(object);
            } else {
                send(new Object[]{object});
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
