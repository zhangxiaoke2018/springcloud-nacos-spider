package com.jinguduo.spider.spider.iqiyi;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Maps;
import com.jinguduo.spider.common.util.Md5Util;
import com.jinguduo.spider.spider.youku.HttpClientUtil;
import lombok.extern.apachecommons.CommonsLog;

import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.google.common.collect.Lists;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.constant.FrequencyConstant;
import com.jinguduo.spider.common.exception.AntiSpiderException;
import com.jinguduo.spider.common.exception.PageBeChangedException;
import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.common.util.NumberHelper;
import com.jinguduo.spider.data.table.AdLinkedVideoInfos;
import com.jinguduo.spider.data.table.Show;
import com.jinguduo.spider.data.table.ShowLog;
import com.jinguduo.spider.data.table.VipEpisode;
import com.jinguduo.spider.webmagic.Page;

@Worker
@CommonsLog
public class IqiyiApiSpider extends CrawlSpider {

	private Site site = SiteBuilder.builder().setDomain("cache.video.qiyi.com").build();

	private PageRule rules = PageRule.build()
			.add(".*\\/jp\\/pc\\/.*\\/", page -> pc(page))// 总播放量
			.add("/sdvlst/(?!latest)", page -> sdvlst(page))
			.add("/avlist/", page -> avlist(page))
			.add("latest", page -> latest(page));

	private final String FINAL_COMMENT_URL = "http://api.t.iqiyi.com/qx_api/comment/get_video_comments?need_total=1&sort=hot&page=1&page_size=30&tvid=%s";// tvid
	// 评论老接口
	private final String getCommentUrl = "http://api.t.iqiyi.com/qx_api/comment/get_video_comments?need_total=1&page=1&page_size=30&page_size_reply=3&qypid=01010011010000000000&sort=hot&tvid=%s";
	// 弹幕文本
	private final String BARRAGE_URL = "http://cmts.iqiyi.com/bullet/%s/%s/%s_300_1.z?rn=0.5487082269974053&business=danmu&is_iqiyi=true&is_video_page=true&tvid=%s";
	// 无效url判定
	public final String INVALID_URL = "invalid service url";
	// Iqiyi 返回Code(sucess)标志
	private static String NORMAL_RESCODE = "A00000";

	//热度接口
	private final static String HOT_PLAY_TIMES_URL = "https://pcw-api.iqiyi.com/video/video/hotplaytimes/%s";

	// new ️泡泡分集评论
	private final static String EPISODE_PAOPAO_COMMENT_URL ="https://sns-comment.iqiyi.com/v3/comment/get_comments.action?agent_type=118&agent_version=9.11.5&authcookie=null&business_type=17&content_id=%s&hot_size=10&last_id=&page=1&page_size=10&types=hot,time&callback=jsonp_1551681600905_43514";


	/***
	 * @title 网络剧 分集数据处理，生成抓取评论数量任务
	 * @param page
	 */
	private void avlist(Page page) {
		Job job = ((DelayRequest) page.getRequest()).getJob();
		List<Show> shows = Lists.newArrayList();
		List<Job> jobs = Lists.newArrayList();
		/** 解析为标准格式json串 */
		String resStr = page.getRawText();
		resStr = resStr.substring(resStr.indexOf("=") + 1);

		/** 提取分集网络剧参数 */
		JSONObject resJson = JSON.parseObject(resStr);
		if (!NORMAL_RESCODE.equals(resJson.getString("code"))) {
			throw new PageBeChangedException("please check url");
		}
		JSONArray vlist = resJson.getJSONObject("data").getJSONArray("vlist");
		String rootCode =  resJson.getJSONObject("data").getString("aid");

		for (int i = 0; i < vlist.size(); i++) {
				JSONObject video = vlist.getJSONObject(i);
				Show show = new Show();
				show.setDepth(2);
				show.setParentId(job.getShowId());
				show.setCode(video.getString("id"));
				show.setName(video.getString("shortTitle"));
				show.setReleaseDate(video.getDate("publishTime"));
				show.setPlatformId(job.getPlatformId());
				show.setParentCode(rootCode);
				show.setUrl(video.getString("vurl"));
				// 正片
				if (1 == video.getInteger("type")) {
					show.setEpisode(video.getInteger("pd"));
					shows.add(show);
				}
				Integer vip = video.getInteger("payMark");
				if (vip != null && vip == 1) {
					// vip标志
					putVip(page, job, video.getString("id"));
				}

				if (StringUtils.hasText(video.getString("id"))) {
					// 获取网剧分集播放量任务
					String url = "http://cache.video.qiyi.com/jp/pc/" + video.getString("id") + "/";
					Job newJob = new Job(url);
					// 继承当前Job属性
					DbEntityHelper.derive(job, newJob);
					newJob.setCode(video.getString("id"));
					jobs.add(newJob);

					//todo 获取网剧分集热度任务
//					String hotUrl = String.format(HOT_PLAY_TIMES_URL, video.getString("id"));
//					Job hotJob = new Job(hotUrl);
//					DbEntityHelper.derive(job,hotJob);
//					hotJob.setCode(video.getString("id"));
//					jobs.add(hotJob);
				}
				// create Comment
//				Job newJob = new Job(String.format(getCommentUrl, video.getString("id")));
				Job newJob = new Job(String.format(EPISODE_PAOPAO_COMMENT_URL, video.getString("id")));
				DbEntityHelper.derive(job, newJob);
				newJob.setCode(show.getCode());
				jobs.add(newJob);

				// create Barrage
				String tvId = video.getString("id");
				String firstTag = tvId.substring(tvId.length() - 4, tvId.length() - 2);
				String secondTag = tvId.substring(tvId.length() - 2, tvId.length());

				Job barrageJob = DbEntityHelper.deriveNewJob(job,
						String.format(BARRAGE_URL, firstTag, secondTag, tvId, tvId));
				barrageJob.setCode(show.getCode());
				barrageJob.setFrequency(FrequencyConstant.BARRAGE_TEXT);
				jobs.add(barrageJob);
		}

		if (shows != null && !shows.isEmpty()) {
			putModel(page, shows);
		}
		if (jobs != null && !jobs.isEmpty()) {
			putModel(page, jobs);
		}
	}

