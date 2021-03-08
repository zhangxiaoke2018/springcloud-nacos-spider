package com.jinguduo.spider.spider.mgtv;


import java.util.List;

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
public class MgtvPlayCountSpiderTests {
	
	@Autowired
	private MgtvPlayCountSpider mgtvSpider;

	final static String URL = "http://videocenter-2039197532.cn-north-1.elb.amazonaws.com.cn//dynamicinfo?callback=jQuery18209559400354382939_1466759714593&vid=3285994&_=1466759715614";

	private final static String JSON_FILE = "/json/MgtvPlayCountSpiderTests.json";
	private final static String RAW_TEXT = IoResourceHelper.readResourceContent(JSON_FILE);

	DelayRequest delayRequest;

	@Before
	public void setup()  {
		Job job = new Job(URL);
		job.setPlatformId(1);
		job.setShowId(1);
		job.setFrequency(100);

		delayRequest = new DelayRequest(job);
	}

	@Test
	public void testContext() {
		Assert.notNull(mgtvSpider);
	}

	@Test
	public void testRun() throws Exception {
		Page page = new Page();
		page.setRequest(delayRequest);
		page.setStatusCode(HttpStatus.OK.value());
		page.setRawText(RAW_TEXT);

		mgtvSpider.process(page);

		ResultItems resultItems = page.getResultItems();
		Assert.notNull(resultItems);
		List<ShowLog> showLog = resultItems.get(ShowLog.class.getSimpleName());

		Assert.isTrue(showLog.get(0).getPlayCount() == 32744176);

	}
}
