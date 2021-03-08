package com.jinguduo.spider.spider.maoyan;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.Date;
import java.text.ParseException;
import java.util.List;
import java.util.Locale;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.format.datetime.DateFormatter;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.util.RegexUtil;
import com.jinguduo.spider.data.table.MaoyanActor;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.selector.Html;

import lombok.extern.apachecommons.CommonsLog;

@Worker
@CommonsLog
public class MaoyanAppSpider extends CrawlSpider {

	private Site site = SiteBuilder.builder().setDomain("piaofang.maoyan.com").setUserAgent(
			"Mozilla/5.0 (Linux; Android 7.0; SM-G9350 Build/NRD90M; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/57.0.2987.132 MQQBrowser/6.2 TBS/044109 Mobile Safari/537.36 MicroMessenger/6.6.7.1321(0x26060739) NetType/WIFI Language/zh_CN")
			.build();
	private PageRule rules = PageRule.build().add(".*\\/celebrity\\-board\\/query.*", page -> actorList(page))
			.add(".*\\/celebrity\\?id\\=\\d+$", page -> actorDetail(page));

	private static final String ACTOR_LIST = "http://piaofang.maoyan.com/celebrity-board/query?page=%d&cLevel1Id=29&cLevel2Id=30&cId=1&work-type=-1&work-style=-1&verified=-1&gender=-1";
	private static final String ACTOR_DETAIL = "http://piaofang.maoyan.com/celebrity?id=%s";

	private void actorList(Page page) {
		JSONObject response = page.getJson().toObject(JSONObject.class);
		int currentPage = response.getIntValue("page");

		if (response.getBooleanValue("success") && response.containsKey("data")) {
			createDetailJobs(page, response.getString("data"));
			if (1 == currentPage && response.getBooleanValue("hasMore")) {
				createListJobs(page, 2, 500);
			}
		}
	}

	private void actorDetail(Page page) {
		Html html = page.getHtml();
		MaoyanActor actor = new MaoyanActor();
		actor.setUrl(page.getUrl().get());
		actor.setCode(page.getUrl().regex(".*\\/celebrity\\?id\\=(\\d+)$", 1).get());

		String name = html.xpath("//head/title/text()").get();
		actor.setName(name.trim());

		// extract expanded info
		List<String> baseInfos = html.xpath("//p[@class=\"content-page\"]/span/text()").all();
		fillInfo(baseInfos, actor);

		// extract collapse info
		List<String> collapseInfo = html
				.xpath("//div[@class=\"cele-incroduction expand-content content-page\"]/div/text()").all();
		fillInfo(collapseInfo, actor);

		if (!StringUtils.isEmpty(actor.getCode()) && !StringUtils.isEmpty(actor.getName()))
			putModel(page, actor);
	}

	private void fillInfo(List<String> infos, MaoyanActor actor) {
		for (String info : infos) {
			if (StringUtils.isEmpty(info) || info.trim().isEmpty()) {
				continue;
			}
			int index = -1;
			index = info.indexOf("：");
			if (info.trim().startsWith("身高：")) {
				actor.setHeight(Integer.valueOf(RegexUtil.getDataByRegex(info, "(\\d+)", 1)));
			} else if (info.trim().startsWith("出生日期：")) {
				try {
					String dateOfBirth = RegexUtil.getDataByRegex(info, "(\\d{4}-\\d{2}-\\d{2})", 1);
					if (!StringUtils.isEmpty(dateOfBirth)) {
						DateFormatter formatter = new DateFormatter();
						formatter.setPattern("yyyy-MM-dd");
						actor.setBirth(new Date(formatter.parse(dateOfBirth, Locale.getDefault()).getTime()));
					}
				} catch (ParseException e) {
					log.info("parse birth of date failed.");
				}

				String constellation = RegexUtil.getDataByRegex(info, "\uFF08(.*)\uFF09", 1);
				actor.setConstellation(constellation);
			} else if (info.trim().startsWith("毕业院校：")) {
				String school = info.substring(index + 1).trim();
				if (school.length() > 256) {
					if (school.contains("、"))
						school = school.substring(0, school.lastIndexOf("、", 256));
					else if (school.contains(","))
						school = school.substring(0, school.lastIndexOf(",", 256));
					else
						school = school.substring(0, 256);
				}

				actor.setSchool(school);
			} else if (info.trim().startsWith("昵称：")) {
				String nickname = info.substring(index + 1).trim();

				if (nickname.length() > 64) {
					if (nickname.contains("、"))
						nickname = nickname.substring(0, nickname.lastIndexOf("、", 64));
					else if (nickname.contains(","))
						nickname = nickname.substring(0, nickname.lastIndexOf(",", 64));
					else
						nickname = nickname.substring(0, 64);
				}
				actor.setNickname(nickname);
			} else if (info.trim().startsWith("性别：")) {
				actor.setGender(info.substring(index + 1).trim());
			} else if (info.trim().startsWith("出生地：")) {
				actor.setHometown(info.substring(index + 1).trim());
			} else if (info.trim().startsWith("民族：")) {
				String ethnic = info.substring(index + 1).trim();
				if (ethnic.length() <= 32)
					actor.setEthnic(ethnic);
			} else if (info.trim().startsWith("血型：")) {
				String blood = info.substring(index + 1).trim();
				if (blood.length() > 12)
					blood = "其他";
				actor.setBlood(blood);
			} else if (info.trim().startsWith("国籍：")) {
				String nationality = info.substring(index + 1).trim();
				if (nationality.length() <= 32)
					actor.setNationality(nationality);
			}

		}

	}

	private void createListJobs(Page page, int from, int to) {
		for (int i = from; i <= to; i++) {
			Job job = new Job(String.format(ACTOR_LIST, i));
			job.setPlatformId(46);
			job.setCode(DigestUtils.md5Hex(job.getUrl()));
			putModel(page, job);
		}
	}

	private void createDetailJobs(Page page, String data) {
		try {
			Html html = Html.create(URLDecoder.decode(data, "utf-8"));
			List<String> details = html.links().regex(".*\\/celebrity\\?id\\=(\\d+)$").all();
			details.forEach(id -> {
				Job job = new Job(String.format(ACTOR_DETAIL, id));
				job.setPlatformId(46);
				job.setCode(DigestUtils.md5Hex(job.getUrl()));
				putModel(page, job);
			});
		} catch (UnsupportedEncodingException e) {
			log.info("decode charset utf-8 failed");
		}
	}

	@Override
	public Site getSite() {
		return site;
	}

	@Override
	public PageRule getPageRule() {
		return rules;
	}

}
