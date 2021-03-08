package com.jinguduo.spider.spider.iqiyi;

import java.util.List;

import org.apache.http.client.config.CookieSpecs;

import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.constant.CommonEnum.BannerType;
import com.jinguduo.spider.common.constant.CommonEnum.Platform;
import com.jinguduo.spider.common.util.Md5Util;
import com.jinguduo.spider.webmagic.Page;

@Worker
public class IqiyiMobileSpider extends CrawlSpider {

	private Site site = SiteBuilder.builder().setDomain("m.iqiyi.com").setCookieSpecs(CookieSpecs.IGNORE_COOKIES)
			.addHeader("Connection", "keep-alive")
			.setUserAgent("Mozilla/5.0 (iPhone; CPU iPhone OS 11_0 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/11.0 Mobile/15A372 Safari/604.1")
			.build();

	private PageRule rules = PageRule.build().add("#MOBILE_HOME_BANNER", page -> processMain(page))
			.add("#MOBILE_CHANNEL_BANNER", page -> processMain(page));

	private void processMain(Page page) {
		String regex = "#(" + BannerType.MOBILE_CHANNEL_BANNER + "|" + BannerType.MOBILE_HOME_BANNER.name() + ")$";
		String bannerName = page.getUrl().regex(regex, 1).get();
		BannerType bannerType = BannerType.valueOf(bannerName);

		if (bannerType == BannerType.MOBILE_HOME_BANNER) {
			List<String> bannerLinks = page.getHtml().xpath("//ul[@class='list-carousel'][1]/li/a/@href").all();
			if (null != bannerLinks)
				bannerLinks.forEach(l -> createUrlJob(page, fixBannerUrl(l, bannerType)));
		}

		List<String> recommendLinks = page.getHtml().xpath("//ul[@class='m-pic-list m-sliding-list'][1]/li/div[@class='piclist-img']/a/@href")
				.all();
		if (null != recommendLinks) {
			int i = 0;
			if (bannerType != BannerType.MOBILE_HOME_BANNER) {
				createUrlJob(page, fixBannerUrl(recommendLinks.get(i), BannerType.MOBILE_CHANNEL_BANNER));
				i += 1;
			}

			for (int j = i; j < Math.min(i + 4, recommendLinks.size()); j++) {
				createUrlJob(page,
						fixBannerUrl(recommendLinks.get(j),
								bannerType == BannerType.MOBILE_HOME_BANNER ? BannerType.MOBILE_HOME_RECOMMEND
										: BannerType.MOBILE_CHANNEL_RECOMMEND));
			}
		}

	}

	private String fixBannerUrl(String url, BannerType bannerType) {
		url = url.replace("m.iqiyi.com", "www.iqiyi.com");
		url = !url.startsWith("https:") && !url.startsWith("http:") ? "https:" + url : url;
		int questionMarkIdx = url.indexOf("?");
		if (questionMarkIdx > -1)
			url = url.substring(0, questionMarkIdx);
		url = url + "#" + bannerType.name();
		return url;
	}

	private void createUrlJob(Page page, String url) {
		Job job = new Job(url);
		job.setPlatformId(Platform.I_QI_YI.getCode());
		job.setCode(Md5Util.getMd5(url));
		putModel(page, job);
	}

	@Override
	public Site getSite() {
		// TODO Auto-generated method stub
		return site;
	}

	@Override
	public PageRule getPageRule() {
		// TODO Auto-generated method stub
		return rules;
	}

}
