package com.jinguduo.spider.spider.fiction;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.config.CookieSpecs;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.alibaba.fastjson.JSONObject;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.constant.CommonEnum.FictionChannel;
import com.jinguduo.spider.common.constant.CommonEnum.FictionIncome;
import com.jinguduo.spider.common.constant.CommonEnum.Platform;
import com.jinguduo.spider.common.util.Md5Util;
import com.jinguduo.spider.data.table.Fiction;
import com.jinguduo.spider.data.table.FictionCommentLogs;
import com.jinguduo.spider.data.table.FictionIncomeLogs;
import com.jinguduo.spider.webmagic.Page;

import lombok.extern.apachecommons.CommonsLog;

@Worker
@CommonsLog
public class MTWebSpider extends CrawlSpider {

	private Site site = SiteBuilder.builder().setDomain("www.motie.com").build();
	private PageRule rules = PageRule.build().add("/donate/list", this::processIncome);

	public void processIncome(Page page) {
		String bookId = page.getUrl().regex(".*\\/book\\/(\\d+)\\/donate", 1).get();
		FictionIncomeLogs incomeLogs = new FictionIncomeLogs();
		incomeLogs.setCode(bookId);
		incomeLogs.setIncomeId(FictionIncome.MOTIE_DASHANG.getCode());

		String incomeNumberStr = page.getHtml().xpath("span[@class=\"comment_tit_motiecoin\"]").regex(".*总计(\\d+).*")
				.get();
		if (StringUtils.isNotBlank(incomeNumberStr)) {
			incomeLogs.setIncomeNum(Integer.valueOf(incomeNumberStr));
			putModel(page, incomeLogs);
		}
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
