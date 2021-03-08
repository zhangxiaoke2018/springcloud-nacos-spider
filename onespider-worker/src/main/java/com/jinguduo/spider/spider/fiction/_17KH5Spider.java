package com.jinguduo.spider.spider.fiction;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.ImmutableMap;
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
import com.jinguduo.spider.data.table.FictionPlatformFavorite;
import com.jinguduo.spider.data.table.FictionPlatformRecommend;
import com.jinguduo.spider.webmagic.Page;

import lombok.extern.apachecommons.CommonsLog;

@Worker
@CommonsLog
public class _17KH5Spider extends CrawlSpider {
	private Site site = SiteBuilder.builder().setDomain("h5.17k.com").build();

	private PageRule rules = PageRule.build()
			.add("https:\\/\\/h5\\.17k\\.com$", this::processEntrance)
			.add("/rank", this::processRank)
			.add("/original",this::processDetail)
			.add("/databox", this::processAttri);

	private String rankUrl = "https://h5.17k.com/ck/book/rank?orderBy=%d&classId=%d&page=%d&num=100";
	private String detailUrl = "https://h5.17k.com/ck/book/%d/original";
	private String attributeUrl = "https://h5.17k.com/ck/book/%d/databox";
	private int[] orders = { 1, 2, 3 };
	private Map<Integer, Integer> class2Channel = ImmutableMap.of(2, FictionChannel.BOY.getCode(), 3,
			FictionChannel.GIRL.getCode());

	private void processEntrance(Page page) {
		for(Integer classId:class2Channel.keySet()) {
			for(int order:orders) {
				for(int pageId = 1;pageId<4;pageId++) {
					String url = String.format(rankUrl, order,classId,pageId);
					createJob(page, url);
					createJob(page, url+"&newBook=1");
				}
			}
		}
		
	}

	
	
	private void processRank(Page page) {
		JSONObject ob = page.getJson().toObject(JSONObject.class);
		if(ob!=null&&ob.containsKey("data")) {
			JSONArray data = ob.getJSONArray("data");
			JSONObject item;
			for(int i =0,size = data.size();i<size;i++) {
				item = data.getJSONObject(i);
				createJob(page, String.format(detailUrl, item.getLong("id")));
				createJob(page, String.format(attributeUrl, item.getLong("id")));
			}
		}
	}
	
	private void processDetail(Page page) {
		JSONObject ob = page.getJson().toObject(JSONObject.class);
		if(ob!=null&&ob.containsKey("data")) {
			JSONObject item = ob.getJSONObject("data");
			Fiction fiction = new Fiction();
			fiction.setPlatformId(Platform._17_K.getCode());
			fiction.setCode(item.getString("id"));
			fiction.setTotalLength(item.getInteger("wordCount"));
			fiction.setName(item.getString("bookName"));
			fiction.setAuthor(item.getString("authorPenName"));
			fiction.setChannel(class2Channel.get(item.getJSONObject("bookClass").getInteger("id")));
			
			Set<String> tags = new HashSet<>();
			tags.add(item.getJSONObject("bookChannel").getString("name"));
			tags.addAll(item.getJSONArray("keyword").toJavaList(String.class));
			fiction.setTags(String.join("/", tags));
			String intro = item.getString("introduction");
			if (StringUtils.isNoneEmpty(intro))
				intro = intro.replaceAll("\r|\n", "");

			fiction.setIntro(intro);
			fiction.setCover(item.getString("coverImg"));
			fiction.setIsFinish(item.getJSONObject("bookStatus").getInteger("id")==3?1:0);
			putModel(page,fiction);
		}
	}
	
	private void processAttri(Page page) {
		JSONObject ob = page.getJson().toObject(JSONObject.class);
		if(ob!=null&&ob.containsKey("data")) {
			JSONObject item = ob.getJSONObject("data");
			int platformId = Platform._17_K.getCode();
			String code = item.getString("id");
			

			FictionCommentLogs comment = new FictionCommentLogs();
			comment.setPlatformId(platformId);
			comment.setCode(code);
			comment.setCommentCount(item.getInteger("commentCount"));
			putModel(page,comment);
			
			FictionPlatformClick click = new FictionPlatformClick();
			click.setPlatformId(platformId);
			click.setCode(code);
			click.setClickCount(item.getLong("pv"));
			putModel(page,click);
			
			FictionPlatformFavorite favorite = new FictionPlatformFavorite();
			favorite.setPlatformId(platformId);
			favorite.setCode(code);
			favorite.setFavoriteCount(item.getInteger("favoriteCount"));
			putModel(page,favorite);
			
			FictionPlatformRecommend recommend = new FictionPlatformRecommend();
			recommend.setPlatformId(platformId);
			recommend.setCode(code);
			recommend.setRecommendCount(item.getInteger("recommentTicket"));
			putModel(page,recommend);
			
			FictionIncomeLogs income = new FictionIncomeLogs();
			income.setIncomeId(FictionIncome._17K_HONGBAO.getCode());
			income.setCode(code);
			income.setIncomeNum(item.getInteger("rewardAmount")/100);
			putModel(page,income);
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
		Job oldJob = ((DelayRequest) page.getRequest()).getJob();
		Job job = new Job(url);
		DbEntityHelper.derive(oldJob, job);
		job.setCode(Md5Util.getMd5(url));
		job.setPlatformId(Platform._17_K.getCode());
		putModel(page, job);
	}
}
