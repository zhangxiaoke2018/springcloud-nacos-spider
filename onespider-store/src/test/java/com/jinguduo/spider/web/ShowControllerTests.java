package com.jinguduo.spider.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.sql.Date;
import java.util.UUID;

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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.Assert;
import org.springframework.web.context.WebApplicationContext;

import com.alibaba.fastjson.JSON;
import com.jinguduo.spider.StoreMainApplication;
import com.jinguduo.spider.data.table.Show;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 16/6/17 下午3:48
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@WebAppConfiguration
@ActiveProfiles("test")
public class ShowControllerTests {

    private MockMvc mockMvc;

    @Autowired
    private ShowController showController;

    @Resource
    protected WebApplicationContext webApplicationContext;

    private final String title = "你正常吗 第3季 - 高清在线观看 - 腾讯视频";

    @Before
    public void setUp() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        MockHttpServletRequestBuilder requestBuilder = post("/show");
        Show show = new Show();
        show.setName("test show name");
        //show.setParentId(1);
        show.setCode(UUID.randomUUID().toString());
        show.setReleaseDate(new Date(System.currentTimeMillis()));

        requestBuilder.content(JSON.toJSONString(show));
        requestBuilder.contentType(MediaType.APPLICATION_JSON);

        //ResultActions perform = mockMvc.perform(requestBuilder);
    }

    @Test
    public void testContext() {
        Assert.notNull(showController);
    }

    @Test
    public void testGetByTitle() throws Exception {

        MockHttpServletRequestBuilder requestBuilder = get("/shows");
        requestBuilder.param("title",title);

        ResultActions perform = mockMvc.perform(requestBuilder);
        perform.andExpect(status().isOk());
        MvcResult mvcResult = perform.andReturn();
        Assert.notNull(mvcResult);

    }

    @Test
    public void testAddShow() throws Exception {
        MockHttpServletRequestBuilder requestBuilder = post("/show");
        Show show = new Show();
        show.setName("test show name");
        show.setCategory("test");
        show.setCode(UUID.randomUUID().toString());
        show.setReleaseDate(new Date(System.currentTimeMillis()));

        requestBuilder.content(JSON.toJSONString(show));
        requestBuilder.contentType(MediaType.APPLICATION_JSON);

        ResultActions perform = mockMvc.perform(requestBuilder);
        perform.andExpect(status().isOk());
    }
    @Test
    public void testShowPage() throws Exception {

        MockHttpServletRequestBuilder requestBuilder = get("/show/list");

        requestBuilder.param("page","1");
        requestBuilder.param("size","2");

        ResultActions actions = mockMvc.perform(requestBuilder);

        actions.andExpect(status().isOk());


    }
}
