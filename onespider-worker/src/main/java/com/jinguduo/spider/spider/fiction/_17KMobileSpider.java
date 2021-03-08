package com.jinguduo.spider.spider.fiction;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.constant.CommonEnum.FictionChannel;
import com.jinguduo.spider.common.constant.CommonEnum.FictionIncome;
import com.jinguduo.spider.common.constant.CommonEnum.Platform;
import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.common.util.Md5Util;
import com.jinguduo.spider.data.table.Fiction;
import com.jinguduo.spider.data.table.FictionCommentLogs;
import com.jinguduo.spider.data.table.FictionIncomeLogs;
import com.jinguduo.spider.data.table.FictionPlatformClick;
import com.jinguduo.spider.webmagic.Page;

import lombok.extern.apachecommons.CommonsLog;

//@Worker
//@CommonsLog
public class _17KMobileSpider extends CrawlSpider {

	private Site site = SiteBuilder.builder().setDomain("api.17k.com").build();
	private PageRule rules = PageRule.build().add("^http:\\/\\/api\\.17k\\.com$", this::processEntrance)
			.add("/rank", this::processRank)
			.add("/comment/index/top", this::processComment)
			.add("^http:\\/\\/api\\.17k\\.com\\/v2\\/book\\/\\d+\\?app_key=\\d+$", this::processDetail);
	private static final int[] ranks = { 1, 4, 5, 3 };
	private static final int[] gender = { 2, 3 };
	private static final String PATH_RANK = "http://api.17k.com/v2/book/rank?app_key=1351550300&type=%d&site=%d&page=%d&_versions=960&_filter_data=1&channel=2&merchant=17KWeb&_access_version=2";
	private static final String PATH_COMMENT = "http://api.17k.com/v2/book/%s/comment/index/top?app_key=1351550300";
	private static final String PATH_DETAIL = "http://api.17k.com/v2/book/%s?app_key=1351550300";
	private static final String PATH_INCOME = "http://api.ali.17k.com/v2/book/%s/stat_info?app_key=3362611833";

	public void processEntrance(Page page) {
		for (int rank : ranks) {
			for (int g : gender) {
				for (int i = 1; i < 11; i++) {
					createJob(page, String.format(PATH_RANK, rank, g, i));
				}
			}
		}
	}

	private void createJob(Page page, String url) {
		Job oldJob = ((DelayRequest) page.getRequest()).getJob();
		Job job = new Job(url);
		DbEntityHelper.derive(oldJob, job);
		job.setCode(Md5Util.getMd5(url));
		job.setPlatformId(Platform._17_K.getCode());
		putModel(page, job);
	}

	public void processRank(Page page) {
		JSONObject response = page.getJson().toObject(JSONObject.class);
		if (response != null && response.getJSONObject("status").getInteger("code") == 0) {
			JSONArray data = response.getJSONArray("data");
			JSONObject item;
			for (int i = 0, len = data.size(); i < len; i++) {
				item = data.getJSONObject(i);
				Fiction fiction = new Fiction();
				fiction.setTags(item.getString("category_name_1"));
				fiction.setName(item.getString("book_name"));
				fiction.setCode(item.getString("book_id"));
				fiction.setIsFinish(item.getInteger("finish") == 2 ? 1 : 0);
				fiction.setAuthor(item.getString("author_name"));
				fiction.setCover(item.getString("cover"));
				String intro = item.getString("intro");
				if (StringUtils.isNoneEmpty(intro))
					intro = intro.replaceAll("\r\n", "");

				fiction.setIntro(intro);
				fiction.setTotalLength(item.getInteger("word_count"));
				fiction.setChannel(
						item.getInteger("site") == 2 ? FictionChannel.BOY.getCode() : FictionChannel.GIRL.getCode());
				fiction.setPlatformId(Platform._17_K.getCode());
				putModel(page, fiction);

				createJob(page, String.format(PATH_COMMENT, item.getString("book_id")));
				createJob(page, String.format(PATH_DETAIL, item.getString("book_id")));
				createJob(page, String.format(PATH_INCOME,  item.getString("book_id")));
			}

		}
	}

	public void processComment(Page page) {
		JSONObject response = page.getJson().toObject(JSONObject.class);
		if (response != null && response.getJSONObject("status").getInteger("code") == 0) {
			String bookId = page.getUrl().regex(".*\\/([0-9]+)\\/comment\\/index\\/top.*").get();
			if (response.containsKey("total_num")) {
				int comment = response.getIntValue("total_num");
				FictionCommentLogs commentLogs = new FictionCommentLogs();
				commentLogs.setCode(bookId);
				commentLogs.setPlatformId(Platform._17_K.getCode());
				commentLogs.setCommentCount(comment);
				putModel(page, commentLogs);
			}
		}
	}

	public void processIncome(Page page) {
		String bookId = page.getUrl().regex(".*\\/book\\/(\\d+)\\/donate", 1).get();

		String incomeNumberStr = page.getHtml().xpath("span[@class=\"comment_tit_motiecoin\"]").regex(".*总计(\\d+).*")
				.get();

		FictionIncomeLogs incomeLogs = new FictionIncomeLogs();
		incomeLogs.setCode(bookId);
		incomeLogs.setIncomeId(FictionIncome.MOTIE_DASHANG.getCode());
		if (StringUtils.isNoneBlank(incomeNumberStr))
			incomeLogs.setIncomeNum(Integer.valueOf(incomeNumberStr));
		putModel(page, incomeLogs);
	}
	
	private void processDetail(Page page) {
		String bookId = page.getUrl().regex(".*\\/v2\\/book\\/([0-9]+)\\?.*").get();
		JSONObject response = page.getJson().toObject(JSONObject.class);
		if(Integer.valueOf(0) ==response.getJSONObject("status").getInteger("code")) {
			JSONObject data = response.getJSONObject("data");
			if(data.containsKey("total_click")) {
				long totalClick = data.getLongValue("total_click");
				FictionPlatformClick click = new FictionPlatformClick();
				click.setClickCount(totalClick);
				click.setPlatformId(Platform._17_K.getCode());
				click.setCode(bookId);
				putModel(page,click);
			}
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
