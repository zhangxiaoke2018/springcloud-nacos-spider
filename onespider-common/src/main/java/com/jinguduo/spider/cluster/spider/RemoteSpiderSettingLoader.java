package com.jinguduo.spider.cluster.spider;

import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.data.table.SpiderSetting;

import lombok.extern.apachecommons.CommonsLog;

@CommonsLog
public class RemoteSpiderSettingLoader implements SpiderSettingLoader {
	
	private RestTemplate restTemplate;
	private String url;

	public RemoteSpiderSettingLoader(RestTemplate restTemplate, String url) {
		this.restTemplate = restTemplate;
		this.url = url;
	}

	@Override
	public SpiderSetting load(Spider spider) {
		Site site = spider.getSite();
		assert site != null;
		
		SpiderSetting setting = null;
		try {
		    String query = UriComponentsBuilder.fromHttpUrl(url).queryParam("domain", site.getDomain()).build().toUriString();
		    setting = restTemplate.getForObject(query, SpiderSetting.class);
        } catch (Exception e) {
            log.error(site.getDomain(), e);
        }
		if (setting == null) {
            setting = new SpiderSetting();
        }
		return setting;
	}

}
