package com.jinguduo.spider.spider.fiction;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.lang3.StringUtils;

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
import com.jinguduo.spider.data.table.FictionPlatformRecommend;
import com.jinguduo.spider.webmagic.Page;

import lombok.extern.apachecommons.CommonsLog;

@CommonsLog
@Worker
public class ZHApiSpider extends CrawlSpider {
	private static final String key = "082DE6CF1178736AF28EB8065CDBE5AC";
	private static final String PARAM_COMMENT = "api_key=27A28A4D4B24022E543E&apn=wlan&appId=ZHKXS&brand=Netease&channelId=A1002&channelType=H5&clientVersion=4.6.0.21&forumId=%s&installId=1dc77f12094bda897f42000c001e03bf&model=MuMu&modelName=cancro&os=android&osVersion=19&preChannelId=A1002&screenH=1280&screenW=720&type=1&userId=0";
	private static final String PATH_COMMENT = "https://api1.zongheng.com/api/forum/detail?";

	private static final String PARAM_DETAIL = "api_key=27A28A4D4B24022E543E&apn=wlan&appId=ZHKXS&bookId=%s&brand=Netease&channelId=A1002&channelType=H5&clientVersion=4.6.0.21&installId=1dc77f12094bda897f42000c001e03bf&model=MuMu&modelName=cancro&os=android&osVersion=19&preChannelId=A1002&screenH=1280&screenW=720&userId=0";
	private static final String PATH_EXTRA_INFO = "https://api1.zongheng.com/api/book/extraBookInfo?";

	private Site site = SiteBuilder.builder().setDomain("api1.zongheng.com").build();

	private PageRule rules = PageRule.build().add("/api/book/bookInfo", this::processDetail)
			.add("/api/book/extraBookInfo", this::processExtraInfo).add("/api/forum/detail", this::processComment);
	private static final String PATH_CHAPTER= "http://book.zongheng.com/showchapter/%s.html";
	private static final String PATH_CHAPTER2 = "https://m.zongheng.com/h5/ajax/chapter/list?h5=1&bookId=%s&pageNum=1&pageSize=%s";

	public void processDetail(Page page) {
		JSONObject response = page.getJson().toObject(JSONObject.class);
		if (response != null && response.getInteger("code") == 200) {
			JSONObject result = response.getJSONObject("result");
			String bookId = result.getString("bookId");
			Fiction fiction = new Fiction();
			fiction.setName(result.getString("name"));
			fiction.setTags(result.getString("categoryName"));
			fiction.setCode(bookId);
			String intro = result.getString("description");
			if (StringUtils.isNoneEmpty(intro))
				fiction.setIntro(intro.replaceAll("\r\n", ""));
			fiction.setCover(result.getString("picUrl"));
			fiction.setAuthor(result.getString("authorName"));
			fiction.setIsFinish(result.getInteger("serialStatus"));
			fiction.setChannel(
					result.getBoolean("female") ? FictionChannel.GIRL.getCode() : FictionChannel.BOY.getCode());
			fiction.setTotalLength(result.getInteger("totalWord"));
			fiction.setPlatformId(Platform.ZONG_HENG.getCode());
			putModel(page, fiction);

			try {
				createJob(page, buildExtraInfoUrl(bookId, fiction.getChannel()));
				

				if(fiction.getChannel()==2)
					createJob(page,  String.format(PATH_CHAPTER,bookId));
				else
					createJob(page,  String.format(PATH_CHAPTER2,bookId,100));
			} catch (Throwable e) {
				// TODO Auto-generated catch block
				log.error(e.getMessage(), e);
			}

		}
	}

	private void createJob(Page page, String url) {
		Job oldJob = ((DelayRequest) page.getRequest()).getJob();
		try {
			Job job = new Job(url);
			DbEntityHelper.derive(oldJob, job);
			job.setCode(Md5Util.getMd5(url));
			job.setPlatformId(Platform.ZONG_HENG.getCode());
			putModel(page, job);
		} catch (Throwable e) {
		}

	}

	public void processExtraInfo(Page page) {
		int channel = Integer.valueOf(page.getUrl().regex(".*#channel-(\\d)$", 1).get());
		JSONObject response = page.getJson().toObject(JSONObject.class);
		if (response != null && response.getInteger("code") == 200) {
			JSONObject result = response.getJSONObject("result");

			if (channel == FictionChannel.BOY.getCode() && result.containsKey("bookStat")) {
				JSONObject bookStat = result.getJSONObject("bookStat");
				String code = bookStat.getString("bookId");
				if (bookStat.containsKey("monthTicket")) {
					FictionIncomeLogs incomeLog = new FictionIncomeLogs();
					incomeLog.setCode(code);
					incomeLog.setIncomeId(FictionIncome.ZONGHENG_YUEPIAO.getCode());
					incomeLog.setIncomeNum(bookStat.getInteger("monthTicket"));
					putModel(page, incomeLog);
				}

				if (bookStat.containsKey("totalClick")) {
					FictionPlatformClick click = new FictionPlatformClick();
					click.setClickCount(bookStat.getLongValue("totalClick"));
					click.setCode(code);
					click.setPlatformId(Platform.ZONG_HENG.getCode());
					putModel(page,click);
				}
				
				if(bookStat.containsKey("totalRecommend")) {
					FictionPlatformRecommend recommend = new FictionPlatformRecommend();
					recommend.setRecommendCount(bookStat.getIntValue("totalRecommend"));
					recommend.setCode(code);
					recommend.setPlatformId(Platform.ZONG_HENG.getCode());
					putModel(page,recommend);
				}
			}

			try {
				if (result.containsKey("forum"))
					createJob(page, buildCommentUrl(result.getJSONObject("forum").getString("id")));
			} catch (Throwable e) {
				// TODO Auto-generated catch block
				log.error(e.getMessage(), e);
			}
		}
	}

	public void processComment(Page page) {
		JSONObject response = page.getJson().toObject(JSONObject.class);
		if (response != null && response.getInteger("code") == 200 && response.containsKey("result")) {
			JSONObject result = response.getJSONObject("result");
			FictionCommentLogs commentLog = new FictionCommentLogs();
			commentLog.setCode(result.getString("bookId"));
			commentLog.setPlatformId(Platform.ZONG_HENG.getCode());
			if (result.containsKey("threadNum"))
				commentLog.setCommentCount(result.getInteger("threadNum"));
			putModel(page, commentLog);
		}
	}

	private static String buildCommentUrl(String forumId) throws Throwable {
		String params = String.format(PARAM_COMMENT, forumId);
		return PATH_COMMENT + params + "&sig=" + sign(key + params + key);
	}

	private String buildExtraInfoUrl(String bookId, int channel) throws Throwable {
		String params = String.format(PARAM_DETAIL, bookId);
		return PATH_EXTRA_INFO + params + "&sig=" + sign(key + params + key) + "#channel-" + channel;
	}

	private static String sign(String text) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		byte[] digest = MessageDigest.getInstance("MD5").digest(text.getBytes("UTF-8"));
		StringBuilder stringBuilder = new StringBuilder(digest.length * 2);
		for (byte b : digest) {
			if ((b & 255) < 16) {
				stringBuilder.append("0");
			}
			stringBuilder.append(Integer.toHexString(b & 255));
		}
		return stringBuilder.toString();
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
