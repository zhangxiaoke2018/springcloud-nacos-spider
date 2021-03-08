
package com.guduo.dashboard.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.UUID;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;

import com.google.common.collect.Lists;
import com.guduo.dashboard.vo.Worker;
import com.jinguduo.spider.common.constant.WorkerCommand;
import com.jinguduo.spider.common.util.Paginator;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 16/8/3 上午11:40
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class WorkerControllerTests {

    private MockMvc mockMvc;
    
    @Mock
    private RestTemplate restTemplate;

    @Autowired
    @InjectMocks
    private WorkerController workerController;

    @Resource
    protected WebApplicationContext webApplicationContext;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void testContent(){
        Assert.notNull(workerController);
    }

    @Test
    public void testDoList() throws Exception {
        Paginator<Worker> paginator = new Paginator<>(1, 100, 1);
        Worker worker = new Worker();
        worker.setDomain("test.com");
        worker.setUuid("test");
        worker.setCommand(WorkerCommand.Noop);
        paginator.setEntites(Lists.newArrayList(worker));
        ResponseEntity<Paginator<Worker>> resp = ResponseEntity.ok(paginator);
        
        Mockito.when(restTemplate.exchange(
                Matchers.anyString(), 
                Matchers.eq(HttpMethod.GET), 
                Matchers.<HttpEntity<?>> any(), 
                Matchers.<ParameterizedTypeReference<Paginator<Worker>>> any()))
            .thenReturn(resp);
        
        MockHttpServletRequestBuilder builder = get("/workers")
                .param("domain", "test.com");

        ResultActions result = mockMvc.perform(builder);
        result.andExpect(status().isOk());
        result.andExpect(view().name("workers"));
        result.andExpect(model().attributeExists("paginator"));
    }

    @Test
    public void testDoPost() throws Exception {
        String uuid = UUID.randomUUID().toString();
        String domain = "www.WorkerControllerTests.com";
        String hostname = "worker2";
        final WorkerCommand command = WorkerCommand.Terminate;
        
        Worker worker = new Worker();
        worker.setHostname(hostname);
        worker.setDomain(domain);
        worker.setCommand(command);
        worker.setUuid(uuid);
        
        Mockito.when(restTemplate.exchange(
                Matchers.anyString(), 
                Matchers.eq(HttpMethod.POST), 
                Matchers.<HttpEntity<?>> any(), 
                Matchers.<ParameterizedTypeReference<Worker>> any())
        ).thenReturn(ResponseEntity.ok(worker));
        
        MockHttpServletRequestBuilder req = post("/workers");
        req.param("uuid", uuid);
        req.param("command", command.toString());

        ResultActions result = mockMvc.perform(req);
        result.andExpect(status().isOk());
    }

}
