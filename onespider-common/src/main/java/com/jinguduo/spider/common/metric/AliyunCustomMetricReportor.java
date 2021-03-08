package com.jinguduo.spider.common.metric;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.rmi.ServerException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.Assert;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.cms.model.v20180308.PutCustomMetricRequest;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.http.HttpResponse;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.jinguduo.spider.common.thread.AsyncTask;

import lombok.extern.apachecommons.CommonsLog;

/**
 * 阿里云自定义监控（云监控）
 */
@CommonsLog
public class AliyunCustomMetricReportor implements InitializingBean, MetricReportor {

    @Value("${onespider.aliyun.metric.access-key}")
    private String accessKey;
    
    @Value("${onespider.aliyun.metric.access-secret}")
    private String accessSecret;
    
    @Value("${onespider.aliyun.metric.region}")
    private String region;
    
    @Value("${onespider.aliyun.metric.endpoint}")
    private String endpoint;
    
    private IAcsClient acsClient;
    
    private ObjectMapper objectMapper;
    
    private Timer timer = new Timer();
    
    private long period = TimeUnit.MINUTES.toMillis(1);
    
    private final AtomicBoolean isStarted = new AtomicBoolean(false);
    
	protected void send() throws IOException, ServerException, ClientException, InterruptedException {
        Collection<MetricBean> metrics = MetricFactory.getAndResetMetrics();
        
        if (metrics != null && !metrics.isEmpty()) {
        	log.info("size:" + metrics.size());
        	List<AliyunMetricBean> metricBeans = metrics.stream()
        			.map(e -> AliyunMetricBean.of(e))
        			.collect(Collectors.toList());
        	
        	// 单次最多上报100条数据，Body最大为256KB
        	Iterable<List<AliyunMetricBean>> subsets = Iterables.partition(metricBeans, 70);
        	
        	for (List<AliyunMetricBean> beans : subsets) {
        		String s = objectMapper.writeValueAsString(beans);
        		
        		PutCustomMetricRequest request = new PutCustomMetricRequest();
        		request.setMetricList(s);
        		
        		HttpResponse resp = acsClient.doAction(request);
        		if (!resp.isSuccess()) {
        			log.error(resp.getHttpContentString());
        		} else if (resp.getStatus() != 200) {
        			log.warn(resp.getHttpContentString());
        		} else {
        			log.info(resp.getHttpContentString());
        		}
        		// Throttling.User
        		// https://error-center.aliyun.com/status/search?Keyword=Throttling.User&source=PopGw
        		// 北京、上海、杭州地域QPS限制为200，张家口、深圳地域QPS限制为100，其余地域QPS限制为50
        		Thread.sleep(100L);
			}
        }
    }
    
    /* (non-Javadoc)
	 * @see com.jinguduo.spider.common.metric.MetricReportor#start()
	 */
    @Override
	public void start() {
    	if (isStarted.compareAndSet(false, true)) {
    		timer.schedule(new Sender(), period, period);
		}
    }
    
    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(accessKey, "The accessKey maybe null");
        Assert.notNull(accessSecret, "The accessSecret maybe null");
        Assert.notNull(region, "The region maybe null");
        Assert.notNull(endpoint, "The endpoint maybe null");
        
        objectMapper = new ObjectMapper();
    	objectMapper.addMixIn(AliyunMetricBean.class, MetricBeanIgnore.class);
        
        IClientProfile profile = DefaultProfile.getProfile(region, accessKey, accessSecret);
        acsClient = new DefaultAcsClient(profile);
        
        start();
    }
    
    abstract class MetricBeanIgnore  {
    	@JsonIgnore abstract String getNamespace();
    	@JsonIgnore abstract int getValue();
    	@JsonProperty("time") abstract long getTimestamp();
    }
    
    private static HashFunction crc32 = Hashing.crc32();
    
    static class AliyunMetricBean extends MetricBean {
    	
    	AliyunMetricBean(String namespace, String metricName, int type, 
    			int value, Map<String, String> dimensions, long timestamp) {
			super(namespace, metricName, type, value, dimensions, timestamp);
		}
    	
    	@JsonIgnore
    	public static AliyunMetricBean of(MetricBean o) {
    		return new AliyunMetricBean(o.getNamespace(), o.getMetricName(), o.getType(),
    				o.getValue(), o.getDimensions(), o.getTimestamp());
    	}
    	
    	public Map<String, Integer> getValues() {
    		return ImmutableMap.of("value", this.getValue());
    	}

		public int getGroupId() {
    		return Math.abs(crc32.newHasher()
    				.putString(this.getNamespace(), StandardCharsets.UTF_8)
    				.hash()
    				.asInt());
    	}
	}
    
    protected class Sender extends TimerTask {
        private AsyncTask async = new AsyncTask(2);
        
        @Override
        public void run() {
            try {
            	async.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            send();
                        } catch (Exception e) {
                            log.error(e.getMessage(), e);
                        }
                    }
                });
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

	public void setAccessKey(String accessKey) {
		this.accessKey = accessKey;
	}

	public void setAccessSecret(String accessSecret) {
		this.accessSecret = accessSecret;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}
}
