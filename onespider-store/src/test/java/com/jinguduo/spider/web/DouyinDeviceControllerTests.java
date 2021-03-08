package com.jinguduo.spider.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang3.RandomStringUtils;
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
import com.jinguduo.spider.data.table.DouyinDevice;


@RunWith(SpringRunner.class)
@SpringBootTest
@WebAppConfiguration
@ActiveProfiles("test")
public class DouyinDeviceControllerTests {

	private MockMvc mockMvc;

    @Autowired
    private DouyinDeviceController douyinDeviceController;

    @Resource
    protected WebApplicationContext webApplicationContext;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void testContext() {
        Assert.notNull(douyinDeviceController, "Bad");
    }
    
    @Test
    public void testSaveList() throws Exception {
    	MockHttpServletRequestBuilder req = post("/douyin/devices");

    	List<DouyinDevice> devices = new ArrayList<>();
    	for (int i = 0; i < 4; i++) {
    		DouyinDevice douyinDevice = new DouyinDevice();
    		douyinDevice.setDeviceId(RandomStringUtils.randomNumeric(15));
    		douyinDevice.setInstallId(RandomStringUtils.randomNumeric(15));
    		douyinDevice.setOpenudid(RandomStringUtils.randomNumeric(15));
    		devices.add(douyinDevice);
		}
    	String s = JSON.toJSONString(devices);
    	req.content(s);
		req.contentType(MediaType.APPLICATION_JSON);
		
		ResultActions result = mockMvc.perform(req);
		result.andExpect(status().isOk());
    }
    
    @Test
    public void testGetJson() throws Exception {
        MockHttpServletRequestBuilder req = get("/douyin/devices");
        req.param("page", "0");
        req.contentType(MediaType.APPLICATION_JSON);

        ResultActions result = mockMvc.perform(req);
        result.andExpect(status().isOk());
        result.andExpect(content().contentType("application/json;charset=UTF-8"));
        result.andExpect(jsonPath("$.*").isArray());
    }
}
