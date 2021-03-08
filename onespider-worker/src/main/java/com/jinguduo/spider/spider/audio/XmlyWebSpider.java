package com.jinguduo.spider.spider.audio;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
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
import com.jinguduo.spider.common.util.RegexUtil;
import com.jinguduo.spider.data.table.Audio;
import com.jinguduo.spider.data.table.AudioPlayCountLog;
import com.jinguduo.spider.data.table.AudioVolumeLog;
import com.jinguduo.spider.webmagic.Page;

@Worker
public class XmlyWebSpider extends CrawlSpider {
	private Site site = SiteBuilder.builder().setDomain("www.ximalaya.com").build();

	private static final String DETAIL_URL = "https://www.ximalaya.com/%s/%d/#%s";
	private static final String HOST_URL = "https://www.ximalaya.com";

	private static final Map<String, Map<String, String>> KEYWORDS = new ImmutableMap.Builder<String, Map<String, String>>()
			.put("ertong",
					new ImmutableMap.Builder<String, String>().put("reci336", "动画").put("reci155", "故事")
							.put("reci567", "绘本").put("reci337","儿歌").put("xueqianyingyu","英语").build())
			.put("youshengshu",
					new ImmutableMap.Builder<String, String>().put("reci22", "QQ阅读").put("reci348", "男生最爱")
							.put("reci346", "女生最爱").put("reci407", "官场商战").put("reci234", "历史")
							.put("lishixiaoshuo", "历史小说").put("wuxia", "武侠").put("huanxiang", "幻想").put("dushi", "都市")
							.put("xuanyi", "悬疑").put("yanqing", "言情").build())
			.build();

	private static final String[] types = {  "ertong","youshengshu" };
	private static final Map<Integer, String> CATEGORIES = ImmutableMap.of(6, "KID", 3, "AUDIO_BOOK");
	private static final String[] sorts = { "mostplays", "updates", "" };
	private PageRule rules = PageRule.build().add("^https:\\/\\/www\\.ximalaya\\.com$", this::processEntrance)
			.add(".*\\/(ertong|youshengshu)\\/.*p\\d+\\/$", this::processCategory)
			.add(".*\\/(ertong|youshengshu)\\/\\d+\\/#.*$", this::processDetail);

	public void processEntrance(Page page) {
		for (String type : types) {
			Map<String,String> keywords = KEYWORDS.get(type);
			for (String keyword : keywords.keySet()) {
				for (String s : sorts) {
					for (int i = 1; i <= 30; i++) {
						String url = HOST_URL+"/"+type+"/"+keyword+"/";
						if(s.length()>0) {
							url += (s+"/");
						}			
						url+= ("p" + i + "/");
						createJob(page, url);
					}
				}
			}
		}
	}

	private void processCategory(Page page) {
		Elements es = page.getHtml().getDocument().getElementsByTag("script");
		Element el;
		for (int i = 0, size = es.size(); i < size; i++) {
			el = es.get(i);
			String text = el.data();
			if (text.contains("window.__INITIAL_STATE__")) {
				String json = text.substring("window.__INITIAL_STATE__ = ".length(), text.length() - 1);
//				System.out.println(text);
				JSONObject ob = JSONObject.parseObject(json).getJSONObject("store");
//				System.out.println(json);
				JSONArray array = ob.getJSONObject("CategoryFilterResultPage").getJSONObject("albumsResult")
						.getJSONArray("albums");
				String subCategory = ob.getJSONObject("CategoryFilterResultPage").getJSONObject("urlInfo")
						.getString("subcategory");
				String category = ob.getJSONObject("CategoryFilterResultPage").getJSONObject("urlInfo")
						.getString("category");
				AudioPlayCountLog playCountLog;
				AudioVolumeLog volume;
				for (int j = 0, jsize = array.size(); j < jsize; j++) {
					Integer albumId = array.getJSONObject(j).getInteger("albumId");
					Long playCount = array.getJSONObject(j).getLong("playCount");
					Integer trackCount = array.getJSONObject(j).getInteger("trackCount");
					String url = String.format(DETAIL_URL, category, albumId, subCategory);
					createJob(page, url);

					volume = new AudioVolumeLog();
					volume.setCode(String.valueOf(albumId));
					volume.setPlatformId(Platform.XMLY.getCode());
					volume.setVolumes(trackCount);
					putModel(page, volume);

					playCountLog = new AudioPlayCountLog();
					playCountLog.setCode(String.valueOf(albumId));
					playCountLog.setPlatformId(Platform.XMLY.getCode());
					playCountLog.setPlayCount(playCount);
					putModel(page, playCountLog);
				}
				break;
			}
		}
	}

