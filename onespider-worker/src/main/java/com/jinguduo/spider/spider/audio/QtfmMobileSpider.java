package com.jinguduo.spider.spider.audio;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.web.util.HtmlUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.ImmutableMap;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.constant.CommonEnum.Platform;
import com.jinguduo.spider.common.util.Md5Util;
import com.jinguduo.spider.common.util.NumberHelper;
import com.jinguduo.spider.data.table.Audio;
import com.jinguduo.spider.data.table.AudioPlayCountLog;
import com.jinguduo.spider.data.table.AudioVolumeLog;
import com.jinguduo.spider.webmagic.Page;

@Worker
public class QtfmMobileSpider extends CrawlSpider {
	private Site site = SiteBuilder.builder().setDomain("webapi.qingting.fm").build();

	private PageRule rules = PageRule.build()
			.add("/search/layer", this::processEntrance)
			.add("/categories/", this::processRank)
			.add("/channels/", this::processDetail);

	private static String RANK_URL = "https://webapi.qingting.fm/api/mobile/categories/%d/attr/playcount%s/?page=%d";
	private static String RANK_URL_2 = "https://webapi.qingting.fm/api/mobile/categories/%d/attr/0/?page=%d";
	private static String DETAIL_URL = "https://webapi.qingting.fm/api/mobile/channels/%d";
	private static Map<Integer, String> categories = ImmutableMap.of(1599, "KID", 521, "AUDIO_BOOK");;
	private static Map<Integer, Map<Integer, String>> tags = ImmutableMap.of(1599,
			new ImmutableMap.Builder<Integer, String>().put(3823, "儿歌").put(3824, "绘本").put(3825, "故事").put(3828, "英语")
					.put(3833, "动画").build(), 521,new ImmutableMap.Builder<Integer, String>()
					.put(2741, "盗墓").put(2045, "总裁")
					.put(2386, "热门排行").put(3291, "主妇").put(4440, "鬼故事").put(2079, "蜻蜓FM出品").put(2174, "完本")
					.put(3326, "人气播音").put(3554, "免费").put(3632, "新书").build());

	private void processEntrance(Page page) {
		for (int categoryId : categories.keySet()) {
			for (int tag : tags.get(categoryId).keySet()) {
				for (int i = 1; i <= 20; i++) {
					createJob(page, String.format(RANK_URL, categoryId, "-" + tag, i));
				}
			}
			for (int i = 1; i <= 20; i++) {
				createJob(page, String.format(RANK_URL, categoryId, "", i));
			}
			
			for (int i = 1; i <= 100; i++) {
				createJob(page, String.format(RANK_URL_2, categoryId, i));
			}
		}
	}

	private void processRank(Page page) {
		JSONObject ob = page.getJson().toObject(JSONObject.class);
		if (ob != null && ob.containsKey("FilterList")) {
			JSONArray data = ob.getJSONArray("FilterList");
			JSONObject item;
			for(int i = 0,size=data.size();i<size;i++) {
				item = data.getJSONObject(i);
				
//				String code = String.valueOf(item.getIntValue("id"));
				createJob(page, String.format(DETAIL_URL,item.getIntValue("id")));
			}
		}
	}

	private void processDetail(Page page) {
		JSONObject data = page.getJson().toObject(JSONObject.class);
		
		JSONObject channel = data.getJSONObject("channel");
		Audio audio = new Audio();
		audio.setPlatformId(Platform.QTFM.getCode());
		audio.setCode(String.valueOf(channel.getIntValue("channelId")));
		audio.setName(channel.getString("title"));
		audio.setIntroduction(channel.getString("desc").replaceAll("\\r|\\n", ""));
		audio.setCover(channel.getString("img"));
		/**
		 * free/channel-sale
		 */
		//audio.setPaid("free".equals(channel.getString("channelType"))?0:1);
		
		
		JSONArray podcasters = channel.getJSONArray("podcasters");
		List<String> s = new ArrayList<>();
		for(int i=0,size=podcasters.size();i<size;i++) {
			s.add(podcasters.getJSONObject(i).getString("name"));
		}
		audio.setPublisher(String.join(",", s));
		int categoryId = channel.getIntValue("categoryId");
		audio.setCategory(categories.getOrDefault(categoryId, "UNKNOWN"));
		
		JSONArray attributes = data.getJSONArray("attributes");
		List<String> tags = new ArrayList<>();
		for(int i=0,size=attributes.size();i<size;i++) {
			tags.add(attributes.getJSONObject(i).getString("name"));
		}
		audio.setTags(String.join(",", tags));
		putModel(page,audio);
		
		AudioPlayCountLog audioPlayCountLog = new AudioPlayCountLog();
		audioPlayCountLog.setCode(audio.getCode());
		audioPlayCountLog.setPlatformId(Platform.QTFM.getCode());
		audioPlayCountLog.setPlayCount(NumberHelper.parseShortNumber(channel.getString("playCount"),0L));
		putModel(page,audioPlayCountLog);
		
		
		AudioVolumeLog audioVolumeLog = new AudioVolumeLog();
		audioVolumeLog.setCode(audio.getCode());
		audioVolumeLog.setPlatformId(Platform.QTFM.getCode());
		audioVolumeLog.setVolumes(channel.getIntValue("programCount"));
		putModel(page,audioVolumeLog);
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

	private void createJob(Page page, String url) {
		Job job = new Job(url);
		job.setCode(Md5Util.getMd5(url));
		job.setPlatformId(Platform.QTFM.getCode());
		putModel(page, job);
	}
}
