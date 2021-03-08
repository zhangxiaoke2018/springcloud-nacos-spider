package com.jinguduo.spider.cluster.model;

import java.io.IOException;

import org.junit.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.Assert;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.jinguduo.spider.webmagic.model.HttpRequestBody;

@ActiveProfiles("test")
public class JobTests {

	@Test
    public void testGetJsonify() throws IOException {
    	Job job = new Job("https://m.manmanapp.com/site/get-category-info.html");
        job.setCode("-");
        
        ObjectMapper objectMapper = new ObjectMapper();
        String s = objectMapper.writeValueAsString(job);
        
        Job newJob = objectMapper.readValue(s, Job.class);
        
        Assert.isTrue(job.getMethod().equalsIgnoreCase(newJob.getMethod()), "method");
        Assert.isNull(job.getHttpRequestBody(), "body");
        Assert.isTrue(job.getCode().equals(newJob.getCode()), newJob.getCode());
        Assert.isTrue(job.getHost().equals(newJob.getHost()), newJob.getHost());
        Assert.isTrue(job.getUrl().equals(newJob.getUrl()), newJob.getUrl());
    }
	
	@Test
    public void testPostBodyJsonify() throws IOException {
    	Job job = new Job("https://m.manmanapp.com/site/get-category-info.html");
        job.setCode("-");
        job.setMethod("POST");
        job.setHttpRequestBody(HttpRequestBody.form(ImmutableMap.of("categoryId", "33"), "UTF-8"));
        
        ObjectMapper objectMapper = new ObjectMapper();
        String s = objectMapper.writeValueAsString(job);
        
        Job newJob = objectMapper.readValue(s, Job.class);
        
        Assert.isTrue(job.getMethod().equalsIgnoreCase(newJob.getMethod()), "method");
        Assert.notNull(job.getHttpRequestBody().getBody(), "body");
        Assert.isTrue(job.getHttpRequestBody().getBody().length == newJob.getHttpRequestBody().getBody().length, newJob.getHttpRequestBody().toString());
    }
}
