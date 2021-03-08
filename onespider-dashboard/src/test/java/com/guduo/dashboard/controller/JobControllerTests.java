
package com.guduo.dashboard.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.ArrayList;
import java.util.Collection;

import javax.annotation.Resource;

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
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;

import com.google.common.collect.Lists;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.model.JobWrapper;
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
public class JobControllerTests {

    private MockMvc mockMvc;
    
    @Mock
    private RestTemplate restTemplate;

    @Autowired
    @InjectMocks
    private JobController jobController;

    @Resource
    protected WebApplicationContext webApplicationContext;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void testContent(){
        Assert.notNull(jobController);
    }

    @Test
    public void testDoList() throws Exception {
        Paginator<JobWrapper> paginator = new Paginator<>(1, 100, 1);
        Job job = new Job();
        job.setId(String.valueOf(1));
        paginator.setEntites(Lists.newArrayList(new JobWrapper(job)));
        Mockito.when(restTemplate.getForObject(Mockito.anyString(), Mockito.eq(Paginator.class))).thenReturn(paginator);
        
        MockHttpServletRequestBuilder builder = get("/jobs");

        ResultActions result = mockMvc.perform(builder);
        result.andExpect(status().isOk());
        result.andExpect(view().name("jobs"));
        result.andExpect(model().attributeExists("paginator"));
    }

    @Test
    public void testDoListByEmpty() throws Exception {
        Paginator<JobWrapper> paginator = new Paginator<>(1, 100, 1);
        Mockito.when(restTemplate.getForObject(Mockito.anyString(), Mockito.eq(Paginator.class))).thenReturn(paginator);
        
        MockHttpServletRequestBuilder builder = get("/jobs");

        ResultActions result = mockMvc.perform(builder);
        result.andExpect(status().isOk());
        result.andExpect(view().name("jobs"));
        result.andExpect(model().attributeExists("paginator"));
    }

    @Test
    public void testDoListByPage() throws Exception {
        Paginator<JobWrapper> paginator = new Paginator<>(2, 100, 2);
        final int size = 20;
        Collection<JobWrapper> jws = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            Job job = new Job();
            job.setId(String.valueOf(i));
            job.setCode("a--" + i);
            jws.add(new JobWrapper(job));
        }
        paginator.setEntites(jws);
        Mockito.when(restTemplate.getForObject(Mockito.anyString(), Mockito.eq(Paginator.class))).thenReturn(paginator);
        
        MockHttpServletRequestBuilder builder = get("/jobs").param("page", "2");

        ResultActions result = mockMvc.perform(builder);
        result.andExpect(status().isOk());
        result.andExpect(view().name("jobs"));
        result.andExpect(model().hasNoErrors());
        result.andExpect(model().attributeExists("paginator"));
        model().attribute("paginator", paginator);
    }

}
