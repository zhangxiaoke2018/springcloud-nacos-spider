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
import com.jinguduo.spider.data.table.DouyinMusic;


@RunWith(SpringRunner.class)
@SpringBootTest
@WebAppConfiguration
@ActiveProfiles("test")
public class DouyinMusicControllerTests {

	private MockMvc mockMvc;

    @Autowired
    private DouyinMusicController douyinMusicController;

    @Resource
    protected WebApplicationContext webApplicationContext;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void testContext() {
        Assert.notNull(douyinMusicController, "Bad");
    }
    
    @Test
    public void testSaveList() throws Exception {
    	MockHttpServletRequestBuilder req = post("/douyin/musics");

    	List<DouyinMusic> musics = new ArrayList<>();
    	for (int i = 0; i < 4; i++) {
    		DouyinMusic douyinMusic = new DouyinMusic();
    		douyinMusic.setAuthor("xp");
    		douyinMusic.setMid(24124312312L);
    		douyinMusic.setTitle("Test .. -Test");
    		musics.add(douyinMusic);
		}
    	String s = JSON.toJSONString(musics);
    	req.content(s);
		req.contentType(MediaType.APPLICATION_JSON);
		
		ResultActions result = mockMvc.perform(req);
		result.andExpect(status().isOk());
    }
}
