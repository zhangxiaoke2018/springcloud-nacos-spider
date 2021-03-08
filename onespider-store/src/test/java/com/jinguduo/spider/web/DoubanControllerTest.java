package com.jinguduo.spider.web;

import com.alibaba.fastjson.JSON;
import com.jinguduo.spider.data.table.DoubanCommentsText;

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
import javax.annotation.Resource;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class DoubanControllerTest {

    private MockMvc mockMvc;

    @Resource
    private DoubanLogsController controller;

    @Resource
    protected WebApplicationContext webApplicationContext;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void testContext() {
        Assert.notNull(controller, "DoubanLogsController is null!");
    }

    @Test
    public void testAddComment() throws Exception {
        MockHttpServletRequestBuilder requestBuilder = post("/douban_log/comment");

        DoubanCommentsText text = new DoubanCommentsText();
        text.setCode("26614082");
        text.setCommentId(1182156292l);
        text.setUp(10);
        text.setType("P");
        text.setStar(5);
        text.setNickName("努力努力再努力y");
        text.setContent("电视剧还是不错的就是有些地方有点假");

        requestBuilder.content(JSON.toJSONString(text));
        requestBuilder.contentType(MediaType.APPLICATION_JSON);

        ResultActions result = mockMvc.perform(requestBuilder);
        result.andExpect(status().isOk());
    }

}
