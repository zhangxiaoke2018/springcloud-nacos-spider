package com.jinguduo.spider.cluster.spider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.jinguduo.spider.cluster.spider.RemoteSpiderSettingLoader;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.Spider;
import com.jinguduo.spider.cluster.spider.SpiderSettingLoader;
import com.jinguduo.spider.data.table.SpiderSetting;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class RemoteSpiderSettingLoaderTests {

	@Mock
	private RestTemplate restTemplate;
	
	@Value("${onespider.store.spider_setting.url}")
	private String spiderSettingUrl;
	
	@Mock
	private Spider spider;
	
	@Mock
	private Site site;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		
		Mockito.when(spider.getSite()).thenReturn(site);
		Mockito.when(site.getDomain()).thenReturn("www.RemoteSpiderSettingLoaderTests.com");
	}
	
	@Test
	public void testLoad() throws Exception {
		String query = UriComponentsBuilder.fromHttpUrl(spiderSettingUrl).queryParam("domain", site.getDomain()).build().toUriString();
		
		Mockito.when(restTemplate.getForObject(query, SpiderSetting.class)).thenReturn(new SpiderSetting());
		
		SpiderSettingLoader loader = new RemoteSpiderSettingLoader(restTemplate, spiderSettingUrl);
		SpiderSetting setting = loader.load(spider);
		
		Assert.notNull(setting);
	}
}
