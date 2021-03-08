package com.jinguduo.spider.spider.iqiyi;


import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

import com.jinguduo.spider.WorkerMainApplication;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.common.util.IoResourceHelper;
import com.jinguduo.spider.data.table.Show;
import com.jinguduo.spider.data.table.ShowLog;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.ResultItems;
import com.jinguduo.spider.webmagic.selector.PlainText;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class IqiyiApiSpiderTests {

	@Autowired
	private IqiyiApiSpider iqiyiApiSpider;

	//   /jp/pc/
	final static String ALBUM_ID = "422060400";
	final static String URL = String.format("http://cache.video.qiyi.com/jp/pc/%s/", ALBUM_ID);

	//avlist
	final static String TV_ALBUM_ID = "203164301";
	final static String AVLST_URL = String.format("http://cache.video.qiyi.com/jp/avlist/%s/?albumId=%s", TV_ALBUM_ID,TV_ALBUM_ID);

	final static int PLAY_COUNT = 64944;
	final static String RAW_TEXT = String.format("var tvInfoJs=[{\"422060400\":%s}]", PLAY_COUNT);

	DelayRequest pcDelayRequest;
	
	DelayRequest sdvlstDelayRequest;

	DelayRequest avlstDelayRequest;
	
	//   /jp/sdvlst/
	final static String SDVLST_URL = "http://cache.video.qiyi.com/jp/sdvlst/6/203617401/";
	
	@Value("classpath:json/IqiyiPlayCountApiSpiderTests.json")
	private Resource sdvlstRawText;

	@Value("classpath:json/IqiyiAvlistApiSpiderTests.json")
	private Resource avlstRawText;
	
	private final static int sdvlstCount = 25;
	
	@Before
	public void setup()  {
		Job pcJob = new Job(URL);
		pcJob.setPlatformId(1);
		pcJob.setShowId(1);
		pcJob.setFrequency(100);

		// request
		pcDelayRequest = new DelayRequest(pcJob);
		
		Job sdvlstJob = new Job(SDVLST_URL);
		sdvlstJob.setPlatformId(2);
		sdvlstJob.setShowId(2);
		sdvlstJob.setFrequency(100);
		
		sdvlstDelayRequest = new DelayRequest(sdvlstJob);

		Job avlstJob = new Job(AVLST_URL);
		avlstJob.setPlatformId(1);
		avlstJob.setShowId(1);
		avlstJob.setFrequency(100);

		// request
		avlstDelayRequest = new DelayRequest(avlstJob);
	}

	@Test
	public void testProcessPc() throws Exception {
		// page
		Page page = new Page();
		page.setRequest(pcDelayRequest);
		page.setStatusCode(HttpStatus.OK.value());
		page.setRawText(RAW_TEXT);

		iqiyiApiSpider.process(page);

		ResultItems resultItems = page.getResultItems();
		Assert.notNull(resultItems);

		List<ShowLog> showLogList =  resultItems.get(ShowLog.class.getSimpleName());
		Assert.notNull(showLogList);

		Assert.isTrue(showLogList.get(0).getPlayCount() == PLAY_COUNT);
		Assert.notNull(showLogList.get(0).getShowId());
		Assert.notNull(showLogList.get(0).getPlatformId());
	}

	@Test
	public void testProcessPc403() throws Exception {
		// page
		Page page = new Page();
		page.setRequest(pcDelayRequest);
		page.setStatusCode(HttpStatus.FORBIDDEN.value());
		page.setRawText(RAW_TEXT);

		iqiyiApiSpider.process(page);

		ResultItems resultItems = page.getResultItems();
		Assert.notNull(resultItems);

		ShowLog showLog = (ShowLog) resultItems.get(ShowLog.class.getSimpleName());
		Assert.isNull(showLog);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testProcessSdvlstOk() throws Exception {
		Page page = new Page();
		page.setRequest(sdvlstDelayRequest);
		page.setUrl(new PlainText("http://cache.video.qiyi.com/jp/sdvlst/6/203617401/"));
		page.setStatusCode(HttpStatus.OK.value());
		page.setRawText(IoResourceHelper.readResourceContent(sdvlstRawText));

		iqiyiApiSpider.process(page);

		ResultItems resultItems = page.getResultItems();
		Assert.notNull(resultItems);

		List<Show> shows = (List<Show>) resultItems.get(Show.class.getSimpleName());
		Assert.notNull(shows);
		Assert.isTrue(shows.size() == sdvlstCount);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testProcessAvlstOk() throws Exception {
		Page page = new Page();
		page.setRequest(avlstDelayRequest);
		page.setStatusCode(HttpStatus.OK.value());
		page.setRawText(IoResourceHelper.readResourceContent(avlstRawText));

		iqiyiApiSpider.process(page);

		ResultItems resultItems = page.getResultItems();
		Assert.notNull(resultItems);

		List<Show> shows = (List<Show>) resultItems.get(Show.class.getSimpleName());
		Assert.notNull(shows);
		Assert.isTrue(shows.size() == 20);

		List<Job> job = (List<Job>) resultItems.get(Job.class.getSimpleName());
		Assert.notNull(job);
		Assert.isTrue(job.size() == 88);
	}
}
