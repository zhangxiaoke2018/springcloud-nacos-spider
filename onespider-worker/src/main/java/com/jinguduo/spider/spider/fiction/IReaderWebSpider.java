package com.jinguduo.spider.spider.fiction;

import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.constant.CommonEnum.Platform;
import com.jinguduo.spider.common.util.NumberHelper;
import com.jinguduo.spider.data.table.FictionPlatformRate;
import com.jinguduo.spider.webmagic.Page;

@Worker
public class IReaderWebSpider extends CrawlSpider {
	private Site site = new SiteBuilder()
			.setDomain("www.ireader.com")
			.addHeader("Referer","http://www.ireader.com/")
			.addHeader("User-Agent","Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/73.0.3683.86 Safari/537.36")
			.build();

	private PageRule rule = PageRule.build().add("/index", this::processRate);

	private void processRate(Page page) {
		String code = page.getUrl().regex(".*&bid=(\\d+)").get();
		String scoreStr = page.getHtml().xpath("//div[@class='bookinf01']/div[@class='bookName']/span/text()").get();
		String userCount = page.getHtml().xpath("//span[@class='manyMan']/text()").regex("^(.*)人评分$").get();
		FictionPlatformRate rate = new FictionPlatformRate();
		rate.setCode(code);
		rate.setPlatformId(Platform.IREADER.getCode());
		rate.setRate(Float.valueOf(scoreStr));
		rate.setUserCount(NumberHelper.parseInt(userCount,0));
		putModel(page,rate);
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
