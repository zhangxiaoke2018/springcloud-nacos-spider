package com.jinguduo.spider.spider.fiction;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.http.client.config.CookieSpecs;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.unbescape.html.HtmlEscape;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.ImmutableMap;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.constant.CommonEnum.Platform;
import com.jinguduo.spider.common.exception.PageBeChangedException;
import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.common.util.Md5Util;
import com.jinguduo.spider.common.util.NumberHelper;
import com.jinguduo.spider.common.util.RegexUtil;
import com.jinguduo.spider.data.table.FictionPlatformClick;
import com.jinguduo.spider.data.table.FictionPlatformRate;
import com.jinguduo.spider.data.table.FictionPlatformRecommend;
import com.jinguduo.spider.webmagic.Page;

@Worker
public class QDWebSpider extends CrawlSpider {
	private PageRule rules = PageRule.build().add("/info/", this::processAttribute).add("/info/", this::generateJob)
			.add("/ajax/comment/index", this::processScore);

	private Site site = SiteBuilder.builder().setDomain("book.qidian.com").setUserAgent(
			"Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.152 Safari/537.36")
			.setCookieSpecs(CookieSpecs.IGNORE_COOKIES).build();

	private static String SCORE_URL = "https://book.qidian.com/ajax/comment/index?bookId=%s&pageSize=15";

	@Override
	public Site getSite() {
		return site;
	}

	@Override
	public PageRule getPageRule() {
		return rules;
	}

	private void processScore(Page page) {
		JSONObject data = page.getJson().toObject(JSONObject.class).getJSONObject("data");
		int userCount = data.getIntValue("userCount");
		float rate = data.getFloatValue("rate");
		if (rate > 0) {
			String bookId = page.getUrl().regex(".*bookId=([0-9]+).*", 1).get();
			FictionPlatformRate rateItem = new FictionPlatformRate();
			rateItem.setCode(bookId);
			rateItem.setPlatformId(Platform.QI_DIAN.getCode());
			rateItem.setUserCount(userCount);
			rateItem.setRate(rate);
			putModel(page, rateItem);
		}
	}

	private void processAttribute(Page page) throws IOException, FontFormatException {
		String fontUrl = page.getHtml().xpath("//style").regex(".*(https\\:.*\\.ttf).*").get();
		fontUrl = fontUrl.replace("https", "http");
		String fontName = RegexUtil.getDataByRegex(fontUrl, ".*\\/([A-Za-z]+)\\.ttf$", 1);
		byte[] ttfData = Jsoup.connect(fontUrl).cookies(Collections.emptyMap()).ignoreHttpErrors(true)
				.ignoreContentType(true).execute().bodyAsBytes();
		Element p = page.getHtml().getDocument().getElementsByAttributeValue("class", "intro").next().get(0);
		decodeNumber(page, p, fontName, new ByteArrayInputStream(ttfData));
	}

	private void createJob(Page page, String link) {
		Job oldJob = ((DelayRequest) page.getRequest()).getJob();
		Job job = DbEntityHelper.derive(oldJob, new Job(link));
		job.setCode(Md5Util.getMd5(link));
		job.setPlatformId(Platform.QI_DIAN.getCode());
		putModel(page, job);
	}

	private void generateJob(Page page) {
		String bookId = page.getUrl().regex(".*\\/info\\/(\\d+)$").get();
		createJob(page, String.format(SCORE_URL, bookId));
	}