	/**
	 * 分集数据处理（包括分级播放量）
	 * 
	 * @param page
	 */
	private void sdvlst(Page page) {
		Job job = ((DelayRequest) page.getRequest()).getJob();
		String s = page.getRawText();

		if (s.contains(INVALID_URL)) {
			throw new PageBeChangedException(s);
		}

		s = s.substring(s.indexOf("=") + 1);
		JSONObject j = JSON.parseObject(s);
		JSONArray data = j.getJSONArray("data");
		if (data == null) {
			return;
		}

		List<Show> shows = Lists.newArrayList();

		// List<ShowLog> showLogs = Lists.newArrayList();
		final int sInt = data.size();

		List<Job> jobList = Lists.newArrayListWithCapacity(sInt);

		for (int i = 0; i < sInt; i++) {
			JSONObject o = data.getJSONObject(i);
			// Long playCount = o.getLong("disCnt");//这个播放量缓存太久，更新不够及时
				Show show = new Show();
				show.setDepth(2);
				show.setParentId(job.getShowId());
				String tvId = o.getString("tvId");
				show.setCode(tvId);

				show.setName(o.getString("videoName"));
				show.setReleaseDate(o.getDate("tvYear"));
				show.setPlatformId(job.getPlatformId());
//				show.setParentCode(job.getCode());
				show.setParentCode(o.getString("faqipuid"));
				show.setUrl(o.getString("vUrl"));
				if (NumberHelper.isNumeric(o.getString("tvYear").replaceAll("[^0-9]", ""))) {
					show.setEpisode(Integer.valueOf(o.getString("tvYear").replaceAll("[^0-9]", "")));
				}

				shows.add(show);

				if (StringUtils.hasText(tvId)) {
					// 获取网剧分集播放量任务
					String url = "http://cache.video.qiyi.com/jp/pc/" + tvId + "/";
					// 继承当前Job属性
					Job newJob = DbEntityHelper.deriveNewJob(job, url);
					newJob.setCode(tvId);
					jobList.add(newJob);

					//获取网剧分集热度任务
//					String hotUrl = String.format(HOT_PLAY_TIMES_URL, tvId);
//					Job hotJob = new Job(hotUrl);
//					DbEntityHelper.derive(job,hotJob);
//					hotJob.setCode(tvId);
//					jobList.add(hotJob);
				}

				Integer vip = o.getInteger("payMark");
				if (vip != null && vip == 1) {
					// vip标志
					putVip(page, job, tvId);
				}

//			Job newJob = new Job(String.format(getCommentUrl, o.getString("faqipuid")));
//			DbEntityHelper.derive(job, newJob);
//			newJob.setCode(o.getString("tvId"));
//			jobList.add(newJob);
			//泡泡分集新评论
			Job newJob = new Job(String.format(EPISODE_PAOPAO_COMMENT_URL,tvId));
			DbEntityHelper.derive(job, newJob);
			newJob.setCode(show.getCode());
			jobList.add(newJob);

			// create Barrage
			String firstTag = tvId.substring(tvId.length() - 4, tvId.length() - 2);
			String secondTag = tvId.substring(tvId.length() - 2, tvId.length());

			Job barrageJob = DbEntityHelper.deriveNewJob(job,
					String.format(BARRAGE_URL, firstTag, secondTag, tvId, tvId));
			barrageJob.setCode(tvId);
			barrageJob.setFrequency(FrequencyConstant.BARRAGE_TEXT);
			jobList.add(barrageJob);
		}

		if (!shows.isEmpty()) {
			putModel(page, shows);
		}
		if (!jobList.isEmpty()) {
			putModel(page, jobList);
		}
	}

