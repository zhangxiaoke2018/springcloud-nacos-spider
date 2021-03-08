package com.jinguduo.spider.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
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
import com.jinguduo.spider.data.table.DouyinHotSearch;


@RunWith(SpringRunner.class)
@SpringBootTest
@WebAppConfiguration
@ActiveProfiles("test")
public class DouyinHotSearchControllerTests {

	private MockMvc mockMvc;

    @Autowired
    private DouyinHotSearchController douyinHotSearchController;

    @Resource
    protected WebApplicationContext webApplicationContext;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void testContext() {
        Assert.notNull(douyinHotSearchController, "Bad");
    }
    
    @Test
    public void testSaveList() throws Exception {
    	MockHttpServletRequestBuilder req = post("/douyin/hot_searches");

    	List<DouyinHotSearch> searches = new ArrayList<>();
    	for (int i = 1; i <= 30; i++) {
    		DouyinHotSearch search = new DouyinHotSearch();
    		search.setActiveTime("8月20日 20:00");
    		search.setOrdinal(i);
    		search.setHotValue((RandomUtils.nextInt() * 100000) / i);
    		search.setWord(RandomStringUtils.random(12));
    		searches.add(search);
		}
    	String s = JSON.toJSONString(searches);
    	req.content(s);
		req.contentType(MediaType.APPLICATION_JSON);
		
		ResultActions result = mockMvc.perform(req);
		result.andExpect(status().isOk());
    }
}
