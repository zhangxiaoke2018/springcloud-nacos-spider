package com.jinguduo.spider.spider.weibo;

import java.sql.Timestamp;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.data.text.WeiboHotSearchText;
import com.jinguduo.spider.webmagic.Page;

import lombok.extern.apachecommons.CommonsLog;

@Worker
@CommonsLog
public class WeiboMobileSpider extends CrawlSpider {
	private Site site = SiteBuilder.builder().setDomain("api.weibo.cn").addHeader("Cache-Control", "no-cache")
			.addHeader("Pragma", "no-cache").build();

	private PageRule rules = PageRule.build().add("realtimehot", page -> processRealtimeHot(page));

	private void processRealtimeHot(Page page) {
		JSONObject result = page.getJson().toObject(JSONObject.class);
		if (result != null && result.containsKey("cards")) {
			JSONArray cards = result.getJSONArray("cards");
			JSONObject card;
			for (int i = 0, len = cards.size(); i < len; i++) {
				card = cards.getJSONObject(i);
				if ("hotword".equals(card.getString("itemid")) && card.containsKey("card_group")) {
					JSONArray hotwords = card.getJSONArray("card_group");
					JSONObject hotword;
					WeiboHotSearchText model;
					Timestamp crawledAt = new Timestamp(System.currentTimeMillis());
					for (int j = 0, jLen = hotwords.size(); j < jLen; j++) {
						hotword = hotwords.getJSONObject(j);
						model = new WeiboHotSearchText();
						model.setCrawledAt(crawledAt);
						model.setText(hotword.getString("desc"));
						model.setHotValue(hotword.getIntValue("desc_extr"));
						putModel(page, model);
					}

					break;
				}
			}
		} else {
			log.info("error:" + (result == null ? "emtpy" : result.toJSONString()));
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
