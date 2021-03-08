package com.jinguduo.spider.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.Assert;
import org.springframework.web.context.WebApplicationContext;

import com.jinguduo.spider.common.constant.WorkerCommand;
import com.jinguduo.spider.worker.Worker;
import com.jinguduo.spider.worker.WorkerManager;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class WorkerControllerTests {

	private MockMvc mockMvc;

	@Mock
	private WorkerManager workerManager;
	
	@Autowired
	@InjectMocks
	private WorkerController workerController;

	@Autowired
	protected WebApplicationContext webApplicationContext;

	@Before
	public void setUp() {
	    MockitoAnnotations.initMocks(this);
		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
	}

	@Test
	public void testContext() {
		Assert.notNull(workerController);
	}

	@Test
	public void testPost() throws Exception {
	    String uuid = UUID.randomUUID().toString();
	    String domain = "www.WorkerControllerTests.com";
	    String hostname = "worker2";
	    
		MockHttpServletRequestBuilder req = post("/workers");
		req.param("uuid", uuid);
		req.param("command", WorkerCommand.Terminate.toString());
		
		Worker worker = new Worker(hostname, uuid, domain);
		
		Mockito.when(workerManager.getActivedWorkerByUuid(uuid)).thenReturn(worker);
		
		
		ResultActions result = mockMvc.perform(req);
		result.andExpect(status().isOk());
	}
}
