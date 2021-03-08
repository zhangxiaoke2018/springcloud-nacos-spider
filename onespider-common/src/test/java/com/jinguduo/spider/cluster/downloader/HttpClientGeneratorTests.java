package com.jinguduo.spider.cluster.downloader;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.Assert;

import com.jinguduo.spider.cluster.downloader.HttpClientGenerator;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;

@ActiveProfiles("test")
public class HttpClientGeneratorTests {

	@Test
	public void testGetClient() throws ClientProtocolException, IOException {
		HttpClientGenerator httpClientGenerator = new HttpClientGenerator();
		
		Site site = SiteBuilder.builder().setDomain("www.baidu.com").build();
		
		CloseableHttpClient client = httpClientGenerator.getClient(site);
		
		Assert.notNull(client, "Bad");
		
		//HttpUriRequest req = RequestBuilder.get("https://www.baidu.com/").build();
		//client.execute(req);
	}
}
