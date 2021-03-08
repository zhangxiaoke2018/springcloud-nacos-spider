package com.jinguduo.spider.spider.mgtv;

import java.util.List;

import com.jinguduo.spider.data.table.CommentLog;
import com.jinguduo.spider.data.text.CommentText;

import lombok.extern.apachecommons.CommonsLog;

import org.apache.commons.collections.CollectionUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

import com.jinguduo.spider.cluster.engine.SpiderEngine;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.pipeline.TestPipeline;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.webmagic.ResultItems;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
@CommonsLog
public class MgTvCommentCountApiSpiderIT {

	@Autowired
	private MgTvCommentCountApiSpider mgTvCommentCountApiSpider;

	final static String URL = "http://comment.mgtv.com/video_comment/list/?subject_id=9552358&page=1";

	final static String COMMENT_TEXT_URL = "http://comment.mgtv.com/video_comment/list/?subject_id=3809508&page=1";

	DelayRequest delayRequest;

	@Before
	public void setup() {
		Job job = new Job(URL);
		job.setPlatformId(1);
		job.setShowId(1);
		job.setFrequency(100);
		job.setCode("1039135");

		delayRequest = new DelayRequest(job);
	}

	@Test
	public void testContext() {
		Assert.notNull(mgTvCommentCountApiSpider);
	}

	@Test
	public void testTest() {
		String url = "http://comment.mgtv.com/video_comment/list/?subject_id=9589845&page=1";
		TestPipeline testPipeline = new TestPipeline();
		Job job = new Job(url);
		job.setPlatformId(7);
		job.setCode("9589845");
		job.setMethod("GET");
		DelayRequest request = new DelayRequest(job);
		SpiderEngine.create(mgTvCommentCountApiSpider).addPipeline(testPipeline).addRequest(request).run();

		ResultItems resultItems = testPipeline.getResultItems();
		List<CommentLog> commentLog = resultItems.get(CommentLog.class.getSimpleName());

		List<CommentText> commentTexts = resultItems.get(CommentText.class.getSimpleName());

		List<Job> newJobs = resultItems.get(Job.class.getSimpleName());

		Assert.notNull(commentLog, "sad");
		Assert.notNull(commentTexts, "sad");
		Assert.notNull(newJobs, "sad");

		Job next = newJobs.get(newJobs.size()-1);
		testPipeline = new TestPipeline();
		request = new DelayRequest(next);
		SpiderEngine.create(mgTvCommentCountApiSpider).addPipeline(testPipeline).addRequest(request).run();
		resultItems = testPipeline.getResultItems();
		commentLog = resultItems.get(CommentLog.class.getSimpleName());

		commentTexts = resultItems.get(CommentText.class.getSimpleName());

		newJobs = resultItems.get(Job.class.getSimpleName());
		Assert.isTrue(CollectionUtils.isEmpty(commentLog), "sad");
		Assert.notNull(commentTexts, "sad");
		Assert.isTrue(CollectionUtils.isEmpty(newJobs), "sad");
	}

	@Test
	public void testRun() {
		TestPipeline testPipeline = new TestPipeline();
		SpiderEngine.create(mgTvCommentCountApiSpider).addPipeline(testPipeline).addRequest(delayRequest).run();

		ResultItems resultItems = testPipeline.getResultItems();
		List<CommentLog> commentLog = resultItems.get(CommentLog.class.getSimpleName());
		Assert.notNull(commentLog);
		log.debug(commentLog);

	}

	/** 评论文本测试 */
	@Test
	public void testCommentText() {
		Job job = new Job(COMMENT_TEXT_URL);
		job.setPlatformId(7);
		job.setCode("3809508");
		job.setMethod("GET");
		DelayRequest delayRequest = new DelayRequest(job);
		TestPipeline testPipeline = new TestPipeline();
		SpiderEngine.create(mgTvCommentCountApiSpider).addPipeline(testPipeline).addRequest(delayRequest).run();

		ResultItems resultItems = testPipeline.getResultItems();
		List<CommentText> commentTexts = resultItems.get(CommentText.class.getSimpleName());
		Assert.isTrue(commentTexts.size() == 15);
		List<Job> nextJob = resultItems.get(Job.class.getSimpleName());
		Assert.isTrue(nextJob.get(0).getUrl()
				.equals("http://comment.mgtv.com/video_comment/list/?subject_id=3809508&page=2"));
		Assert.notNull(commentTexts);

	}

}
