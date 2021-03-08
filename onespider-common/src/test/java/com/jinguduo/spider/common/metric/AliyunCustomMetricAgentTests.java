package com.jinguduo.spider.common.metric;

import java.util.Random;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
public class AliyunCustomMetricAgentTests {

    @Ignore("for debug")
    @Test
    public void testSend() throws Exception {
        
        Random random = new Random();
        
        for (int i = 0; i < 10; i++) {
            MetricFactory.builder()
            		.namespace("onespider_test")
                    .metricName("page_count")
                    .addDimension("Host", "test" + i)
                    .addDimension("Domain", "www.test.com")
                    .build().addAndGet(random.nextInt(200000));
        }
        
        for (int i = 0; i < 10; i++) {
            MetricFactory.builder()
            		.namespace("onespider_test")
                    .metricName("success")
                    .addDimension("Host", "test" + i)
                    .addDimension("Domain", "www.test.com")
                    .build().addAndGet(random.nextInt(200));
            
            MetricFactory.builder()
            		.namespace("onespider_test")
                    .metricName("exception")
                    .addDimension("Host", "test" + i)
                    .addDimension("Domain", "www.test.com")
                    .build().addAndGet(random.nextInt(200));
        }
        
        AliyunCustomMetricReportor agent = new AliyunCustomMetricReportor();
        agent.setAccessKey("IYxlrWnrkPFZTDDA");
        agent.setAccessSecret("i0RiU5p7HIH7IrQ8gcH1SlxDcPMgJF");
        agent.setRegion("cn-qingdao");
        agent.setEndpoint("metrics.aliyuncs.com");
        
        agent.afterPropertiesSet();
        agent.send();
    }
}
