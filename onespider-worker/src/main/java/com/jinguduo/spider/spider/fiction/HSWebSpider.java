package com.jinguduo.spider.spider.fiction;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Connection.Method;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

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
import com.jinguduo.spider.webmagic.Page;

import lombok.extern.apachecommons.CommonsLog;

@Worker
@CommonsLog
public class HSWebSpider extends CrawlSpider {
	private Site site = new SiteBuilder().setDomain("www.hongshu.com").build();
	private PageRule rule = PageRule.build().add("^http:\\/\\/www\\.hongshu\\.com$", this::processEntrance)
			.add("/book/", this::processDetail);
	private static final Pattern PATTERN_DIGIT = Pattern.compile("([0-9]+\\.?([0-9,]+)?)");
	private static final String PATH_SEARCH = "http://www.hongshu.com/homeajax.do";
	private static final String PATH_DETAIL = "http://www.hongshu.com/book/%s/";

	public void processEntrance(Page page) throws IOException {
		for (int index = 1; index < 3; index++) {
				String response = Jsoup.connect(PATH_SEARCH).ignoreContentType(true)
						.data("method", "search", "pagesize", "100", "page", String.valueOf(index), "order", "4")
						.method(Method.POST).execute().body();
				JSONArray data = JSONObject.parseObject(response).getJSONArray("bookinfo");
				JSONObject item;
				for (int i = 0, size = data.size(); i < size; i++) {
					item = data.getJSONObject(i);
					Fiction fiction = new Fiction();
					fiction.setTags(item.getString("classname"));
					fiction.setCode(item.getString("bid"));
					fiction.setCover(item.getString("bookface"));
					String intro = item.getString("intro");
					if(StringUtils.isNoneEmpty(intro)){
						intro = intro.replaceAll("<br />\r\n", "");
					}
					fiction.setIntro(intro);
					fiction.setAuthor(item.getString("authorname"));
					fiction.setChannel("nan".equals(item.getString("sex_flag")) ? FictionChannel.BOY.getCode()
							: FictionChannel.GIRL.getCode());
					fiction.setIsFinish(item.getInteger("finish") == 2 ? 0 : 1);
					fiction.setName(item.getString("catename"));
					fiction.setPlatformId(Platform.HONG_SHU.getCode());
					fiction.setTotalLength(item.getInteger("charnum"));
					putModel(page, fiction);
					createJob(page, String.format(PATH_DETAIL, fiction.getCode()));
				}
		}
	}

	private void createJob(Page page, String url) {
		Job oldJob = ((DelayRequest)page.getRequest()).getJob();
		Job job = new Job();
		DbEntityHelper.derive(oldJob, job);
		job.setCode(Md5Util.getMd5(url));
		job.setUrl(url);
		job.setPlatformId(Platform.HONG_SHU.getCode());
		putModel(page, job);
	}

	public void processDetail(Page page) {
		String bookId = page.getUrl().regex(".*\\/book\\/(\\d+)\\/$", 1).get();

		Element incomeInfos = page.getHtml().getDocument().getElementsByClass("giving clearfix").first().child(0);
		int incomeCount = 0;
		Matcher count;
		for (Element income : incomeInfos.children()) {
			count = PATTERN_DIGIT.matcher(income.text());
			if (count.find()) {
				incomeCount += Integer.valueOf(income.attr("price")) * Integer.valueOf(count.group());
			}
		}

		FictionIncomeLogs incomeLog = new FictionIncomeLogs();
		incomeLog.setCode(bookId);
		incomeLog.setIncomeId(FictionIncome.HONGSHU_DASHANG.getCode());
		incomeLog.setIncomeNum(incomeCount);
		putModel(page, incomeLog);

		String commentCountStr = page.getHtml().getDocument().getElementsByAttributeValue("category", "comment").first()
				.child(1).text();
		

		FictionCommentLogs commentLogs = new FictionCommentLogs();
		commentLogs.setCode(bookId);
		commentLogs.setPlatformId(Platform.HONG_SHU.getCode());
		if (commentCountStr != null && commentCountStr.contains("最新书评")) {
			Matcher matcher = PATTERN_DIGIT.matcher(commentCountStr);
			if (matcher.find()) {
				commentLogs.setCommentCount(Integer.valueOf( matcher.group().replace(",", "")));
			}
		}
		putModel(page, commentLogs);
	}

	@Override
	public Site getSite() {
		// TODO Auto-generated method stub
		return site;
	}

	@Override
	public PageRule getPageRule() {
		// TODO Auto-generated method stub
		return rule;
	}
}
