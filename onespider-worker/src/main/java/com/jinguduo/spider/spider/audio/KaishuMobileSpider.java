package com.jinguduo.spider.spider.audio;

import java.sql.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.constant.CommonEnum.Platform;
import com.jinguduo.spider.common.util.Md5Util;
import com.jinguduo.spider.common.util.NumberHelper;
import com.jinguduo.spider.common.util.RegexUtil;
import com.jinguduo.spider.data.table.Audio;
import com.jinguduo.spider.data.table.AudioPlayCountLog;
import com.jinguduo.spider.data.table.AudioVolumeLog;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.model.HttpRequestBody;
import com.jinguduo.spider.webmagic.utils.HttpConstant.Method;

@Worker
public class KaishuMobileSpider extends CrawlSpider {
	private Site site = SiteBuilder.builder().setDomain("api.kaishustory.com").addHeader("appVersion", "6.7.2")
			.addHeader("channelid", "yingyongbao").addHeader("appid", "992099001")
			.addSpiderListener(new KaishuTokenSpiderListener()).build();

	private PageRule rules = PageRule.build().add("/top/story/hot", this::processTopHotJob)
			.add("/top/story/product/vip", this::processTopVIPJob).add("/appinit/initialize", this::processToken)
			.add("/story/findbyid", this::processStoryDetail).add("/product/detail/storylist", this::processDetail);

	// private static String currentToken =
	// "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJhdWQiOiI5OTIwOTkwMDEiLCJpbCI6ZmFsc2UsImlzcyI6ImthaXNodXN0b3J5IiwiZXhwIjoyNTYwNjcwMzQyLCJ1ZCI6MH0.YHepSgPDoPD9M5rTyEYc4rtABTIH8Y7YFS1fIBH1mhg";
	private static final String TOKEN_URL = "http://api.kaishustory.com/userapi/appinit/initialize";
	private static final String TOP_HOT_URL = "http://api.kaishustory.com/top/story/hot?page_no=%d&page_size=10&token=%s";
	private static final String VIP_HOT_URL = "http://api.kaishustory.com/top/story/product/vip?page_no=%d&page_size=10&token=%s";
	private static final String STORY_DETAIL_URL = "http://api.kaishustory.com/storyservice/story/findbyid?id=%d&token=%s";
	private static final String ALBUM_URL = "http://api.kaishustory.com/product/detail/storylist?page_size=500&productid=%d&token=%s";

	private void processToken(Page page) {
		JSONObject result = page.getJson().toObject(JSONObject.class);

		if (0 == result.getIntValue("code")) {
			String token = result.getJSONObject("data").getString("token");
			putModel(page, createUrlJob(String.format(TOP_HOT_URL, 1, token)));
		}
	}

	private void processTopVIPJob(Page page) {
		JSONObject data = page.getJson().toObject(JSONObject.class);
		if (data.getIntValue("errcode") == 0) {
			String token = page.getRequest().getHeaders().get("token");
			if (null == token || StringUtils.isEmpty(token)) {
				return;
			}
			JSONArray list = data.getJSONObject("result").getJSONArray("list");
			JSONObject item;
			for (int i = 0, size = list.size(); i < size; i++) {
				item = list.getJSONObject(i);
				if ("product".equals(item.getString("contenttype"))) {
					int productid = item.getJSONObject("product").getIntValue("productid");
					putModel(page, createUrlJob(String.format(ALBUM_URL, productid, token)));
				}
			}
		}
	}

	private void processStoryDetail(Page page) {
		String token = page.getRequest().getHeaders().get("token");
		if (null == token || StringUtils.isEmpty(token)) {
			return;
		}
		JSONObject data = page.getJson().toObject(JSONObject.class);
		if (data.getIntValue("errcode") == 0) {
			int productid = data.getJSONObject("result").getJSONObject("product").getIntValue("productid");
			putModel(page, createUrlJob(String.format(ALBUM_URL, productid, token)));
		}
	}

	private void processTopHotJob(Page page) {
		String page_no = page.getUrl().regex("&?page_no=([0-9]+)").get();

		JSONObject data = page.getJson().toObject(JSONObject.class);
		if (data.getIntValue("errcode") != 0 && "1".equals(page_no.trim())) {
			putModel(page, createTokenJob());
		} else if (data.getIntValue("errcode") == 0) {
			String token = page.getRequest().getHeaders().get("token");
			if (null == token || StringUtils.isEmpty(token)) {
				return;
			}
			JSONArray list = data.getJSONObject("result").getJSONArray("list");
			int pageNumber = data.getJSONObject("result").getIntValue("page_no");
			if (pageNumber == 1) {

				for (int i = 2; i <= 10; i++) {
					putModel(page, createUrlJob(String.format(TOP_HOT_URL, i, token)));
				}

				for (int i = 1; i <= 10; i++) {
					putModel(page, createUrlJob(String.format(VIP_HOT_URL, i, token)));
				}
			}
			JSONObject item;
			for (int i = 0, size = list.size(); i < size; i++) {
				item = list.getJSONObject(i);
				if ("story".equals(item.getString("contenttype"))
						&& "01".equals(item.getJSONObject("story").getString("feetype"))) {
					int storyid = item.getJSONObject("story").getIntValue("storyid");
					putModel(page, createUrlJob(String.format(STORY_DETAIL_URL, storyid, token)));
				}
			}
		}
	}

