package com.jinguduo.spider.spider.fiction;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Element;

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

@Worker
public class Xiang5Spider extends CrawlSpider {
	private Site site = SiteBuilder.builder().setDomain("www.xiang5.com")
			.addCookie("www_say", "df4b543c57f805aaf30b53615d718c18")
			.addHeader("User-Agent",
					"Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.152 Safari/537.36")
			.build();
	private PageRule rules = PageRule.build()
			.add("^http:\\/\\/www\\.xiang5\\.com$", this::processEntrance)
			.add("/paihang/", this::processRank)
			.add("/bookinfo/", this::processDetail);

	private static final String PATH_RANK = "http://www.xiang5.com/paihang/%s/dtype/isfinish/%s/%d.html";
	private static final String[] RANK_TYPE = { "viewTop", "costTop", "giftTop", "hongbaoTop", "tuijianTop" };
	private static final String[] TIME_SORT = { "totalnum", "monthnum" };
	private static final Pattern PATTERN_DIGIT = Pattern.compile("(\\d*\\.?\\d+?)");

	public void processEntrance(Page page) {
		for (String rankType : RANK_TYPE) {
			for (String timeSort : TIME_SORT) {
				createJob(page, String.format(PATH_RANK, rankType, timeSort, 1));
				if ("totalnum".equals(timeSort)) {
					createJob(page, String.format(PATH_RANK, rankType, timeSort, 2));
				}
			}
		}
	}

	public void processRank(Page page) {
		List<String> detailLinks = page.getHtml().links().regex(".*\\/bookinfo\\/\\d+\\.html$").all();
		for (String detailUrl : detailLinks) {
			createJob(page, detailUrl);
		}
	}

	public void processDetail(Page page) {
		String bookId = page.getUrl().regex(".*\\/bookinfo\\/(\\d+)\\.html$").get();
		Element info = page.getHtml().getDocument().getElementById("sendprize");

		String cover = info.getElementsByClass(" fl worksLL").first().getElementsByTag("a").first().child(0).attr("src");
		String intro = info.getElementsByClass(" fr worksLR").first().getElementsByTag("p").first().text();
		Element btns = info.getElementsByClass("workListBtn").first();
		for (Element child : btns.children()) {
			if (child.text().contains("红包")) {
				Matcher matcher = PATTERN_DIGIT.matcher(child.text());
				if (matcher.find()) {
					FictionIncomeLogs incomeLog = new FictionIncomeLogs();
					incomeLog.setCode(bookId);
					incomeLog.setIncomeId(FictionIncome.XIANG_5_HONGBAO.getCode());
					String incomeStr = matcher.group();
					float incomeNum = Float.valueOf(incomeStr);
					if (child.text().contains("万"))
						incomeNum *= 10000;
					incomeLog.setIncomeNum((int) incomeNum);
					putModel(page, incomeLog);
				}
			}
		}

		String title = info.child(1).child(0).text();
		String author = info.getElementsByClass("workSecTitle").first().getElementsByTag("a").text();
		String isFinish = info.getElementsByClass("workSecTitle").first().child(0).text();
		Element infoOther = info.getElementsByClass("workSecHit").first();
		float wordCount = 0f;
		for (Element child : infoOther.children()) {
			if (child.text().contains("字数")) {
				Matcher matcher = PATTERN_DIGIT.matcher(child.text());
				if (matcher.find()) {
					String wordCountStr = matcher.group();
					wordCount = Float.valueOf(wordCountStr);
					if (child.text().contains("万"))
						wordCount *= 10000;
				}
			}
		}
		String category = info.getElementsByClass("workInfoList").first().child(0).child(0).text();
		Fiction fiction = new Fiction();
		fiction.setAuthor(author);
		fiction.setChannel(FictionChannel.GIRL.getCode());
		fiction.setCode(bookId);
		fiction.setTags(category);
		fiction.setIsFinish((isFinish != null && isFinish.contains("完结")) ? 1 : 0);
		fiction.setName(title);
		fiction.setCover(cover);
		fiction.setIntro(intro);
		fiction.setPlatformId(Platform.XIANG_5.getCode());
		fiction.setTotalLength((int) wordCount);
		putModel(page, fiction);

		Integer comment = Integer.valueOf(
				page.getHtml().xpath("//div[@id=\"pinglun\"]/h4/span[@class=\"fl\"]/b/text()").regex("(\\d+)").get());
		FictionCommentLogs commentLogs = new FictionCommentLogs();
		commentLogs.setCode(bookId);
		commentLogs.setPlatformId(Platform.XIANG_5.getCode());
		commentLogs.setCommentCount(comment);
		putModel(page, commentLogs);
	}

	public void createJob(Page page, String url) {
		Job oldJob = ((DelayRequest)page.getRequest()).getJob();
		Job job = new Job(url);
		DbEntityHelper.derive(oldJob, job);
		job.setCode(Md5Util.getMd5(url));
		job.setPlatformId(Platform.XIANG_5.getCode());
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
