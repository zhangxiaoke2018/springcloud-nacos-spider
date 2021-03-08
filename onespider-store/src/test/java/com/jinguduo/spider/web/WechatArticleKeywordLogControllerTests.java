package com.jinguduo.spider.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.sql.Date;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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
import com.jinguduo.spider.StoreMainApplication;
import com.jinguduo.spider.data.table.WechatArticleKeywordLog;


@RunWith(SpringRunner.class)
@SpringBootTest
@WebAppConfiguration
@ActiveProfiles("test")
public class WechatArticleKeywordLogControllerTests {

	private MockMvc mockMvc;

	@Resource
	private WechatArticleKeywordLogController controller;

	@Resource
	protected WebApplicationContext webApplicationContext;

	@Before
	public void setUp() {
		mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
	}

	@Test
	public void testContext() {
		Assert.notNull(controller);
	}

	@Test
	public void testPost() throws Exception {
		MockHttpServletRequestBuilder req = post("/wechat_article_keyword_log");
		
		WechatArticleKeywordLog[] logs = new WechatArticleKeywordLog[10];
		for (int i = 0; i < logs.length; i++) {
		    WechatArticleKeywordLog d = new WechatArticleKeywordLog();
		    d.setKeyword("k" + i);
		    d.setDay(new Date(System.currentTimeMillis()));
		    d.setArticleCount(i + 100);
		    logs[i] =  d;
        }
		
		req.content(JSON.toJSONString(logs));
		req.contentType(MediaType.APPLICATION_JSON);
		
		ResultActions result = mockMvc.perform(req);
		result.andExpect(status().isOk());
	}
}
