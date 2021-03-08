package com.jinguduo.spider.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jinguduo.spider.cluster.model.Job;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class JobControllerTests {

    private MockMvc mockMvc;

    @SuppressWarnings("deprecation")
    @Resource
    private JobController jobController;
    
    private ObjectMapper objectMapper = new ObjectMapper();

    @Resource
    protected WebApplicationContext webApplicationContext;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void testContext() {
        Assert.notNull(jobController);
    }

    @Test
    public void testPost() throws Exception {
        MockHttpServletRequestBuilder req = post("/job2");

        Job job = new Job("http://v.pptv.com/show/tzIZlv5k1BJ181s.html?rcc_src=S1");
        job.setCode("tzIZlv5k1BJ181s");
        req.content(objectMapper.writeValueAsString(new Job[] { job }));
        req.contentType(MediaType.APPLICATION_JSON);

        ResultActions result = mockMvc.perform(req);
        result.andExpect(status().isOk());
    }
}
