package com.jinguduo.spider.spider.iqiyi;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.config.CookieSpecs;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.google.common.collect.Maps;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.spider.listener.UserAgentSpiderListener;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.constant.CommonEnum.BannerType;
import com.jinguduo.spider.common.constant.CommonEnum.Platform;
import com.jinguduo.spider.common.constant.FrequencyConstant;
import com.jinguduo.spider.common.exception.PageBeChangedException;
import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.common.util.Md5Util;
import com.jinguduo.spider.common.util.RegexUtil;
import com.jinguduo.spider.data.table.BannerRecommendation;
import com.jinguduo.spider.data.table.ShowCategoryCode;
import com.jinguduo.spider.data.table.VipEpisode;
import com.jinguduo.spider.webmagic.Page;

@Worker
@Slf4j
public class IqiyiPageSpider extends CrawlSpider {

	// 网剧所有剧集Api
	private final static String TELEPLAY_ALL_LIST_URL = "http://cache.video.qiyi.com/jp/avlist/%s/?albumId=%s";// albumId
	// 综艺所有剧集Api
	private final static String ZONGYI_ALL_LIST_URL = "http://cache.video.qiyi.com/jp/sdvlst/%s/%s/";// cid,sourceId
	// 评论url(电影用-老接口)
	private final String getCommentUrl = "http://api.t.iqiyi.com/qx_api/comment/get_video_comments?need_total=1&page=1&page_size=30&page_size_reply=3&qypid=01010011010000000000&sort=hot&tvid=%s";
	// get_feeds 对应泡泡评论量抓取
	private static final String PAOPAO_COMMENT_COUNT_URL = "http://paopao.iqiyi.com/apis/e/starwall/basic_wall.action?authcookie=&device_id=pc_web&agenttype=118&wallId=%s&atoken=8ffffbc44F3tKBShRo5tC9Bm1J5k01EeIqm1jnhIKXLdVMRB2m11Ctom4";

	private final static String SOURCE_ID_DRAMA_LIST_URL = "http://cache.video.qiyi.com/jp/sdvlst/latest?key=sdvlist&categoryId=%s&sourceId=%s&tvYear=%s";// categoryId,sourceId,tvYear

	// private final static String PLAY_COUNT_URL =
	// "http://cache.video.qiyi.com/jp/pc/%s/";
	// 2018-09-06 13:20:45
	// 精确的
	private final static String PLAY_COUNT_URL = "http://paopao.iqiyi.com/apis/e/starwall/home.action?m_device_id=358239054455227&agenttype=115&atoken=4437fyrzkGxZlyL8EM2aaNZPSPd3EfLGm3vboGsdkam3Js2REUm4&playPlatform=10&version=1&wallId=%s";

	private final static String HOT_PLAY_TIMES_URL = "https://pcw-api.iqiyi.com/video/video/hotplaytimes/%s";

	// new ️泡泡分集评论
	private final static String EPISODE_PAOPAO_COMMENT_URL = "https://sns-comment.iqiyi.com/v3/comment/get_comments.action?agent_type=118&agent_version=9.11.5&authcookie=null&business_type=17&content_id=%s&hot_size=10&last_id=&page=1&page_size=10&types=hot,time&callback=jsonp_1551681600905_43514";

	private final static String WEB_CHANNEL_BANNER = "https://www.iqiyi.com/dianshiju/#WEB_CHANNEL_BANNER";
	private static final String MOBILE_HOME_BANNER = "https://m.iqiyi.com#MOBILE_HOME_BANNER";
	private final static String MOBILE_CHANNEL_BANNER = "https://m.iqiyi.com/dianshiju/#MOBILE_CHANNEL_BANNER";
	
	private Site site = SiteBuilder.builder().setDomain("www.iqiyi.com").setCookieSpecs(CookieSpecs.IGNORE_COOKIES)
			.addHeader("Connection", "keep-alive") // 无此Header可能会返回404状态码
			// user-agent 动态化
			.addSpiderListener(new UserAgentSpiderListener()).build();

	private PageRule rules = PageRule.build().add(".", page -> processMain(page));

	final static Pattern PATTERN_SUBDOMAIN = Pattern.compile("^(?:http|https)://(\\w+)\\.iqiyi\\.com");

