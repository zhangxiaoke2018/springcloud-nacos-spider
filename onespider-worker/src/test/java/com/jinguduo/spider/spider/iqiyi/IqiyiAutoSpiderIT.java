package com.jinguduo.spider.spider.iqiyi;

import java.util.List;

import lombok.extern.apachecommons.CommonsLog;

import org.apache.commons.collections.CollectionUtils;
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
import com.jinguduo.spider.data.table.Show;
import com.jinguduo.spider.webmagic.ResultItems;
import com.jinguduo.spider.webmagic.pipeline.ConsolePipeline;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
@CommonsLog
public class IqiyiAutoSpiderIT {

	@Autowired
	private IqiyiAutoSpider iqiyiPageSpider;
	
	private final static String NETWORK_MOVIE_URL = "http://list.iqiyi.com/www/1/------27401-------4-1-1-iqiyi--.html";
	private final static String DRAMA_URL = "http://list.iqiyi.com/www/2/15-----------2017--4-1-1-iqiyi--.html";
	private final static String VARIETY_URL = "http://list.iqiyi.com/www/6/-------------11-1-1-iqiyi--.html";
	private final static String KID_ANIME ="http://list.iqiyi.com/www/15/1259-31020------------24-2-1-iqiyi--.html";
	private final static String FOREIGN_KID_ANIME="http://list.iqiyi.com/www/15/1261-31020------------24-1-1-iqiyi--.html";
	final String EA_FOREIGN_KID_ANIME = "http://list.iqiyi.com/www/15/1261-31020------------24-1-1-iqiyi--.html";
	final String JAPAN_FOREIGN_KID_ANIME="http://list.iqiyi.com/www/15/1262-31020------------24-1-1-iqiyi--.html";
	final String KOREA_FOREIGN_KID_ANIME ="http://list.iqiyi.com/www/15/28933-31020------------24-1-1-iqiyi--.html";
	final String HK_FOREIGN_KID_ANIME="http://list.iqiyi.com/www/15/1260-31020------------24-1-1-iqiyi--.html";
	final String OTHER_FOREIGN_KID_ANIME ="http://list.iqiyi.com/www/15/1263-31020------------24-1-1-iqiyi--.html";
	final String EA_KID_ANIME_MOVIE = "http://list.iqiyi.com/www/15/31013-1261------------24-1-1-iqiyi--.html";
	final String CN_KID_ANIME_MOVIE="http://list.iqiyi.com/www/15/31013-1259------------24-1-1-iqiyi--.html";
	final String JP_KID_ANIME_MOVIE="http://list.iqiyi.com/www/15/31013-1262------------24-1-1-iqiyi--.html";

	final String JP_ANIME_MOVIE="http://list.iqiyi.com/www/4/38----30220---------4-1-1-iqiyi--.html";
	final String JP_ANIME_MOVIE2="https://pcw-api.iqiyi.com/search/video/videolists?access_play_control_platform=14&channel_id=4&data_type=1&from=pcw_list&is_album_finished=&is_purchase=&key=&market_release_date_level=&mode=4&pageNum=1&pageSize=48&site=iqiyi&source_type=&three_category_id=38";
	private DelayRequest delayRequest;
	final String url = "http://list.iqiyi.com/www/4/38-30220------------11-1-1-iqiyi--.html";
	final String japan ="https://pcw-api.iqiyi.com/video/video/hotplaytimes/208157001";

	@Test
	public void testContext() {
		Assert.notNull(iqiyiPageSpider);
	}

	@Autowired
	IqiyiHotPlayTimesSpider timesSpider;
	@Test
	public void testAutoFind()  {
		Job job = new Job();
		job.setUrl(japan);
		// request
		delayRequest = new DelayRequest(job);
		TestPipeline testPipeline = new TestPipeline();
		SpiderEngine.create(timesSpider).addPipeline(testPipeline).addPipeline(new ConsolePipeline()).addRequest(delayRequest).run();

		ResultItems resultItems = testPipeline.getResultItems();

		List<Job> jobs = resultItems.get(Job.class.getSimpleName());
		List<Show> shows = resultItems.get(Show.class.getSimpleName());
		
		Assert.isTrue(CollectionUtils.isNotEmpty(jobs), "爱奇艺自动发现没有找到新的Jobs,或者失败!");
		Assert.isTrue(CollectionUtils.isNotEmpty(shows), "爱奇艺自动发现没有找到新的Jobs,或者失败!");
	}
}
