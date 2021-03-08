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
import com.jinguduo.spider.data.table.DouyinAuthor;


@RunWith(SpringRunner.class)
@SpringBootTest
@WebAppConfiguration
@ActiveProfiles("test")
public class DouyinAuthorControllerTests {

	private MockMvc mockMvc;

    @Autowired
    private DouyinAuthorController douyinAuthorController;

    @Resource
    protected WebApplicationContext webApplicationContext;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void testContext() {
        Assert.notNull(douyinAuthorController, "Bad");
    }
    
    @Test
    public void testSaveList() throws Exception {
    	MockHttpServletRequestBuilder req = post("/douyin/authors");

    	List<DouyinAuthor> authors = new ArrayList<>();
    	for (int i = 0; i < 4; i++) {
    		DouyinAuthor douyinAuthor = new DouyinAuthor();
    		douyinAuthor.setUserId(123123131L);
    		douyinAuthor.setNickname("中文");
    		douyinAuthor.setSignature("sdjflsajfsbbab哈哈哈哈哈哈哈");
    		authors.add(douyinAuthor);
		}
    	String s = JSON.toJSONString(authors);
    	req.content(s);
		req.contentType(MediaType.APPLICATION_JSON);
		
		ResultActions result = mockMvc.perform(req);
		result.andExpect(status().isOk());
    }
}
