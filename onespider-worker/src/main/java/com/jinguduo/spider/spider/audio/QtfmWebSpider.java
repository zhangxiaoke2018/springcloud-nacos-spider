package com.jinguduo.spider.spider.audio;

import java.util.HashSet;
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
import com.jinguduo.spider.common.util.RegexUtil;
import com.jinguduo.spider.data.table.Audio;
import com.jinguduo.spider.data.table.AudioPlayCountLog;
import com.jinguduo.spider.data.table.AudioVolumeLog;
import com.jinguduo.spider.webmagic.Page;

//@Worker
public class QtfmWebSpider extends CrawlSpider {
	private Site site = SiteBuilder.builder().setDomain("i.qingting.fm").build();

	private PageRule rules = PageRule.build()
			.add("^https:\\/\\/i\\.qingting\\.fm$", this::processEntrance)
			.add("/neo-channel-filter", this::processRank)
			.add("/channels", this::processDetail);
	private static String RANK_URL_2 = "https://i.qingting.fm/capi/neo-channel-filter?category=%s&attrs=%d&curpage=%d";
	private static String RANK_URL = "https://i.qingting.fm/capi/neo-channel-filter?category=%s&attrs=%d-%d&curpage=%d";
	private static String DETAIL_URL = "https://i.qingting.fm/wapi/channels/%d";
	private static Map<Integer, String> categories = ImmutableMap.of(521, "AUDIO_BOOK");
	private static Map<Integer, String> genders = ImmutableMap.of(3289, "男生", 3290, "女生");
	private static Map<Integer, Map<Integer,String>> tags = ImmutableMap.of(521,new ImmutableMap.Builder<Integer, String>()
			.put(2741, "盗墓").put(2045, "总裁")
			.put(2386, "热门排行").put(3291, "主妇").put(4440, "鬼故事").put(2079, "蜻蜓FM出品").put(2174, "完本")
			.put(3326, "人气播音").put(3554, "免费").put(3632, "新书").build());

	private void processEntrance(Page page) {
		for (int categoryId : categories.keySet()) {
			for (int gender : genders.keySet()) {
				for (int tag : tags.get(categoryId).keySet()) {
					createJob(page, String.format(RANK_URL, categoryId, gender, tag, 1));
				}
				createJob(page, String.format(RANK_URL_2, categoryId, gender, 1));
			}
		}	
	}

	private void processRank(Page page) {
		JSONObject ob = page.getJson().toObject(JSONObject.class);
		if (ob != null && ob.containsKey("data")) {
			String tags = page.getUrl().regex(".*attrs=(.*)&.*", 1).get();
			int totalResult = ob.getInteger("total");
			int totalPage = totalResult==0?0:(totalResult-1)/12+1;
			int currentPage = ob.getInteger("curpage");
			
			if(totalPage>0 && currentPage<=totalPage) {
				JSONArray items = ob.getJSONObject("data").getJSONArray("channels");
				for(int i =0,isize = items.size();i<isize;i++) {
					createJob(page,String.format(DETAIL_URL, items.getJSONObject(i).getInteger("id"))+"#"+tags);
				}
			}
			
			if(totalPage>1&&currentPage==1) {
				for(int j=2,jsize = Math.min(totalPage, 10);j<=jsize;j++) {
					String url = page.getUrl().replace("curpage=1", "curpage="+j).toString();
					createJob(page,url);
				}
			}
		}
	}
	
	private void processDetail(Page page) {
		JSONObject ob = page.getJson().toObject(JSONObject.class);
		if (ob != null && ob.containsKey("data")) {
			JSONObject data = ob.getJSONObject("data");
			String attrs = page.getUrl().regex(".*#(\\d+-?\\d+?)$", 1).get();
			int categoryId = data.getInteger("category_id");
			
			Audio audio = new Audio();
			audio.setCover(data.getString("img_url"));
			audio.setCode(data.getString("id"));
			audio.setPlatformId(Platform.QTFM.getCode());
			audio.setCategory(categories.get(categoryId));
			
			Set<String> castername = new HashSet<>();
			int len = 0;
			JSONArray podcasters = data.getJSONArray("podcasters");
			if(podcasters!=null&&podcasters.size()>0) {
				String name;
				for(int i=0,isize = podcasters.size();i<isize;i++) {
					name = podcasters.getJSONObject(i).getString("name");
					castername.add(name);
					len+=name.length();
					if(len>100)
						break;
				}
			}
			audio.setPublisher(String.join(",", castername));
			String intro = data.getString("desc");
			if(audio.getCategory().equals("AUDIO_BOOK")) {
				audio.setOriginalAuthor(parseAuthor(intro));
			}
			try {
				intro = HtmlUtils.htmlUnescape(intro);
				intro = intro.replaceAll("<[^>]*>|\r|\n", "");
				intro = intro.replaceAll("\\s+", " ");
			} catch (Exception e) {

			}
			audio.setIntroduction(intro);
			String tag;
			if(attrs.contains("-")) {
				tag = genders.get(Integer.valueOf(attrs.substring(0, attrs.indexOf("-"))))+","+tags.get(categoryId).get(Integer.valueOf(attrs.substring(attrs.indexOf("-")+1)));
			}else {
				tag = genders.get(Integer.valueOf(attrs));
			}
			audio.setTags(tag);
			audio.setName(data.getString("name"));
			putModel(page,audio);
			
			AudioPlayCountLog playCount = new AudioPlayCountLog();
			playCount.setPlatformId(Platform.QTFM.getCode());
			playCount.setCode(audio.getCode());
			playCount.setPlayCount((long)(NumberHelper.parseShortNumber(data.getString("playcount"),0)));
			putModel(page,playCount);
			
			AudioVolumeLog volume = new AudioVolumeLog();
			volume.setPlatformId(Platform.QTFM.getCode());
			volume.setCode(audio.getCode());
			volume.setVolumes(data.getInteger("program_count"));
			putModel(page,volume);
		}
	}
	
	private String parseAuthor(String text) {
		String[] authorAhead = {"【作者简介】","作者:","作者：","原著：","作者简介：","【作者】"};
		String author = null;
		for(String a:authorAhead) {
			if(text.contains(a)) {
				String regex = ".*"+a+"\\s?([^<>：:，,—.。；;\\s\\n\\r\\t【】]+).*";
				String authorText = RegexUtil.getDataByRegex(text, regex);
				if(authorText!=null) {
					authorText = authorText.replaceAll("@|\\u0020|\\u00A0|\\：|\\:|（|\\[|\\(|【|】|\\)|\\]|）|正版授权|原著|作者|播音|签约", "");
					if(authorText.length()>0&&(author==null||author.length()>authorText.length())) {
						author = authorText;
					}
				}
				
			}
		}
		return author;
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