	private void processDetail(Page page) {
		String category = page.getUrl().regex(".*\\/(ertong|youshengshu)\\/\\d+\\/#([0-9a-zA-Z]+)$",1).get();
		String subCategory = page.getUrl().regex(".*\\/(ertong|youshengshu)\\/\\d+\\/#([0-9a-zA-Z]+)$",2).get();
		Elements es = page.getHtml().getDocument().getElementsByTag("script");
		Element el;
		for (int i = 0, size = es.size(); i < size; i++) {
			el = es.get(i);
			String text = el.data();
			if (text.contains("__INITIAL_STATE__")) {
				String json = text.substring("window.__INITIAL_STATE__ = ".length(), text.length() - 1);
				JSONObject ob = JSONObject.parseObject(json).getJSONObject("store").getJSONObject("AlbumDetailPage")
						.getJSONObject("albumInfo");
				Audio audio = new Audio();
				audio.setCode(String.valueOf(ob.getIntValue("albumId")));
				JSONObject mainInfo = ob.getJSONObject("mainInfo");
				audio.setPlatformId(Platform.XMLY.getCode());
				audio.setCategory(CATEGORIES.get(mainInfo.getJSONObject("crumbs").getIntValue("categoryId")));
				audio.setName(mainInfo.getString("albumTitle"));
				
				String cover  = mainInfo.getString("cover");
				if(cover.startsWith("//"))
					cover = "https:"+cover;
				audio.setCover(cover);
				audio.setReleaseDate(new java.sql.Date(mainInfo.getDate("createDate").getTime()));
				audio.setIsFinish(mainInfo.getInteger("isFinished"));
				audio.setPublisher(ob.getJSONObject("anchorInfo").getString("anchorName"));

				String detailRichIntro = mainInfo.getString("detailRichIntro");

				try {
					detailRichIntro = HtmlUtils.htmlUnescape(detailRichIntro);
					detailRichIntro = detailRichIntro.replaceAll("<br />|<br/>|</p>", "\u0020");
					detailRichIntro = detailRichIntro.replaceAll("<[^>]*>", "\u0020");
					detailRichIntro = detailRichIntro.replaceAll("\u00A0", "\u0020");
					detailRichIntro = detailRichIntro.replaceAll("\\s+|\\r|\\n", "\u0020");
				} catch (Exception e) {

				}

				if(audio.getCategory().equals("AUDIO_BOOK"))
					audio.setOriginalAuthor(parseAuthor(detailRichIntro));
				audio.setIntroduction(detailRichIntro);
				JSONArray metaList = mainInfo.getJSONArray("metas");
				JSONObject meta;
				Set<String> tags = new HashSet<>();
				if(metaList!=null&&metaList.size()>0) {
					for(int  k = 0, ksize = metaList.size();k<ksize;k++) {
						meta = metaList.getJSONObject(k);
						tags.add(meta.getString("metaDisplayName").trim());
					}
				}
				tags.add(KEYWORDS.get(category).get(subCategory));
				audio.setTags(String.join(",", tags));
				putModel(page, audio);
				break;
			}
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
		job.setCode(Md5Util.getMd5(clearHashTag(url)));
		job.setPlatformId(Platform.XMLY.getCode());
		putModel(page, job);
	}
	
	public static String clearHashTag(String url) {
		int indexHashTag = url.indexOf("#");
		if(indexHashTag>=0) {
			return url.substring(0, indexHashTag);
		}else {
			return url;
		}
	}
}
