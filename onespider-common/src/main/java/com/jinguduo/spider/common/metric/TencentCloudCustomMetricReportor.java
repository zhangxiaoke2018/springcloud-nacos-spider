package com.jinguduo.spider.common.metric;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.monitor.v20180724.MonitorClient;
import com.tencentcloudapi.monitor.v20180724.models.PutMonitorDataRequest;
import com.tencentcloudapi.monitor.v20180724.models.PutMonitorDataResponse;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.util.Base64Utils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.jinguduo.spider.common.thread.AsyncTask;

import lombok.extern.apachecommons.CommonsLog;

/**
 * 腾讯云自定义监控
 */
@CommonsLog
public class TencentCloudCustomMetricReportor implements InitializingBean, MetricReportor {

    @Value("${onespider.tencent.metric.secret-id}")
    private String secretId;
    
    @Value("${onespider.tencent.metric.secret-key}")
    private String secretKey;
    
    @Value("${onespider.tencent.metric.region}")
    private String region;

    @Value("${onespider.tencent.metric.new.region}")
	private String newRegion;
    
    @Value("${onespider.tencent.metric.url}")
    private String url;

    @Value("${onespider.tencent.metric.new.url}")
	private String newUrl;
    
    private final static String ACTION = "PutMonitorData";
    
    private RestTemplate restTemplate;
    
    private ObjectMapper objectMapper;
    
    private Timer timer = new Timer();
    
    private long period = TimeUnit.MINUTES.toMillis(1);
    
    private final AtomicBoolean isStarted = new AtomicBoolean(false);

    private static String announceInstanceKey = "Host";

	private static String announceIpKey = "Domain" ;
    
    public TencentCloudCustomMetricReportor(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}
    
	protected void send() throws IOException, URISyntaxException, InterruptedException {
        Collection<MetricBean> metrics = MetricFactory.getAndResetMetrics();
        
        if (!StringUtils.hasText(url)) {
            log.error("The url cannot be empty.");
            return;
        }
        
        if (metrics != null && !metrics.isEmpty()) {
        	// group by namespace
        	Map<String, List<MetricBean>> folded = metrics.stream()
        			.collect(Collectors.groupingBy(MetricBean::getNamespace));
        	for (String namespace : folded.keySet()) {
        		List<MetricBean> metricBeans = folded.get(namespace);
        		
        		SortedMap<String, Object> params = new TreeMap<>();
        		// Signature start...
        		params.put("Action", ACTION);
        		params.put("SecretId", secretId);
        		params.put("Timestamp", (int)(System.currentTimeMillis() / 1000L));
        		params.put("Nonce", RandomUtils.nextInt(10000, 99999));
        		params.put("Region", region);
        		// Signature end
        		params.put("Signature", sign("POST", params));
        		params.put("Namespace", namespace);
        		params.put("Data", metricBeans);
        		
        		HttpHeaders headers = new HttpHeaders();
        		headers.setContentType(MediaType.APPLICATION_JSON);
        		headers.setAccept(ImmutableList.of(MediaType.APPLICATION_JSON));
        		HttpEntity<String> req = new HttpEntity<>(objectMapper.writeValueAsString(params), headers);
        		
        		ResponseEntity<String> resp = restTemplate.exchange(url, HttpMethod.POST, req, String.class);
        		if (resp.getStatusCode() == HttpStatus.OK) {
        			// 因为腾讯云返回"application/octet-stream"
        			JSONObject body = JSON.parseObject(resp.getBody());
        			String code = body.getString("code");
        			if (!"0".equals(code)) {
        				log.error("The TencentCloud CVM namespace "+ namespace + 
        						" example " + objectMapper.writeValueAsString(metricBeans.get(0)) +
        						" [" + code + " " + body.getOrDefault("message", "") + "]");
        			}
        		} else {
        			log.error("The TencentCloud CVM http status code " + resp.getStatusCodeValue());
        		}
        		Thread.sleep(1000L);
			}
        }
    }

