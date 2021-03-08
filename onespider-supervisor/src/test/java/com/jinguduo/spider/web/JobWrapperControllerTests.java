package com.jinguduo.spider.web;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
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

import com.google.common.collect.Lists;
import com.jinguduo.spider.cluster.model.JobWrapper;
import com.jinguduo.spider.common.util.Paginator;
import com.jinguduo.spider.job.JobManager;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class JobWrapperControllerTests {

    private MockMvc mockMvc;
    
    @Mock
    private JobManager jobManager;

    @Autowired
    @InjectMocks
    private JobWrapperController jobWrapperController;

    @Autowired
    protected WebApplicationContext webApplicationContext;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }
    
    @Test
    public void testContext() {
        Assert.notNull(jobWrapperController);
    }
    
    @Test
    public void testDoList() throws Exception {
        Paginator<JobWrapper> paginator = new Paginator<>(1, 100, 1);
        paginator.setEntites(Lists.newArrayList(new JobWrapper()));
        Mockito.when(jobManager.getJobsByPaginator(Mockito.anyInt(), Mockito.anyInt())).thenReturn(paginator);
        
        MockHttpServletRequestBuilder builder = get("/job").accept(MediaType.parseMediaType("application/json;charset=UTF-8"));

        ResultActions result = mockMvc.perform(builder);
        result.andExpect(status().isOk());
        result.andExpect(content().contentType("application/json;charset=UTF-8"));
        result.andExpect(jsonPath("$.page", is(paginator.getPage().intValue())));
        result.andExpect(jsonPath("$.size", is(paginator.getSize().intValue())));
        result.andExpect(jsonPath("$.pageCount", is(paginator.getPageCount().intValue())));
        result.andExpect(jsonPath("$.entites", hasSize(1)));
    }
}
