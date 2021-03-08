package com.jinguduo.spider.common.metric;

import java.util.*;
import java.util.stream.Collectors;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.ImmutableList;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.monitor.v20180724.MonitorClient;
import com.tencentcloudapi.monitor.v20180724.models.PutMonitorDataRequest;
import com.tencentcloudapi.monitor.v20180724.models.PutMonitorDataResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;

@ActiveProfiles("test")
@Slf4j
public class TencentCloudCustomMetricAgentTests {

	@Ignore("for debug")
    @Test
    public void testSend() throws Exception {
        
        Random random = new Random();
        for (int i = 0; i < 1000; i++) {
        	MetricFactory.builder()
        	.namespace("onespider_test")
        	.metricName("test_count")
        	.addDimension("host", "test" + i)
        	.addDimension("domain", "www.test.com")
        	.build().addAndGet(random.nextInt(20000));
		}
        
        TencentCloudCustomMetricReportor agent = new TencentCloudCustomMetricReportor(new RestTemplate());
        agent.setSecretId("AKIDoW3OYXIwRmvtgLiFyZ0FH7kCXgdO2KVT");
        agent.setSecretKey("88wZ0GPfnjT2PacS5WlndIm7HF2unV5V");
        agent.setRegion("bj");
        agent.setUrl("http://receiver.monitor.tencentyun.com:8080/v2/index.php");
        
        agent.afterPropertiesSet();
        agent.send();
    }
    
    @Test
    public void testSign() throws Exception {
    	TencentCloudCustomMetricReportor agent = new TencentCloudCustomMetricReportor(new RestTemplate());
        agent.setSecretId("AKIDz8krbsJ5yKBZQpn74WFkmLPx3gnPhESA");
        agent.setSecretKey("Gu5t9xGARNpq86cd98joQYCN3Cozk1qA");
        //agent.setNamespace("cvm");
        agent.setRegion("gz");
        agent.setUrl("http://cvm.api.qcloud.com/v2/index.php");
        agent.afterPropertiesSet();
        
        SortedMap<String, Object> params = new TreeMap<String, Object>();
        
        // Signature start...
        params.put("Action", "DescribeInstances");
        params.put("SecretId", "AKIDz8krbsJ5yKBZQpn74WFkmLPx3gnPhESA");
        params.put("Timestamp", 1465185768);
        params.put("Nonce", 11886);
        params.put("Region", "gz");
        //
        params.put("instanceIds.0", "ins-09dx96dg");
        params.put("limit", "20");
        params.put("offset", "0");
        //
        String sign = agent.sign("GET", params);
        
        Assert.isTrue("NSI3UqqD99b/UJb4tbG/xZpRW64=".equals(sign), "Sign: " + sign);
    }


    @Test
    public void testNewSend() throws  Exception{

        Random random = new Random();
        for (int i = 0; i < 1000; i++) {
            MetricFactory.builder()
                    .namespace("onespider_test")
                    .metricName("test_count")
                    .addDimension("host", "test" + i)
                    .addDimension("domain", "www.test.com")
                    .build().addAndGet(random.nextInt(20000));
        }

        Credential cred = new Credential("AKIDymVyKY1FuPMMdmoY1cO3d2oRtHYjRldk", "874rUzNeGqLm6GJG5CD3S0Rhs00pSx9E");
        HttpProfile httpProfile = new HttpProfile();
        httpProfile.setEndpoint("monitor.tencentcloudapi.com");
        ClientProfile clientProfile = new ClientProfile();
        clientProfile.setHttpProfile(httpProfile);
        MonitorClient client = new MonitorClient(cred, "ap-beijing", clientProfile);
        Collection<MetricBean> metrics = MetricFactory.getAndResetMetrics();
        if (metrics != null && !metrics.isEmpty()) {
            // group by namespace
            for (MetricBean b:metrics) {
                int announceTimestamp = (int) (System.currentTimeMillis() / 1000L);
                String metricName = b.getMetricName();
                Integer value = b.getValue();
                String announceInstance = b.getDimensions().get("host");
                String params = "{\"AnnounceTimestamp\":"+announceTimestamp+"," +
                                "\"Metrics\":[{\"MetricName\":\""+metricName+"\"," +
                                "\"Value\":"+value+"}],\"AnnounceInstance\":\""+announceInstance+"\"}";
                PutMonitorDataRequest req = PutMonitorDataRequest.fromJsonString(params, PutMonitorDataRequest.class);
                PutMonitorDataResponse resp = client.PutMonitorData(req);
                log.info("The TencentCloud CVM metric:" + b.getMetricName()+" Host :"+b.getDimensions().get("host")+
                            " ! RequestId : " +resp.getRequestId() );
                System.out.println(PutMonitorDataRequest.toJsonString(resp));
            }


        }








    }
}
