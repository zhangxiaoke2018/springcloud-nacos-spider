package com.jinguduo.spider.spider.youku;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.Lists;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.code.FetchCodeEnum;
import com.jinguduo.spider.common.constant.CommonEnum;
import com.jinguduo.spider.common.constant.CommonEnum.BannerType;
import com.jinguduo.spider.common.constant.CommonEnum.Platform;
import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.common.util.RegexUtil;
import com.jinguduo.spider.data.table.AutoFindLogs;
import com.jinguduo.spider.data.table.BannerRecommendation;
import com.jinguduo.spider.data.table.Category;
import com.jinguduo.spider.data.table.Show;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.selector.Html;
import com.jinguduo.spider.webmagic.selector.Selectable;

@Worker
@Slf4j
public class YoukuBannerSpider extends CrawlSpider {

	private static Site site = SiteBuilder.builder().setDomain("tv.youku.com")
			.addSpiderListener(new YoukuBannerSpiderListener()).build();

	private static final String DETAIL_URL = "https://list.youku.com/show/id_%s.html";

	private PageRule rule = PageRule.build()
			.add("^((?!#(MOBILE_CHANNEL_BANNER|WEB_CHANNEL_BANNER)).)*$", page -> processMain(page))
			.add("#WEB_CHANNEL_BANNER", page -> processPCBanner(page))
			.add("#MOBILE_CHANNEL_BANNER", page -> processMobileBanner(page));;

	private void processPCBanner(Page page) {
		JSONObject pageInfo = parsePcPage(getData(page));
		processPCBanner(page, pageInfo.getJSONObject("PC_BANNER"), BannerType.WEB_CHANNEL_BANNER);
	}

	private void processMobileBanner(Page page) {
		JSONObject pageInfo = parseMobilePage(getData(page));
		processMobileBanner(page, pageInfo.getJSONObject("PHONE_LUNBO"), BannerType.MOBILE_CHANNEL_BANNER);
		processMobileBanner(page, pageInfo.getJSONObject("最新热剧"), BannerType.MOBILE_CHANNEL_RECOMMEND);
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
		if (bannerType == BannerType.MOBILE_CHANNEL_RECOMMEND) {
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

	private void processMain(Page page) {

		Html html = page.getHtml();
		Job oldJob = ((DelayRequest) page.getRequest()).getJob();
		Selectable script = html.xpath("//script").nodes().stream().filter(f -> f.get().contains("posterTvGrid86804"))
				.findFirst().orElse(null);
		if (script == null) {
			return;
		}
		String scriptStr = script.get();
		scriptStr = scriptStr.substring(scriptStr.indexOf("[{"), scriptStr.indexOf("}]") + 2);
		JSONArray array = JSONArray.parseArray(scriptStr);

		if (CollectionUtils.isEmpty(array)) {
			log.error("no get any result");
			return;
		}

		List<Job> jobs = Lists.newArrayList();
		List<Show> shows = Lists.newArrayList();
		List<AutoFindLogs> findLogs = Lists.newArrayList();

		for (int i = 0; i < array.size(); i++) {
			JSONObject o = array.getJSONObject(i);
			saveAutoFindJobAndShow(o, oldJob, jobs, findLogs, shows);
		}

		if (CollectionUtils.isNotEmpty(shows)) {
			putModel(page, shows);
		}
		if (CollectionUtils.isNotEmpty(findLogs)) {
			putModel(page, findLogs);
		}
		if (CollectionUtils.isNotEmpty(jobs)) {
			putModel(page, jobs);
		}
	}

	public void saveAutoFindJobAndShow(JSONObject s, Job old, List<Job> jobs, List<AutoFindLogs> findLogs,
			List<Show> shows) {
		String title = s.getString("title").replaceAll("\\s*", "");
		String href = fixUrl(s.getString("url"));

		String code = "";
		String code2 = FetchCodeEnum.getCode(href + "?source=autoFind");
		if (StringUtils.isNotBlank(code2)) {
			code = "z" + code2;
		}

		if (StringUtils.isBlank(code)) {
			return;
		}

		String url = String.format(DETAIL_URL, code);
		Show show = new Show(title, code, CommonEnum.Platform.YOU_KU.getCode(), 0);
		show.setUrl(url);
		show.setCategory(Category.TV_DRAMA.name());
		show.setSource(3);// 3-代表自动发现的剧
		shows.add(show);

		findLogs.add(
				new AutoFindLogs(title, Category.TV_DRAMA.name(), CommonEnum.Platform.YOU_KU.getCode(), url, code));

		Job newJob = DbEntityHelper.deriveNewJob(old, url);
		newJob.setCode(code);
		jobs.add(newJob);
	}

	private String fixUrl(String href) {
		if (StringUtils.isNotBlank(href) && !href.startsWith("http:")) {
			href = "http:" + href;
		}
		return href;
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
