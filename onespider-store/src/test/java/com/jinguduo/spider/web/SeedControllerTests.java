package com.jinguduo.spider.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import com.jinguduo.spider.StoreMainApplication;
import com.jinguduo.spider.data.table.Seed;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 16/6/17 下午4:19
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@WebAppConfiguration
@ActiveProfiles("test")
public class SeedControllerTests {

    private MockMvc mockMvc;

    @Autowired
    private SeedController seedController;

    @Resource
    protected WebApplicationContext webApplicationContext;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void testContext() {
        Assert.notNull(seedController);
    }

    @Test
    public void testAddSeed() throws Exception {

        MockHttpServletRequestBuilder requestBuilder = post("/seed");

        Seed seed = new Seed();
        seed.setPlatformId(1);
        seed.setPriority(2);
        seed.setCode("SDFlkgsdASLfd");
        seed.setUrl("http://iqiyi.com/23/SDFlkgsdASLfd.html");

        requestBuilder.content(JSON.toJSONString(seed));
        requestBuilder.contentType(MediaType.APPLICATION_JSON);

        ResultActions perform = mockMvc.perform(requestBuilder);
        perform.andExpect(status().isOk());

    }


    @Test
    public void testGetCode() throws Exception {

        MockHttpServletRequestBuilder requestBuilder = get("/seed/code");

        requestBuilder.param("url","http://v.qq.com/detail/4/45584.html");

        ResultActions perform = mockMvc.perform(requestBuilder);
        perform.andExpect(status().isOk());

    }
}