	private void processMain(Page page) {

		// banner entry
		boolean bannerProcessed = bannerEntryJob(page);

		if (bannerProcessed) {
			return;
		}

		Job oldJob = ((DelayRequest) page.getRequest()).getJob();

		Map<String, String> pageInfo = getPageInfo(page);// 获取剧集信息script
		if (MapUtils.isEmpty(pageInfo)) {
			throw new PageBeChangedException("IqiyiPageSpider processMain can't get pageInfo");
		}

		// banner job
		bannerProcessed = bannerJob(page, pageInfo);
		if (bannerProcessed) {
			return;
		}

		// 总播放量 job
		totalPlayCountJob(page, oldJob, pageInfo);
		// 总热度 job
		// TODO: 2018/9/3 标记
		totalHotPlayTimesJob(page, oldJob, pageInfo);

		log.info("Iqiyi shows start seccess! code" + oldJob.getCode());

		// 分集剧集 job
		allListShowJob(page, oldJob, pageInfo);
		// 电影及泡泡评论文本以及评论量 job
		createCommentJob(page, oldJob, pageInfo);
		// 弹幕 job
		createBarrageJob(page, oldJob, pageInfo);
		// vip 标记
		vipMark(page, oldJob, pageInfo);
	}

	private boolean bannerEntryJob(Page page) {
		if(!page.getUrl().get().equals("https://www.iqiyi.com#WEB_HOME_BANNER")
				&&!page.getUrl().get().equals("https://www.iqiyi.com/dianshiju/#WEB_CHANNEL_BANNER"))
			return false;
			
		String tag = page.getUrl().regex("#(WEB_HOME_BANNER|WEB_CHANNEL_BANNER)").get();
		if (StringUtils.isEmpty(tag))
			return false;

		BannerType bannerType = BannerType.valueOf(tag);

		if (bannerType == BannerType.WEB_HOME_BANNER) {
			createUrlJob(page, WEB_CHANNEL_BANNER);
			createUrlJob(page, MOBILE_CHANNEL_BANNER);
			createUrlJob(page, MOBILE_HOME_BANNER);
		}

		List<String> links = page.getHtml().xpath("//div[@id='block-B']//ul[@class='focus-index-list' or @class='img-list'][1]/li//a/@href").all();
		if (null != links)links.forEach(l -> createUrlJob(page, fixBannerUrl(l, bannerType)));
		return true;
	}

	private String fixBannerUrl(String url, BannerType bannerType) {
		if (StringUtils.isEmpty(url))
			return url;
		url = !url.startsWith("https:") && !url.startsWith("http:") ? "https:" + url : url;
		int questionMarkIdx = url.indexOf("?");
		if(questionMarkIdx>-1)
			url = url.substring(0,questionMarkIdx);
		url = url + "#" + bannerType.name();
		return url;
	}

	private void createUrlJob(Page page, String url) {
		Job job = new Job(url);
		job.setPlatformId(Platform.I_QI_YI.getCode());
		job.setCode(Md5Util.getMd5(url));
		putModel(page, job);
	}

	private boolean bannerJob(Page page, Map<String, String> pageInfo) {
		String tag = page.getUrl().regex("#(" + BannerType.WEB_HOME_BANNER.name() 
		+ "|" + BannerType.WEB_CHANNEL_BANNER.name() 
		+ "|"+ BannerType.MOBILE_HOME_BANNER.name() 
		+ "|"+ BannerType.MOBILE_CHANNEL_BANNER.name() 
		+ "|"+ BannerType.MOBILE_CHANNEL_RECOMMEND.name() 
		+ "|" + BannerType.MOBILE_HOME_RECOMMEND.name() + ")").get();
		if (StringUtils.isEmpty(tag)) {
			return false;
		}
		BannerType bannerType = BannerType.valueOf(tag);
		
		String albumId = pageInfo.get("albumId");
		if (!StringUtils.isEmpty(albumId)&&Integer.valueOf(albumId)>0) {
			BannerRecommendation br = new BannerRecommendation(albumId, Platform.I_QI_YI.getCode(), bannerType);
			putModel(page, br);
		}
		return true;
	}

	private void totalHotPlayTimesJob(Page page, Job oldJob, Map<String, String> pageInfo) {
		String id = this.findtotalHotPlayTimesId(pageInfo);
		Job newJob = DbEntityHelper.deriveNewJob(oldJob, String.format(HOT_PLAY_TIMES_URL, id));
		newJob.setFrequency(FrequencyConstant.DEFAULT);
		putModel(page, newJob);
	}

	// 和总播放量的id获取方式一样。改变命名，方便调试
	private String findtotalHotPlayTimesId(Map<String, String> pageInfo) {
		String id = pageInfo.get("sourceId");
		if (StringUtils.isNotBlank(id) && !"0".equals(id)) {
			return id;
		}
		id = pageInfo.get("albumId");
		if (StringUtils.isNotBlank(id) && !"0".equals(id)) {
			return id;
		}
		id = pageInfo.get("tvId");
		if (StringUtils.isNotBlank(id) && !"0".equals(id)) {
			return id;
		}
		throw new PageBeChangedException("IqiyiPageSpider TotalPlayCountJob can not get id");
	}

