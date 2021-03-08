package com.jinguduo.spider.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.Assert;
import org.springframework.web.context.WebApplicationContext;

import com.alibaba.fastjson.JSON;
import com.jinguduo.spider.cluster.model.Job;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class JobSyncControllerTests {

	private MockMvc mockMvc;

	@Resource
	private JobSyncController jobSyncController;

	@Resource
	protected WebApplicationContext webApplicationContext;

	@Before
	public void setUp() {
		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
	}

	@Test
	public void testContext() {
		Assert.notNull(jobSyncController);
	}

	@Test
	public void testPost() throws Exception {
		MockHttpServletRequestBuilder req = post("/jobs/sync");
		req.param("uuid", UUID.randomUUID().toString());
		req.param("version", "0");
		
		Job job = new Job();
		job.setMethod("GET");
		job.setUrl("http://www.JobSyncControllerTests.com/");
		req.content(JSON.toJSONString(new Job[]{ job }));
		req.contentType(MediaType.APPLICATION_JSON);
		
		ResultActions result = mockMvc.perform(req);
		result.andExpect(status().isOk());
	}
}
