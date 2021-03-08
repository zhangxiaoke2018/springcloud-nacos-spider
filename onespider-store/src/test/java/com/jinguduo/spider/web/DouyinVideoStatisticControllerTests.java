package com.jinguduo.spider.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.Assert;
import org.springframework.web.context.WebApplicationContext;

import com.alibaba.fastjson.JSON;
import com.jinguduo.spider.data.table.DouyinVideoStatistic;


@RunWith(SpringRunner.class)
@SpringBootTest
@WebAppConfiguration
@ActiveProfiles("test")
public class DouyinVideoStatisticControllerTests {

	private MockMvc mockMvc;

    @Autowired
    private DouyinVideoStatisticController douyinVideoStatisticController;

    @Resource
    protected WebApplicationContext webApplicationContext;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void testContext() {
        Assert.notNull(douyinVideoStatisticController, "Bad");
    }
    
    @Test
    public void testSaveList() throws Exception {
    	MockHttpServletRequestBuilder req = post("/douyin/video_statistics");

    	List<DouyinVideoStatistic> statistics = new ArrayList<>();
    	for (int i = 0; i < 20; i++) {
    		DouyinVideoStatistic dvs = new DouyinVideoStatistic();
    		dvs.setAwemeId(1234564524247890L + i);
    		dvs.setMid(7675618247666544L + i);
    		dvs.setDiggCount(123);
    		dvs.setCommentCount(3);
    		statistics.add(dvs);
		}
    	req.content(JSON.toJSONString(statistics));
		req.contentType(MediaType.APPLICATION_JSON);
		
		ResultActions result = mockMvc.perform(req);
		result.andExpect(status().isOk());
    }
}