	private void decodeNumber(Page page, Element p, String fontName, InputStream inputStream)
			throws IOException, FontFormatException {

		String originText = p.text();
		Elements elements = p.getElementsByAttributeValue("class", fontName);
		Element element;
		Font font = createFont(inputStream);
		inputStream.close();

		for (int i = 0, size = elements.size(); i < size; i++) {
			element = elements.get(i);
			String unescaped = element.text();
			int[] codePoint = unescaped.codePoints().toArray();
			String[] decode = new String[codePoint.length];
			int start = 0, end = 0;
			for (int j = 0; j < codePoint.length; j++) {
				while (unescaped.codePointAt(start) == unescaped.codePointAt(end)) {
					end++;
				}
				String snippet = unescaped.substring(start, end + 1);
				String md5Str = getData(createImage(font, snippet));
				decode[j] = decodeMap.get(md5Str);
				start = end + 1;
				end++;
				if (end >= unescaped.length()) {
					break;
				}
			}
			String plainText = String.join("", decode);
			originText = originText.replaceFirst(unescaped, plainText);
		}
//		String totalClick = RegexUtil.getDataByRegex(originText, ".*\\|\\s+([0-9]+(.[0-9]+)?万?)阅文总点击.*");
		String totalRecommend = RegexUtil.getDataByRegex(originText, ".*\\|\\s+([0-9]+(.[0-9]+)?万?)总推荐.*");
		String bookId = page.getUrl().regex(".*\\/info\\/(\\d+)$").get();

		if (totalRecommend != null) {
			int recommend = (int) NumberHelper.parseShortNumber(totalRecommend, 0);
			FictionPlatformRecommend platformRecommend = new FictionPlatformRecommend();
			platformRecommend.setCode(bookId);
			platformRecommend.setPlatformId(Platform.QI_DIAN.getCode());
			platformRecommend.setRecommendCount(recommend);
			putModel(page, platformRecommend);
		}

//		if (totalClick != null) {
//			long click = NumberHelper.parseShortNumber(totalClick, 0);
//			FictionPlatformClick platformClick = new FictionPlatformClick();
//			platformClick.setCode(bookId);
//			platformClick.setPlatformId(Platform.QI_DIAN.getCode());
//			platformClick.setClickCount(click);
//			putModel(page, platformClick);
//		}

//		if (totalClick == null || totalRecommend == null) {
//			throw new PageBeChangedException("number bad [" + HtmlEscape.escapeHtml5(p.text()) + "]" + fontName);
//		}

	}

	private static Map<String, String> decodeMap = ImmutableMap.<String, String>builder()
			.put("39ccf661a9e0f11aaff81f1633c10b11", ".")
			.put("34c1795d25d685aa180b8ea5f3490f1c", "0")
			.put("87811fc14438f8642dcde8b889d5abd6", "1").put("15fc7bb55057bf34af2af89016e0df4e", "2")
			.put("99aef49e9f8983ef4e337d87faadb501", "3").put("945d3c8e089e4ee2bea376794387c727", "4")
			.put("6ef2b712109de9e01bd0cd98f23db18f", "5").put("80590309f63b00fb8f77555eea7d7110", "6")
			.put("26b7ec15331996ca021ce7feebf3fb33", "7").put("b422b9da8617f00cb82e00b58cd91a86", "8")
			.put("c8e23c0bc7ab85730ec2ca3784859aeb", "9").build();

	private static BufferedImage createImage(Font f, String text) {
		BufferedImage img = new BufferedImage(9, 19, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = img.createGraphics();
		g2d.setFont(f);
		g2d.setColor(Color.BLACK);
		g2d.drawString(text, 0, 15);
		g2d.dispose();
		
		return img;
	}

	private static Font createFont(InputStream fileInputStream) throws FontFormatException, IOException {
		Font f = Font.createFont(Font.TRUETYPE_FONT, fileInputStream);
		f = f.deriveFont(Font.PLAIN, 16f);
		return f;
	}

	static HashFunction hashing = Hashing.md5();

	private static String getData(BufferedImage originalImage) throws IOException {
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream();) {
			ImageIO.write(originalImage, "PNG", baos);
			baos.flush();
			byte[] imageInByte = baos.toByteArray();
			Hasher hasher = hashing.newHasher();
			return hasher.putBytes(imageInByte).hash().toString();
		}
	}
}
