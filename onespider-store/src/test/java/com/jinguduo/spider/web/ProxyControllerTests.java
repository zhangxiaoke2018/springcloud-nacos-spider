package com.jinguduo.spider.web;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
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

import com.google.common.collect.Lists;
import com.jinguduo.spider.common.constant.ProxyState;
import com.jinguduo.spider.data.table.Proxy;
import com.jinguduo.spider.service.ProxyService;

@RunWith(SpringRunner.class)
@SpringBootTest
@WebAppConfiguration
@ActiveProfiles("test")
public class ProxyControllerTests {

    private MockMvc mockMvc;

    @Autowired
    @InjectMocks
    private ProxyController proxyController;
    
    @Mock
    private ProxyService proxyService;

    @Resource
    protected WebApplicationContext webApplicationContext;

    @Before
    public void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        
        List<Proxy> proxies = Lists.newArrayList(Proxy.newHttpProxy(),
                Proxy.newSocks4Proxy(),
                Proxy.newSocks5Proxy());
        
        
        Mockito.when(proxyService.findAllByState(ProxyState.Broken)).thenReturn(proxies);
        Mockito.when(proxyService.findAllByStateAndPtypeIn(ProxyState.Broken, null, 0, 100)).thenReturn(new PageImpl<Proxy>(proxies));
    }

    @Test
    public void testContext() {
        Assert.notNull(proxyController, "bad");
    }

    @Test
    public void testGetPlain() throws Exception {
        MockHttpServletRequestBuilder req = get("/proxies");
        req.param("state", "Broken");
        req.contentType(MediaType.TEXT_PLAIN);

        ResultActions result = mockMvc.perform(req);
        result.andExpect(status().isOk());
        result.andExpect(content().contentType("text/plain;charset=UTF-8"));
    }
    
    @Test
    public void testGetJson() throws Exception {
        MockHttpServletRequestBuilder req = get("/proxies");
        req.param("state", "Broken");
        req.contentType(MediaType.APPLICATION_JSON);

        ResultActions result = mockMvc.perform(req);
        result.andExpect(status().isOk());
        result.andExpect(content().contentType("application/json;charset=UTF-8"));
        result.andExpect(jsonPath("$.*").isArray());
    }
    
    @Test
    public void testGetJson2() throws Exception {
        MockHttpServletRequestBuilder req = get("/proxies");
        req.param("state", "Broken");

        ResultActions result = mockMvc.perform(req);
        result.andExpect(status().isOk());
        result.andExpect(content().contentType("application/json;charset=UTF-8"));
        result.andExpect(jsonPath("$.*").isArray());
    }
}
