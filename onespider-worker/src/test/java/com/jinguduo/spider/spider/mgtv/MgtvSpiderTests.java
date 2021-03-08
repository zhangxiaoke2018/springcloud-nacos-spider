package com.jinguduo.spider.spider.mgtv;



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
import com.jinguduo.spider.data.table.Show;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.ResultItems;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class MgtvSpiderTests {
	
	@Autowired
	private MgtvSpider mgtvSpider;

	final static String URL = "http://www.mgtv.com/v/1/291976/f/3285994.html";

	final static String YEAR_URL = "http://www.mgtv.com/v/1/291976/s/json.year.js";

	final static String INNER_YEAR_URL = "http://www.mgtv.com/v/1/291976/s/json.2016.js";

	private final static String HTML_FILE = "/html/MgtvPageSpiderTests.html";
	private final static String RAW_TEXT = IoResourceHelper.readResourceContent(HTML_FILE);

	private final static String YEAR_JSON_FILE = "/json/MgtvSpiderYearTests.json";
	private final static String YEAR_RAW_TEXT = IoResourceHelper.readResourceContent(YEAR_JSON_FILE);

	private final static String JSON_FILE = "/json/MgtvSpiderTests.json";
	private final static String YEAR_JSON_RAW_TEXT = IoResourceHelper.readResourceContent(JSON_FILE);

	DelayRequest delayRequest;
	DelayRequest delayRequest2;
	DelayRequest delayRequest3;

	@Before
	public void setup()  {
		Job job = new Job(URL);
		job.setPlatformId(1);
		job.setShowId(1);
		job.setFrequency(100);
		delayRequest = new DelayRequest(job);

		Job job2 = new Job(YEAR_URL);
		job2.setPlatformId(1);
		job2.setShowId(1);
		job2.setFrequency(100);
		delayRequest2 = new DelayRequest(job2);

		Job job3 = new Job(INNER_YEAR_URL);
		job3.setPlatformId(1);
		job3.setShowId(1);
		job3.setFrequency(100);
		delayRequest3 = new DelayRequest(job3);
	}

	@Test
	public void testContext() {
		Assert.notNull(mgtvSpider);
	}


//	@Test
	public void testYear() throws Exception {

		Page page = new Page();
		page.setRequest(delayRequest2);
		page.setStatusCode(HttpStatus.OK.value());
		page.setRawText(YEAR_RAW_TEXT);

		mgtvSpider.process(page);

		ResultItems resultItems = page.getResultItems();
		Assert.notNull(resultItems);

		Job job = (Job) resultItems.get(Job.class.getSimpleName());
		Assert.notNull(job);

	}

//	@Test
	public void testInnerYear() throws Exception {

		Page page = new Page();
		page.setRequest(delayRequest3);
		page.setStatusCode(HttpStatus.OK.value());
		page.setRawText(YEAR_JSON_RAW_TEXT);

		mgtvSpider.process(page);

		ResultItems resultItems = page.getResultItems();
		Assert.notNull(resultItems);

		Job job = (Job) resultItems.get(Job.class.getSimpleName());
		Assert.notNull(job);
		Show show = (Show) resultItems.get(Show.class.getSimpleName());
		Assert.notNull(show);

	}
}
