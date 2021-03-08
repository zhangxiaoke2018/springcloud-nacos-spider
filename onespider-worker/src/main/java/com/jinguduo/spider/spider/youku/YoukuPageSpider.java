package com.jinguduo.spider.spider.youku;

import org.apache.http.client.config.CookieSpecs;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.constant.CommonEnum.BannerType;
import com.jinguduo.spider.common.constant.CommonEnum.Platform;
import com.jinguduo.spider.common.util.Md5Util;
import com.jinguduo.spider.common.util.RegexUtil;
import com.jinguduo.spider.data.table.BannerRecommendation;
import com.jinguduo.spider.webmagic.Page;

@Worker
public class YoukuPageSpider extends CrawlSpider {

	private static final String WEB_CHANNEL_BANNER = "https://tv.youku.com#WEB_CHANNEL_BANNER";

	private static final String MOBILE_HOME_BANNER = "https://www.youku.com#MOBILE_HOME_BANNER";
	
	private static final String MOBILE_CHANNEL_BANNER = "https://tv.youku.com#MOBILE_CHANNEL_BANNER";
	
	private static Site site = SiteBuilder.builder().setDomain("www.youku.com")
			.addSpiderListener(new YoukuBannerSpiderListener()).build();

	private PageRule rule = PageRule.build().add("#WEB_HOME_BANNER", page -> processPCBanner(page))
			.add("#MOBILE_HOME_BANNER", page -> processMobileBanner(page));

	private void processPCBanner(Page page) {
		JSONObject pageInfo = parsePcPage(getData(page));
		processPCBanner(page, pageInfo.getJSONObject("PC_BANNER"), BannerType.WEB_HOME_BANNER);
		createUrlJob(page,WEB_CHANNEL_BANNER);
		createUrlJob(page,MOBILE_HOME_BANNER);
		createUrlJob(page,MOBILE_CHANNEL_BANNER);
	}
	
	private void createUrlJob(Page page,String url) {
		Job job = new Job(url);
		job.setPlatformId(Platform.YOU_KU.getCode());
		job.setCode(Md5Util.getMd5(url));
		putModel(page,job);
	}

	private void processMobileBanner(Page page) {
		JSONObject pageInfo = parseMobilePage(getData(page));
		processMobileBanner(page, pageInfo.getJSONObject("PHONE_LUNBO_N"), BannerType.MOBILE_HOME_BANNER);
		processMobileBanner(page, pageInfo.getJSONObject("PHONE_BASE_B"), BannerType.MOBILE_HOME_RECOMMEND);
	}

	private void processPCBanner(Page page, JSONObject bannerInfo, BannerType bannerType) {
		JSONArray itemList = bannerInfo.getJSONArray("focusList");

		int len = itemList.size();
		for (int i = 0; i < len; i++) {
			String videoLink = itemList.getJSONObject(i).getString("link");
			if (videoLink.contains("v.youku.com") && videoLink.contains("s=")) {
				String albumId = RegexUtil.getDataByRegex(videoLink, ".*[^s]+s=([a-f0-9]{20})");
				if (null != albumId) {
					/**
					 * WARNING: 优酷code新版为20位长度16进制hash，由于库里暂存为21位showid
					 * ，即z+新ID，所以在此为了兼容均添加z在开头，组成21位code
					 */
					BannerRecommendation br = new BannerRecommendation("z" + albumId, Platform.YOU_KU.getCode(),
							bannerType);
					putModel(page, br);
				}
			}
		}
	}

	private void processMobileBanner(Page page, JSONObject bannerInfo, BannerType bannerType) {
		JSONArray itemList = bannerInfo.getJSONArray("components").getJSONObject(0).getJSONArray("itemMap");

		int len = itemList.size();
		if (bannerType == BannerType.MOBILE_HOME_RECOMMEND) {
			len = Math.min(4, len);
		}
		JSONObject item;
		for (int i = 0; i < len; i++) {
			item = itemList.getJSONObject(i);
			if (!item.containsKey("action"))
				continue;
			String actionType = item.getJSONObject("action").getString("type");
			String albumId = item.getJSONObject("action").getJSONObject("extra").getString("value");
			if (albumId == null)
				continue;
			if ("JUMP_TO_SHOW".equals(actionType) && albumId.matches("([a-f0-9]{20})")) {
				/**
				 * WARNING: 优酷code新版为20位长度16进制hash，由于库里暂存为21位showid
				 * ，即z+新ID，所以在此为了兼容均添加z在开头，组成21位code
				 */
				BannerRecommendation br = new BannerRecommendation("z" + albumId, Platform.YOU_KU.getCode(),
						bannerType);
				putModel(page, br);

			} else if ("JUMP_TO_VIDEO".equals(actionType) && albumId.matches("([a-zA-Z0-9]{15}==)")) {
				/**
				 * 这种情况跳转链接是分集code
				 */
				BannerRecommendation br = new BannerRecommendation(albumId, Platform.YOU_KU.getCode(), bannerType);
				putModel(page, br);
			}
		}
	}

	private String getData(Page page) {
		String data = page.getHtml().regex("<script>window.__INITIAL_DATA__ =([^;^<]+)", 1).get();
		data = data.replaceAll("undefined", "null");
		return data;
	}

	private JSONObject parsePcPage(String data) {
		JSONArray moduleList = null;
		if (data.startsWith("[")) {
			JSONArray a = JSON.parseObject(data.getBytes(), JSONArray.class);
			moduleList = a.getJSONObject(0).getJSONArray("moduleList");
		} else {
			JSONObject o = JSON.parseObject(data.getBytes(), JSONObject.class);
			moduleList = o.getJSONArray("moduleList");
		}
		JSONObject nOb = new JSONObject();
		int len = moduleList.size();
		JSONObject comp;
		for (int i = 0; i < len; i++) {
			comp = moduleList.getJSONObject(i);
			if (comp.containsKey("type")) {
				nOb.put(comp.getString("type"), comp);
			}
		}
		return nOb;
	}

	private JSONObject parseMobilePage(String data) {
		JSONArray moduleList = null;
		if (data.startsWith("[")) {
			JSONArray a = JSON.parseObject(data.getBytes(), JSONArray.class);
			moduleList = a.getJSONObject(0).getJSONObject("data").getJSONArray("moduleList");
		} else {
			JSONObject o = JSON.parseObject(data.getBytes(), JSONObject.class);
			moduleList = o.getJSONObject("data").getJSONArray("moduleList");
		}
		JSONObject nOb = new JSONObject();
		int len = moduleList.size();
		JSONObject comp;
		for (int i = 0; i < len; i++) {
			comp = moduleList.getJSONObject(i);
			if (comp.containsKey("title")) {
				String title = comp.getString("title");
				String tag = comp.getJSONArray("components").getJSONObject(0).getJSONObject("template")
						.getString("tag");
				if (!StringUtils.isEmpty(title)) {
					nOb.put(title, comp);
				} else {
					nOb.put(tag, comp);
				}
			}
		}
		return nOb;
	}

	@Override
	public Site getSite() {
		return site;
	}

	@Override
	public PageRule getPageRule() {
		return rule;
	}

}