	// 评论相关job
	private void createCommentJob(Page page, Job oldJob, Map<String, String> pageInfo) {
		// 电影评论量 job
		// if
		// (ShowCategoryCode.IqiyiCategoryEnum.FILM.getCode().equals(pageInfo.get("cid")))
		// {
		// Job newJob = DbEntityHelper.deriveNewJob(oldJob, String.format(getCommentUrl,
		// pageInfo.get("albumId")));
		// putModel(page, newJob);
		// }

		String tvId = pageInfo.get("tvId");
		String wallId = pageInfo.get("wallId");

		if (ShowCategoryCode.IqiyiCategoryEnum.FILM.getCode().equals(pageInfo.get("cid"))) {
			Job newJob = DbEntityHelper.deriveNewJob(oldJob, String.format(EPISODE_PAOPAO_COMMENT_URL, tvId));
			putModel(page, newJob);
		}

		if ("4".equals(pageInfo.get("cid")) && StringUtils.isEmpty(wallId)) { // 动漫网络电影
			// Job newJob = DbEntityHelper.deriveNewJob(oldJob, String.format(getCommentUrl,
			// pageInfo.get("albumId")));
			// putModel(page, newJob);
			Job newJob = DbEntityHelper.deriveNewJob(oldJob, String.format(EPISODE_PAOPAO_COMMENT_URL, tvId));
			putModel(page, newJob);
		}

		if (StringUtils.isEmpty(wallId)) {
			return;
		}

		// 生成泡泡评论量爬取的任务
		Job paopaoJob = DbEntityHelper.deriveNewJob(oldJob, String.format(PAOPAO_COMMENT_COUNT_URL, wallId));// code
																												// 关联深度为1的job
		paopaoJob.setFrequency(FrequencyConstant.COMMENT_COUNT);
		putModel(page, paopaoJob);

		// 生成泡泡评论文本爬取任务
		String url = "http://api.t.iqiyi.com/feed/get_feeds?agenttype=118&wallId=" + wallId
				+ "&feedTypes=1%2C7&count=20&top=1&baseTvId=" + tvId + "&feedId=";
		Job paopaoCommentContentJob = DbEntityHelper.deriveNewJob(oldJob, url);// code
																				// 关联深度为1的job
		paopaoCommentContentJob.setFrequency(FrequencyConstant.COMMENT_TEXT);
		putModel(page, paopaoCommentContentJob);
	}

	// 总播放量相关job
	private void totalPlayCountJob(Page page, Job oldJob, Map<String, String> pageInfo) {
		String wallId = pageInfo.get("wallId");
		if (StringUtils.isNotBlank(wallId) && !"0".equals(wallId)) {
			Job newJob = DbEntityHelper.deriveNewJob(oldJob, String.format(PLAY_COUNT_URL, wallId));
			newJob.setFrequency(FrequencyConstant.DEFAULT);
			putModel(page, newJob);
		}

	}

	// 所有剧集api job
	private void allListShowJob(Page page, Job oldJob, Map<String, String> pageInfo) {
		String sourceId = null, url = null, albumId = null;
		String cid = pageInfo.get("cid");
		/** 按照视频类型区分获取所有的剧集的链接 */
		if (StringUtils.isEmpty(cid)) {
			throw new PageBeChangedException("The cid is null");
		}
		// 综艺、网剧
		if (ShowCategoryCode.IqiyiCategoryEnum.ZONGYI.getCode().contains(cid)) {
			// 2018-08-13 17:19:19 爱奇艺改版不再使用 sourceId -> albumId
			sourceId = pageInfo.get("albumId");
			if (StringUtils.isEmpty(sourceId)) {
				return;
			}
			url = String.format(ZONGYI_ALL_LIST_URL, cid, sourceId);
		} else if (ShowCategoryCode.IqiyiCategoryEnum.TALK_SHOW.getCode().equals(cid)) {
			sourceId = pageInfo.get("sourceId");
			if (StringUtils.isEmpty(sourceId)) {
				return;
			}
			url = String.format(ZONGYI_ALL_LIST_URL, cid, sourceId);
		} else if (ShowCategoryCode.IqiyiCategoryEnum.ORIGINAL_TELEPLAY.getCode().equals(cid)) {// 原创网络剧
																								// 分集同综艺
			sourceId = pageInfo.get("sourceId");
			if (StringUtils.isEmpty(sourceId)) {
				return;
			}
			// 如果source为0，则此原创网络剧应该按网络剧的方式去抓分集
			if (StringUtils.equals(sourceId, "0")) {
				albumId = pageInfo.get("albumId");
				if (StringUtils.isEmpty(albumId)) {
					return;
				}
				url = String.format(TELEPLAY_ALL_LIST_URL, albumId, albumId);
			} else {
				url = String.format(ZONGYI_ALL_LIST_URL, cid, sourceId);
			}
		} else if (ShowCategoryCode.IqiyiCategoryEnum.TELEPLAY.getCode().equals(cid)) {
			albumId = pageInfo.get("albumId");
			if (StringUtils.isEmpty(albumId)) {
				return;
			}
			url = String.format(TELEPLAY_ALL_LIST_URL, albumId, albumId);
		} else if (pageInfo.get("pageType") != null && pageInfo.get("pageType").equals("sourceId")) {
			sourceId = pageInfo.get("sourceId");
			String tvYear = pageInfo.get("tvYear");
			url = String.format(SOURCE_ID_DRAMA_LIST_URL, cid, sourceId, tvYear);
		} else {
			albumId = pageInfo.get("albumId");
			if (StringUtils.isEmpty(albumId)) {
				return;
			}
			url = String.format(TELEPLAY_ALL_LIST_URL, albumId, albumId);
		}
		Job newJob = DbEntityHelper.deriveNewJob(oldJob, url);
		putModel(page, newJob);
	}

