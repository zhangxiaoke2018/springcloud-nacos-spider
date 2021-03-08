package com.jinguduo.spider.spider.mgtv;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.constant.FrequencyConstant;
import com.jinguduo.spider.common.exception.AntiSpiderException;
import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.data.table.CommentLog;
import com.jinguduo.spider.data.text.CommentText;
import com.jinguduo.spider.webmagic.Page;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.config.CookieSpecs;

import java.sql.Timestamp;

@Worker
public class MgTvCommentCountApiSpider extends CrawlSpider {

	/** 芒果视频评论文本URL */
	final String COMMENT_TEXT_URL = "http://comment.mgtv.com/video_comment/list/?subject_id=3336769&page=1";

	/** 以下两个常量用于获取评论文本创建时间对应的时间戳 */
	private final static String HOUR_BEFORE = "小时前";
	private final static String MINUTE_BEFORE = "分钟前";
	private final static String DAY_BEFORE = "天前";
	private final static String MONTH_BEFORE = "月前";

	private final static Long ONE_MINUTE = 60 * 1000l;// 一分钟
	private final static Long ONE_HOUR = 60 * 60 * 1000l;// 一小时
	private final static Long ONE_DAY = 24 * 60 * 60 * 1000l;// 一天
	private final static Long ONE_MONTH = 30 * 24 * 60 * 60 * 1000l;// 一月

	private Site site = SiteBuilder.builder()
			.setDomain("comment.mgtv.com")
			.setCookieSpecs(CookieSpecs.IGNORE_COOKIES)
			.build();

	private PageRule rule = PageRule.build().add("/video_comment/list/", page -> processComment(page));
	
	private static final String COMENT_URL = "http://comment.mgtv.com/video_comment/list/?subject_id=%s&page=%d";

	public void processComment(Page page) {
		Job job = ((DelayRequest) page.getRequest()).getJob();

		String rawText = page.getRawText();
		if (StringUtils.isBlank(rawText)) {
			throw new AntiSpiderException("response body is null");
		}

		JSONObject jsonObject = JSONObject.parseObject(rawText);
		if (jsonObject.getInteger("total_number") > 0 && jsonObject.getJSONArray("comments").size() > 0) {
			
			int currentPage = jsonObject.getIntValue("current_page");
			int totalNumber = jsonObject.getIntValue("total_number");
			int perPage = jsonObject.getIntValue("perpage");
			
			String code = page.getUrl().regex(".*\\?subject_id=(\\d+)&page=",1).get();
			
			//存评论
			saveCommentText(page,job,jsonObject.getJSONArray("comments"));
			
			//在第一页生成其他分页任务，存一次 总数
			if (currentPage == 1) {
				CommentLog commentLog = new CommentLog(totalNumber);
				DbEntityHelper.derive(job, commentLog);
				putModel(page, commentLog);
				
				createOtherJobs(page,job,code,2,totalNumber,perPage);
			}
		}
	}
	
	private void saveCommentText(Page page,Job job,JSONArray jsonArray) {
		JSONObject json;
		for(int i =0,size=jsonArray.size();i<size;i++) {
			json = jsonArray.getJSONObject(i);
			
			String commentId = json.getString("comment_id");// 评论ID
			if (!json.containsKey("content")) {
				return;
			}
			String content = json.getString("content");// 评论文本
			Integer up = json.getInteger("up_num");// 点赞量
			JSONObject userJson = json.getJSONObject("user");
			String nickname = "";// 用户昵称
			if (null != userJson) {
				nickname = userJson.getString("nickname");
			}
			String create_time = json.getString("create_time");// 文本创建时间
			Long createTime = 0L;

			if (create_time.endsWith(HOUR_BEFORE)) {
				create_time = create_time.substring(0, create_time.length() - HOUR_BEFORE.length());
				createTime = Long.parseLong(create_time) * ONE_HOUR;
			} else if (create_time.endsWith(MINUTE_BEFORE)) {
				create_time = create_time.substring(0, create_time.length() - MINUTE_BEFORE.length());
				createTime = Long.parseLong(create_time) * ONE_MINUTE;
			} else if (create_time.endsWith(DAY_BEFORE)) {
				create_time = create_time.substring(0, create_time.length() - DAY_BEFORE.length());
				createTime = Long.parseLong(create_time) * ONE_DAY;
			} else if (create_time.endsWith(MONTH_BEFORE)) {
				create_time = create_time.substring(0, create_time.length() - MONTH_BEFORE.length());
				createTime = Long.parseLong(create_time) * ONE_MONTH;
			}
			CommentText commentText = new CommentText();
			commentText.setCommentId(commentId);
			commentText.setUp(up);
			commentText.setContent(content);
			commentText.setCreatedTime(new Timestamp(System.currentTimeMillis() - createTime));
			commentText.setNickName(nickname);
			DbEntityHelper.derive(job, commentText);
			putModel(page, commentText);
		}
	}
	
	private void createOtherJobs(Page page,Job old,String code,int start,int totalCount,int pageSize) {
		if(totalCount<=pageSize)
			return;

		for(int i=start;i<=1+totalCount/pageSize;i++) {
			Job newJob = new Job(String.format(COMENT_URL, code, i));
			DbEntityHelper.derive(old, newJob);
			newJob.setFrequency(FrequencyConstant.COMMENT_TEXT);
			putModel(page, newJob);
		}
	}

	@Override
	public PageRule getPageRule() {
		return rule;
	}

	/**
	 * get the site settings
	 *
	 * @return site
	 * @see Site
	 */
	@Override
	public Site getSite() {
		return site;
	}
}