	private void processDetail(Page page) {
		JSONObject data = page.getJson().toObject(JSONObject.class);

		if (data.getIntValue("errcode") != 0) {
			createTokenJob();
		} else {
			JSONObject product = data.getJSONObject("result").getJSONObject("product");
			Audio audio = new Audio();
			audio.setPublisher("凯叔说故事");
			audio.setPlatformId(Platform.KS_STORY.getCode());
			audio.setCategory("KID");
			audio.setName(product.getString("productname"));
			audio.setCode(String.valueOf(product.getInteger("productid")));
			audio.setCover(product.getString("homeiconurl"));
			String label = product.getString("label");
			if (!label.contains("故事")) {
				label = label + ",故事";
			}
			audio.setTags(label);
			audio.setIntroduction(product.getString("description"));
			audio.setReleaseDate(new Date(product.getLongValue("createtime")));
			putModel(page, audio);

			int storyCount = data.getJSONObject("result").getJSONObject("headerinfo").getIntValue("loadallstorycount");
			AudioVolumeLog volumeLog = new AudioVolumeLog();
			volumeLog.setCode(audio.getCode());
			volumeLog.setPlatformId(Platform.KS_STORY.getCode());
			volumeLog.setVolumes(storyCount);
			putModel(page, volumeLog);

			JSONArray moduleList = data.getJSONObject("result").getJSONArray("modulelistvalue");
			JSONObject item;
			Map<String, Long> pc = new HashMap<>();
			for (int i = 0, size = moduleList.size(); i < size; i++) {
				JSONArray list = moduleList.getJSONObject(i).getJSONArray("list");
				for (int j = 0, sizej = list.size(); j < sizej; j++) {
					// "storyid": 102923,"storyname": "冒险湾的守护者","playcount": 9365696,"commentcount":
					// 587,
					item = list.getJSONObject(j);
					pc.put(String.valueOf(item.getIntValue("storyid")), item.getLong("playcount"));
				}
			}

			// type 2
			if (pc.size() == 0) {
				JSONArray list = data.getJSONObject("result").getJSONObject("productlistvalue").getJSONObject("info")
						.getJSONArray("list");
				for (int j = 0, sizej = list.size(); j < sizej; j++) {
					item = list.getJSONObject(j);
					pc.put(String.valueOf(item.getIntValue("storyid")), item.getLong("playcount"));
				}
			}

			Long totalPlayCount = pc.values().stream().reduce(0L, Long::sum);
			AudioPlayCountLog playcountLog = new AudioPlayCountLog();
			playcountLog.setCode(audio.getCode());
			playcountLog.setPlatformId(Platform.KS_STORY.getCode());
			playcountLog.setPlayCount(totalPlayCount);
			putModel(page, playcountLog);
		}
	}

	private Job createTokenJob() {
		String deviceId = Hex.encodeHexString(RandomUtils.nextBytes(8));
		String macAddress = NumberHelper.generateFakeMacAddress();
		String seriesNumber = Hex.encodeHexString(RandomUtils.nextBytes(8));
		String imei = NumberHelper.generateRandomNumberSeries(15);

		String postData = String
				.format("{\"adrdid\":\"adrdid:%s\",\"adrdmac\":\"adrdmac:%s\",\"adrdsnmac\":\"adrdsnmac:%s%s\""
						+ ",\"adrksimei\":\"%s\",\"channelMsg\":\"android\",\"phoneDeviceCode\":\"%s\",\"phoneModel\":\"Nexus 5\""
						+ ",\"sysVersion\":\"23\"}", deviceId, macAddress, seriesNumber, macAddress, imei, imei);

		Job job = new Job(TOKEN_URL);
		job.setMethod(Method.POST);
		job.setFrequency(14400);
		job.setCode(Md5Util.getMd5(TOKEN_URL));
		job.setHttpRequestBody(HttpRequestBody.json(postData, "utf-8"));
		job.setPlatformId(Platform.KS_STORY.getCode());
		return job;
	}

	private Job createUrlJob(String url) {
		Job job = new Job();
		job.setUrl(url);
		job.setMethod(Method.GET);
		job.setCode(Md5Util.getMd5(url));
		job.setPlatformId(Platform.KS_STORY.getCode());
		return job;
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