    protected void newSend() throws IOException, URISyntaxException, InterruptedException{

		if (!StringUtils.hasText(newUrl)) {
			log.error("The newUrl cannot be empty.");
			return;
		}

		Credential cred = new Credential(secretId, secretKey);
		HttpProfile httpProfile = new HttpProfile();
		httpProfile.setEndpoint(newUrl);
		ClientProfile clientProfile = new ClientProfile();
		clientProfile.setHttpProfile(httpProfile);
		MonitorClient client = new MonitorClient(cred, newRegion, clientProfile);
		Collection<MetricBean> metrics = MetricFactory.getAndResetMetrics();

		if (metrics != null && !metrics.isEmpty()) {
			for (MetricBean b:metrics) {
				int announceTimestamp = (int) (System.currentTimeMillis() / 1000L);
				String metricName = b.getMetricName();
				Integer value = b.getValue();
				String announceInstance = b.getDimensions().get(announceInstanceKey);
				String announceIp = b.getDimensions().get(announceIpKey);

				String params = "{\"AnnounceIp\":\""+announceIp+"\"," +
								"\"AnnounceTimestamp\":"+announceTimestamp+"," +
								"\"Metrics\":[{\"MetricName\":\""+metricName+"\",\"Value\":"+value+"}]," +
								"\"AnnounceInstance\":\""+announceInstance+"\"}";

				PutMonitorDataRequest req = PutMonitorDataRequest.fromJsonString(params, PutMonitorDataRequest.class);

				PutMonitorDataResponse resp = null;
				try {
					resp = client.PutMonitorData(req);
				} catch (TencentCloudSDKException e) {
					e.printStackTrace();
					log.error("The TencentCloud CVM metrics upload  Failed !! metricName:" +b.getMetricName()+" Host :"+announceInstance+ "Domain :" + announceIp +
							" ! RequestId : [" +resp.getRequestId()+"]" );
				}
				Thread.sleep(1000L);
			}


		}
	}
    
	private HashFunction sha1;
	
    String sign(String method, SortedMap<String, Object> params) {
    	StringBuffer sb = new StringBuffer();
		for (String key : params.keySet()) {
			if (sb.length() > 0) {
				sb.append("&");
			}
			String v = StringUtils.replace(String.valueOf(params.get(key)), "_", ".");
			sb.append(key).append("=").append(v);
		}
		
		String u = url;
		u = StringUtils.delete(u, "http://");
		u = StringUtils.delete(u, "https://");
		u = StringUtils.delete(u, ":8080");
		
		HashCode hash = sha1.newHasher()
				.putString(method, StandardCharsets.UTF_8)
				.putString(u, StandardCharsets.UTF_8)
				.putString("?", StandardCharsets.UTF_8)
				.putString(sb.toString(), StandardCharsets.UTF_8)
				.hash();
		return Base64Utils.encodeToString(hash.asBytes());
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
    
    void init() {
    	sha1 = Hashing.hmacSha1(secretKey.getBytes());
    	
    	objectMapper = new ObjectMapper();
    	//objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.LOWER_CAMEL_CASE);
    	objectMapper.addMixIn(MetricBean.class, MetricBeanIgnore.class);
    }
    
    /* (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        Assert.notNull(secretId, "The secretId maybe null");
        Assert.notNull(secretKey, "The secretKey maybe null");
        Assert.notNull(url, "The url maybe null");
        Assert.notNull(restTemplate, "The restTemplate maybe null");
        Assert.notNull(newUrl,"The newUrl maybe null");
        Assert.notNull(newRegion,"The newRegion maybe null");

        init();
        
        start();
    }
    
    abstract class MetricBeanIgnore {
    	//@JsonIgnore abstract String getDimensions();
    	@JsonIgnore abstract String getNamespace();
    	@JsonIgnore abstract int getType();
    	@JsonIgnore abstract long getTimestamp();
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
							Thread.sleep(5000L);
                        } catch (Exception e) {
                            log.error(e.getMessage(), e);
                        }
//						try {
//							newSend();
//						} catch (IOException e) {
//							e.printStackTrace();
//						} catch (InterruptedException e) {
//							e.printStackTrace();
//						} catch (URISyntaxException e) {
//							e.printStackTrace();
//						}
					}
                });
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    public void setSecretId(String secretId) {
        this.secretId = secretId;
    }
    
    public void setSecretKey(String secretKey) {
    	this.secretKey = secretKey;
    }
    
    public void setRegion(String region) {
    	this.region = region;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
