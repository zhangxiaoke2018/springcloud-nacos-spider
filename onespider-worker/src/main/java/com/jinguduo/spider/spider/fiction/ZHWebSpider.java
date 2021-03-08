package com.jinguduo.spider.spider.fiction;

import org.jsoup.select.Elements;

import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.constant.CommonEnum.Platform;
import com.jinguduo.spider.data.table.FictionChapters;
import com.jinguduo.spider.webmagic.Page;

import lombok.extern.apachecommons.CommonsLog;

@Worker
@CommonsLog
public class ZHWebSpider  extends CrawlSpider {
	private Site site = SiteBuilder.builder().setDomain("book.zongheng.com").build();

	private PageRule rules = PageRule.build()
			.add("/showchapter", this::processChapter);
	
	private void processChapter(Page page) {
		String code = page.getUrl().regex(".*\\/showchapter\\/(\\d+)\\.html", 1).get();
		Elements chapters = page.getHtml().getDocument().getElementsByAttributeValueContaining("class", "col-4");
		int totalChapterCount = chapters.size();
		chapters = page.getHtml().getDocument().getElementsByAttributeValueContaining("class", "vip col-4");
		int vipChapterCount = chapters.size();
		FictionChapters chapter = new FictionChapters();
		chapter.setCode(code);
		chapter.setPlatformId(Platform.ZONG_HENG.getCode());
		chapter.setFreeChapterCount(totalChapterCount-vipChapterCount);
		chapter.setIsVip(vipChapterCount>0 ? 1:0);
		chapter.setTotalChapterCount(totalChapterCount);
		putModel(page,chapter);
	}
	
	public Site getSite() {
		return site;
	}
	
	public PageRule getPageRule() {
		return rules;
	}
}
