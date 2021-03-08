package com.jinguduo.spider.cluster.spider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

import com.jinguduo.spider.cluster.spider.DefaultSpiderSettingLoader;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.Spider;
import com.jinguduo.spider.data.table.SpiderSetting;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class DefaultSpiderSettingLoaderTests {

	
	@Mock
	private Spider spider;
	
	@Mock
	private Site site;
	
	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		
		Mockito.when(spider.getSite()).thenReturn(site);
		Mockito.when(site.getDomain()).thenReturn("www.DefaultSpiderSettingLoaderTests.com");
	}
	
	@Test
	public void testLoad() throws Exception {
		DefaultSpiderSettingLoader loader = new DefaultSpiderSettingLoader();
		SpiderSetting setting = loader.load(spider);
		
		Assert.notNull(setting.getSleepTime(), "bad");
	}
	
}