	private String findTotalPcId(Map<String, String> pageInfo) {
		String id = pageInfo.get("sourceId");
		if (StringUtils.isNotBlank(id) && !"0".equals(id)) {
			return id;
		}
		id = pageInfo.get("albumId");
		if (StringUtils.isNotBlank(id) && !"0".equals(id)) {
			return id;
		}
		id = pageInfo.get("tvId");
		if (StringUtils.isNotBlank(id) && !"0".equals(id)) {
			return id;
		}
		throw new PageBeChangedException("IqiyiPageSpider TotalPlayCountJob can not get id");
	}

	// 截取出带有剧信息的script json map
	// 爱奇艺部分页面改版，不区分类型
	// 并从页面获取泡泡评论所需的wallId参数
	private Map<String, String> getPageInfo(Page page) {
		List<String> scripts = page.getHtml().xpath("//script").all();
		Map<String, String> pageInfo = Maps.newHashMap();
		for (String s : scripts) {
			s = StringUtils.replaceAll(s, " ", "");
			if ((s.contains("albumId:") || s.contains("\"albumId\":")) && !s.contains("info.albumId")) {
				int idx = s.indexOf("playPageInfo=");
				if (idx > 0) {
					s = s.substring(s.indexOf('{', idx), s.indexOf("}", idx) + 1);
					s = s.replace("|| {}", "");
					try {
						Map<String, String> pi = JSON.parseObject(s, new TypeReference<Map<String, String>>() {
						});
						pageInfo.putAll(pi);
					} catch (Exception e) {
						throw new PageBeChangedException(s);
					}
				}
			} else if (s.contains("playPageInfo['wallId']")) {
				String wallId = RegexUtil.getDataByRegex(s, "playPageInfo\\['wallId'\\].*?\"(\\d+)\"", 1);
				pageInfo.put("wallId", wallId);
			} else if (s.contains("param['vid']")) {
				String vId = RegexUtil.getDataByRegex(s, "param\\['vid'\\].*?\"([0-9a-fA-F]+)\"");
				pageInfo.put("vid", vId);
			}
		}
		return pageInfo;
	}

	public void createBarrageJob(Page page, Job oldJob, Map<String, String> pageInfo) {
		String tvId = pageInfo.get("tvId");
		if (StringUtils.isBlank(tvId) || tvId.length() < 4) {
			return;
		}
		String firstTag = tvId.substring(tvId.length() - 4, tvId.length() - 2);
		String secondTag = tvId.substring(tvId.length() - 2, tvId.length());
		String url = "http://cmts.iqiyi.com/bullet/" + firstTag + "/" + secondTag + "/" + tvId
				+ "_300_1.z?rn=0.5487082269974053&business=danmu&is_iqiyi=true&is_video_page=true&tvid=" + tvId + "";

		Job newJob = DbEntityHelper.deriveNewJob(oldJob, url);
		newJob.setFrequency(FrequencyConstant.BARRAGE_TEXT);
		newJob.setCode(tvId);
		putModel(page, newJob);
	}

	// vip标记,这里主要针对电影在这判断，分集判断会在接口中标识payMark
	private void vipMark(Page page, Job oldJob, Map<String, String> pageInfo) {
		String payBtn = page.getHtml().$(".play_right_topArea").$("#movielistpaybtn").get();
		String cid = pageInfo.get("cid");
		// Boolean feiZhengPian =
		// Boolean.valueOf(pageInfo.get("isfeizhengpian"));//网大用这个字段判断是否整片,一般vi是true,片花和非vip是false
		if (ShowCategoryCode.IqiyiCategoryEnum.FILM.getCode().equals(cid) && StringUtils.isNotBlank(payBtn)) {
			VipEpisode vip = new VipEpisode();
			vip.setCode(oldJob.getCode());
			vip.setPlatformId(oldJob.getPlatformId());
			putModel(page, vip);
		}
	}

	@Override
	public Site getSite() {
		return site;
	}

	@Override
	public PageRule getPageRule() {
		return this.rules;
	}
}
