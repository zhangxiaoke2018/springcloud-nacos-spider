package com.jinguduo.spider.spider.fiction;

import com.alibaba.fastjson.JSONObject;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.constant.CommonEnum.FictionIncome;
import com.jinguduo.spider.data.table.FictionIncomeLogs;
import com.jinguduo.spider.webmagic.Page;

//@Worker
public class _17KWebSpider extends CrawlSpider {
	private Site site = SiteBuilder.builder().setDomain("api.ali.17k.com").build();
	private PageRule rules = PageRule.build().add("/v2\\/book\\/\\d+\\/stat_info", this::processIncome);

	public void processIncome(Page page) {
		String bookId = page.getUrl().regex(".*\\/book\\/(\\d+)\\/stat_info.*", 1).get();
		JSONObject response = page.getJson().toObject(JSONObject.class);
		if (response != null && response.getJSONObject("status").getInteger("code") == 0) {
			JSONObject hbInfo = response.getJSONObject("data").getJSONObject("hb_info");
			FictionIncomeLogs incomeLogs = new FictionIncomeLogs();
			incomeLogs.setCode(bookId);
			incomeLogs.setIncomeId(FictionIncome._17K_HONGBAO.getCode());
			if(hbInfo.containsKey("total_count"))
				incomeLogs.setIncomeNum(hbInfo.getInteger("total_count"));
			putModel(page,incomeLogs);
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
