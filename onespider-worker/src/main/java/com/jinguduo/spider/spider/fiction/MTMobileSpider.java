package com.jinguduo.spider.spider.fiction;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import com.alibaba.fastjson.JSONObject;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.constant.CommonEnum.FictionChannel;
import com.jinguduo.spider.common.constant.CommonEnum.Platform;
import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.common.util.Md5Util;
import com.jinguduo.spider.data.table.Fiction;
import com.jinguduo.spider.data.table.FictionCommentLogs;
import com.jinguduo.spider.webmagic.Page;

import lombok.extern.apachecommons.CommonsLog;

@Worker
@CommonsLog
public class MTMobileSpider extends CrawlSpider {

	
	private Site site = SiteBuilder.builder().setDomain("app.motie.com").build();
	private PageRule rules = PageRule.build().add("^http:\\/\\/app\\.motie\\.com$", this::processEntrance)
			.add("/android/2/ranking/2017", this::processAllRank)
			.add("/android/2/ranking_detail/2017", this::processRank)
			.add("/android/2/book/extend/", this::processDetail);

	private static final String HOST= "http://app.motie.com";
	private static final String PATH_RANK = "http://app.motie.com/android/2/ranking/2017?group=%d";
	private static final String PATH_DETAIL = "http://app.motie.com/android/2/book/extend/%s";
	private static final String PATH_DASHANG = "http://www.motie.com/book/%s/donate/list";

	public void processEntrance(Page page) {
		for (int i = 1; i < 3; i++) {
			createJob(page, String.format(PATH_RANK, i), false);
		}
	}

	public void processAllRank(Page page) {
		List<String> ranks = page.getHtml().getDocument().getElementsByAttribute("data-url").eachAttr("data-url");
		if (CollectionUtils.isEmpty(ranks))
			return;
		for (String rank : ranks)
			createJob(page, HOST+rank, false);
	}

	public void processRank(Page page) {
		List<String> bookIds = page.getHtml().links().regex(".*\\/book\\/(\\d+)", 1).all();
		if (CollectionUtils.isEmpty(bookIds))
			return;

		for (String bookId : bookIds) {
			createJob(page, String.format(PATH_DETAIL, bookId), true);
			createJob(page, String.format(PATH_DASHANG, bookId), false);
		}
	}

	public void processDetail(Page page) {
		JSONObject response = page.getJson().toObject(JSONObject.class);
		if (response != null && response.getInteger("error_no") == 0 && response.getInteger("result") == 1) {
			JSONObject data = response.getJSONObject("data");
			Fiction fiction = new Fiction();
			fiction.setPlatformId(Platform.MO_TIE.getCode());
			fiction.setTags(data.getString("sortName"));
			String code = data.getString("id");
			fiction.setCode(code);
			fiction.setTotalLength(data.getInteger("words"));
			fiction.setIntro(data.getString("introduce"));
			fiction.setCover(data.getString("icon"));
			fiction.setIsFinish(data.getBoolean("finished") ? 1 : 0);
			fiction.setName(data.getString("name"));
			fiction.setAuthor(data.getString("authorName"));
			fiction.setChannel(
					data.getInteger("group") == 2 ? FictionChannel.GIRL.getCode() : FictionChannel.BOY.getCode());
			putModel(page, fiction);

			FictionCommentLogs commentLogs = new FictionCommentLogs();
			commentLogs.setPlatformId(Platform.MO_TIE.getCode());
			commentLogs.setCode(code);
			
			if(data.containsKey("reviewListCount"))
				commentLogs.setCommentCount(data.getInteger("reviewListCount"));
			putModel(page, commentLogs);
		}
	}

	private void createJob(Page page, String url, boolean post) {
		Job oldJob = ((DelayRequest)page.getRequest()).getJob();
		Job job = new Job(url);
		DbEntityHelper.derive(oldJob, job);
		job.setMethod(post ? "POST" : "GET");
		job.setPlatformId(Platform.MO_TIE.getCode());
		job.setCode(Md5Util.getMd5(url));
		putModel(page, job);
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
