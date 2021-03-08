package com.jinguduo.spider.spider.sohu;


import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.common.util.IoResourceHelper;
import com.jinguduo.spider.data.table.Show;
import com.jinguduo.spider.data.table.ShowLog;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.ResultItems;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class SohuSpiderTests {
	
    @Autowired
    private SohuSpider sohuSpider;

    @Test
    public void testNetMovie1() throws Exception {
        String url = String.format("http://tv.sohu.com/item/MTIwNDM1Ng==.html");
        
    	String htmlFile = "/html/sohu/netmovie-1.html";
    	String rawText = IoResourceHelper.readResourceContent(htmlFile);
    	
    	Job job = new Job(url);
        DelayRequest delayRequest = new DelayRequest(job);
        
        Page page = new Page();
        page.setRequest(delayRequest);
        page.setStatusCode(HttpStatus.OK.value());
        page.setRawText(rawText);

        sohuSpider.process(page);
        
        ResultItems resultItems = page.getResultItems();
        Assert.notNull(resultItems, "bad");
        
        List<Job> jobs = resultItems.get(Job.class.getSimpleName());
        Assert.notEmpty(jobs, "bad");
    }
    
    @Test
    public void testNetDrama1() throws Exception {
        String url = String.format("http://tv.sohu.com/s2018/dsjphgl/");
        
    	String htmlFile = "/html/sohu/netdrama-1.html";
    	String rawText = IoResourceHelper.readResourceContent(htmlFile);
    	
    	Job job = new Job(url);
    	job.setCode("9485021");
        DelayRequest delayRequest = new DelayRequest(job);
        
        Page page = new Page();
        page.setRequest(delayRequest);
        page.setStatusCode(HttpStatus.OK.value());
        page.setRawText(rawText);

        sohuSpider.process(page);
        
        ResultItems resultItems = page.getResultItems();
        Assert.notNull(resultItems, "bad");
        
        List<Job> jobs = resultItems.get(Job.class.getSimpleName());
        Assert.notEmpty(jobs, "bad");
        
        Assert.isTrue(jobs.stream().anyMatch(e -> e.getUrl().contains("9485021")), "提取code成功");
    }
    
    @Test
    public void testNetVariety1() throws Exception {
        String url = String.format("https://tv.sohu.com/s2018/zysybwnhhj/");
        
    	String htmlFile = "/html/sohu/netvariety-1.html";
    	String rawText = IoResourceHelper.readResourceContent(htmlFile);
    	
    	Job job = new Job(url);
    	job.setCode("9457553");
        DelayRequest delayRequest = new DelayRequest(job);
        
        Page page = new Page();
        page.setRequest(delayRequest);
        page.setStatusCode(HttpStatus.OK.value());
        page.setRawText(rawText);

        sohuSpider.process(page);
        
        ResultItems resultItems = page.getResultItems();
        Assert.notNull(resultItems, "bad");
        
        List<Job> jobs = resultItems.get(Job.class.getSimpleName());
        Assert.notEmpty(jobs, "bad");
        
        Assert.isTrue(jobs.stream().anyMatch(e -> e.getUrl().contains("9457553")), "提取code成功");
    }

    @Test
    public void testPlayCount() throws Exception {
    	String vid = "9097254";
    	String jsonFile = "/json/sohu/PlayCountSpiderTests.json";
        String rawText = IoResourceHelper.readResourceContent(jsonFile);
        String url = String.format("http://tv.sohu.com/item/VideoServlet?source=sohu&id=%s&year=2016&month=0&page=0", vid);
        
    	Job playCountJob = new Job(url);
        DelayRequest playCountDelayRequest = new DelayRequest(playCountJob);
        
        Page page = new Page();
        page.setRequest(playCountDelayRequest);
        page.setStatusCode(HttpStatus.OK.value());
        page.setRawText(rawText);

        sohuSpider.process(page);

        ResultItems resultItems = page.getResultItems();
        Assert.notNull(resultItems, "bad");
        
        Assert.notNull(resultItems.get(Show.class.getSimpleName()), "bad");
        Assert.notNull(resultItems.get(ShowLog.class.getSimpleName()), "bad");
        
        List<ShowLog> showLogList = resultItems.get(ShowLog.class.getSimpleName());
        Assert.isTrue(showLogList.get(0).getPlayCount() == 823879, "bad");
    }
}
