package com.jinguduo.spider.spider.audio;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.web.util.HtmlUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.constant.CommonEnum.Platform;
import com.jinguduo.spider.common.util.Md5Util;
import com.jinguduo.spider.data.table.Audio;
import com.jinguduo.spider.data.table.AudioPlayCountLog;
import com.jinguduo.spider.data.table.AudioVolumeLog;
import com.jinguduo.spider.webmagic.Page;

@Worker
public class LrtsWebSpider extends CrawlSpider {
	private Site site = SiteBuilder.builder().setDomain("m.lrts.me")
			.setUserAgent("Mozilla/5.0 (iPhone; CPU iPhone OS 11_0 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/11.0 Mobile/15A372 Safari/604.1")
			.build();

	private PageRule rules = PageRule.build().add("^https:\\/\\/m\\.lrts\\.me$", this::processEntrance)
			.add("/getResourceList", this::processRank)
			.add("/getBookDetail", this::processDetail);

	private static Map<String, Set<Integer>> categories = ImmutableMap.of(
			"AUDIO_BOOK",ImmutableSet.of(11, 3020, 8, 3109, 9, 10, 44, 3107, 14, 15, 3106, 12, 3021, 9042, 9041,9208,9139),
			"KID",ImmutableSet.of(3027,63,68,9029,9246,59,64));

	private static int[] sorts = { 10001, 10002, 10003 };
	private static final String RANK_URL = "https://m.lrts.me/ajax/getResourceList?dsize=20&entityId=%d&entityType=1&pageNum=%d&showFilters=0&labelIds=%d";
	private static final String DETAIL_URL = "https://m.lrts.me/ajax/getBookDetail?bookId=%d";

	private void processEntrance(Page page) {
		for (String category : categories.keySet()) {
			for (int categoryId : categories.get(category)) {
				for (int sort : sorts) {
					createJob(page, String.format(RANK_URL, categoryId,1,sort));
				}
			}
		}
	}

	private void processRank(Page page) {
		JSONObject ob = page.getJson().toObject(JSONObject.class);
		if (ob != null && ob.containsKey("bookIds")) {
			JSONArray bookIds = ob.getJSONArray("bookIds");
			for (int i = 0, size = Math.min(100, bookIds.size()); i < size; i++) {
				int bookId = bookIds.getInteger(i);
				createJob(page, String.format(DETAIL_URL, bookId));
			}
		}
	}

	private void processDetail(Page page) {
		JSONObject ob = page.getJson().toObject(JSONObject.class);
		if (ob != null && ob.containsKey("data")) {
			JSONObject data = ob.getJSONObject("data").getJSONObject("bookDetail");
			Audio audio = new Audio();
			String announcer = data.getString("announcer");

			int textLimit = 100;
			if(announcer.length()>textLimit) {
				int endPoint = announcer.indexOf(",", textLimit);
				announcer = announcer.substring(0, endPoint==-1?textLimit:endPoint)+"等等";
			}

			audio.setPublisher(announcer);
			audio.setOriginalAuthor(data.getString("author"));
			String intro = data.getString("desc");
			try {
				intro = HtmlUtils.htmlUnescape(intro);
				intro = intro.replaceAll("<[^>]*>|\r|\n", "");
				intro = intro.replaceAll("\\s+", " ");
			} catch (Exception e) {

			}
			audio.setIntroduction(intro);
			Set<String> tags = new HashSet<>();
			JSONArray labels = data.getJSONArray("labels");
			if (labels != null && labels.size() > 0) {
				for (int j = 0, jsize = labels.size(); j < jsize; j++) {
					tags.add(labels.getJSONObject(j).getString("name"));
				}
			}
			tags.add(data.getString("type"));
			switch(data.getIntValue("typeId")) {
				case 3027:
				case 63:
					tags.add("故事");
					break;
				case 9029:
					tags.add("动画");
					break;
				case 9246:
					tags.add("绘本");
					break;
				case 68:
					tags.add("英语");
					break;
				case 59:
					tags.add("儿歌");
					break;
			}
			audio.setTags(String.join(",", tags));
		
			audio.setName(data.getString("name"));
			audio.setCode(data.getString("id"));
			audio.setPlatformId(Platform.LRTS.getCode());
			int typeId = data.getIntValue("typeId");
			String category = "UNKNOWN";
			for(String cat:categories.keySet()) {
				if(categories.get(cat).contains(typeId)) {
					category = cat;
					break;
				}
			}

			audio.setCategory(category);
			audio.setCover(data.getString("cover"));
			audio.setIsFinish(data.getInteger("state")==2?1:0);
			putModel(page,audio);

			AudioPlayCountLog audioPlayCount = new AudioPlayCountLog();
			audioPlayCount.setPlatformId(Platform.LRTS.getCode());
			audioPlayCount.setCode(audio.getCode());
			audioPlayCount.setPlayCount(data.getLong("play"));
			putModel(page,audioPlayCount);

			AudioVolumeLog volume = new AudioVolumeLog();
			volume.setPlatformId(Platform.LRTS.getCode());
			volume.setCode(audio.getCode());
			volume.setVolumes(data.getInteger("sections"));
			putModel(page,volume);
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

	private void createJob(Page page, String url) {
		Job job = new Job(url);
		job.setCode(Md5Util.getMd5(url));
		job.setPlatformId(Platform.LRTS.getCode());
		putModel(page, job);
	}

}
