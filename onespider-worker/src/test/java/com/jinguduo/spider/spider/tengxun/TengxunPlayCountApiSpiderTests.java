package com.jinguduo.spider.spider.tengxun;



import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

import com.jinguduo.spider.WorkerMainApplication;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.common.util.IoResourceHelper;
import com.jinguduo.spider.data.table.ShowLog;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.ResultItems;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class TengxunPlayCountApiSpiderTests {

	@Autowired
	private TengxunPlayCountSpider tengxunPlayCountSpider;

	final static String ALBUM_ID = "k9gaf8f7u1ef8jz";
	final static String URL = String.format("http://sns.video.qq.com/tvideo/fcgi-bin/batchgetplaymount?id=%s&otype=json", ALBUM_ID);

	final static String JSON_FILE = "/json/TengxunPlayCountSpliderTests.json";
	final static String RAW_TEXT = IoResourceHelper.readResourceContent(JSON_FILE);

	DelayRequest delayRequest;

	@Before
	public void setup()  {
		Job job = new Job();
		job.setPlatformId(1);
		job.setShowId(1);
		job.setUrl(URL);
		job.setFrequency(100);
		job.setMethod("GET");

		// request
		delayRequest = new DelayRequest(job);
	}

	@Test
	public void testContext() {
		Assert.notNull(tengxunPlayCountSpider);
	}

	@Test
	public void testProcessOk() throws Exception {
		// page
		Page page = new Page();
		page.setRequest(delayRequest);
		page.setStatusCode(HttpStatus.OK.value());
		page.setRawText(RAW_TEXT);

		tengxunPlayCountSpider.process(page);

		ResultItems resultItems = page.getResultItems();
		Assert.notNull(resultItems);

		Assert.notNull(resultItems.get(ShowLog.class.getSimpleName()));

	}

	@Test
	public void testProcess403() throws Exception {
		// page
		Page page = new Page();
		page.setRequest(delayRequest);
		page.setStatusCode(HttpStatus.FORBIDDEN.value());
		page.setRawText(RAW_TEXT);

		tengxunPlayCountSpider.process(page);

		ResultItems resultItems = page.getResultItems();
		Assert.notNull(resultItems);

		ShowLog showLog = (ShowLog) resultItems.get(ShowLog.class.getSimpleName());
		Assert.isNull(showLog);
	}
}
