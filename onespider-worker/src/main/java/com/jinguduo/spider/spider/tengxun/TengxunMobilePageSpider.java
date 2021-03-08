package com.jinguduo.spider.spider.tengxun;

import java.util.List;

import org.apache.http.client.config.CookieSpecs;

import com.aliyuncs.utils.StringUtils;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.spider.listener.UserAgentSpiderListener;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.constant.CommonEnum.BannerType;
import com.jinguduo.spider.common.constant.CommonEnum.Platform;
import com.jinguduo.spider.common.util.RegexUtil;
import com.jinguduo.spider.data.table.BannerRecommendation;

@Worker
public class TengxunMobilePageSpider extends CrawlSpider{
	private Site site = SiteBuilder.builder()
            .setDomain("m.v.qq.com")
            .setCookieSpecs(CookieSpecs.IGNORE_COOKIES)
            .addSpiderListener(new UserAgentSpiderListener())
            .build();
	
	
	PageRule rule = PageRule.build()
            .add("/index.html#MOBILE_HOME_BANNER", this::processBanner)
            .add("/x/m/channel/figure/tv#MOBILE_CHANNEL_BANNER", this::processBanner);
	
	private void processBanner(Page page) {
    	BannerType bannerType = BannerType.valueOf(page.getUrl().regex(".*#(MOBILE_HOME_BANNER|MOBILE_CHANNEL_BANNER)$").get());
		List<String> keys = page.getHtml().xpath("//div[@class='swiper-container simple-horizontal']//div[@class='swiper-slide']/@key").all();
		if(null!=keys)keys.forEach(albumId->{
			if(!StringUtils.isEmpty(RegexUtil.getDataByRegex(albumId,"([a-z0-9]{15})"))){
				putModel(page,new BannerRecommendation(albumId,Platform.TENG_XUN.getCode(),bannerType));
			}
		});
	}
	
	@Override
	public Site getSite() {
		// TODO Auto-generated method stub
		return site;
	}

	@Override
	public PageRule getPageRule() {
		// TODO Auto-generated method stub
		return rule;
	}

}