	/**
	 * 原创网络剧 pageType = sourceId
	 * 
	 * @param page
	 */
	private void latest(Page page) {

		Job job = ((DelayRequest) page.getRequest()).getJob();

		JSONObject original = JSONObject.parseObject(page.getRawText().replace("var tvInfoJs=", ""));
		String url = page.getUrl().get();
		String tvYear = url.substring(url.indexOf("tvYear") + 7, url.length());
		JSONArray jsonArray = original.getJSONObject("data").getJSONObject(tvYear).getJSONArray("data");

		List<Show> shows = Lists.newArrayList();
		List<Job> jobs = Lists.newArrayList();

		for (int i = 0; i < jsonArray.size(); i++) {
			try {
				JSONObject epi = jsonArray.getJSONObject(i);

				String tvId = epi.getString("tvId");
				String videoName = epi.getString("videoName");

				Show show = new Show();
				show.setDepth(2);
				show.setParentId(job.getShowId());
				show.setCode(tvId);
//				show.setParentCode(job.getCode());
				show.setParentCode(epi.getString("faqipuid"));
				show.setName(videoName);
				show.setPlatformId(job.getPlatformId());
				shows.add(show);
				Integer vip = epi.getInteger("payMark");
				if (vip != null && vip == 1) {
					// vip标志
					putVip(page, job, tvId);
				}
				if (StringUtils.hasText(tvId)) {
					// 获取网剧分集播放量任务
					String url2 = "http://cache.video.qiyi.com/jp/pc/" + tvId + "/";
					Job newJob = new Job(url2);
					// 继承当前Job属性
					DbEntityHelper.derive(job, newJob);
					newJob.setCode(tvId);
					jobs.add(newJob);

					//获取网剧分集热度任务
//					String hotUrl = String.format(HOT_PLAY_TIMES_URL, tvId);
//					Job hotJob = new Job(hotUrl);
//					DbEntityHelper.derive(job,hotJob);
//					hotJob.setCode(tvId);
//					jobs.add(hotJob);

				}

				Job newJob = new Job(String.format(FINAL_COMMENT_URL, tvId));
				DbEntityHelper.derive(job, newJob);
				newJob.setCode(show.getCode());
				jobs.add(newJob);
			} catch (Exception e) {
				log.error(job.getUrl(), e);
			}
		}
		putModel(page, shows);
		putModel(page, jobs);
	}

	/**
	 * 剧集总播放量
	 * 
	 * @param page
	 */
	private void pc(Page page) {
		if (page.getStatusCode() != HttpStatus.OK.value()) {
			return;
		}
		Job job = ((DelayRequest) page.getRequest()).getJob();
		if (job == null) {
			return;
		}

		String s = page.getRawText();
		// 处理服务端返回错误
		if (s.contains("Server") && s.contains("nginx")) {
			throw new AntiSpiderException(s);
		}
		s = s.substring(s.indexOf("=") + 1);
		int i = s.indexOf(":");
		if (i >= 0){
			List<Map<String, String>> j = JSON.parseObject(s, new TypeReference<List<Map<String, String>>>() {
			});
			Map<String, String> dict = j.get(0);
			for (Entry<String, String> entry : dict.entrySet()) {
				Long playCount = NumberHelper.parseLong(entry.getValue(), -1);
				if (playCount == -1) {
					continue;
				}

				ShowLog showLog = new ShowLog();
				showLog.setPlayCount(playCount);
				DbEntityHelper.derive(job, showLog);

				putModel(page, showLog);
			}
		}
	}


	private void putVip(Page page, Job oldJob, String code) {
		// vip标志
		VipEpisode vip = new VipEpisode();
		vip.setCode(code);
		vip.setPlatformId(oldJob.getPlatformId());
		putModel(page, vip);
	}

	@Override
	public PageRule getPageRule() {
		return rules;
	}

	@Override
	public Site getSite() {
		return site;
	}




	public static void main(String[] args){

		long time = System.currentTimeMillis();

		String domain = "http://comic-data.if.iqiyi.com";

		String servletpath = "/v1/guduo/comic/stat";

		String query = "page=1&size=20&timeStamp="+time+"&day=2020-12-16";

		String key ="TaC;1x#U79L2";

		String sign = Md5Util.getMd5(servletpath+query+key);


		Map<String,String> header = Maps.newHashMap();
		header.put("aclName","guduo");
		header.put("sign",sign);
		String s = HttpClientUtil.sendGetRequest(domain + servletpath + "?" + query, header);

		System.out.println(s);
	}
}
