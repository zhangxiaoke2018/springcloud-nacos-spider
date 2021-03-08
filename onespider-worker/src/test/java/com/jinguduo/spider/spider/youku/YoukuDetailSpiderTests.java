package com.jinguduo.spider.spider.youku;

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
import com.jinguduo.spider.data.table.AdLinkedVideoInfos;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.ResultItems;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class YoukuDetailSpiderTests {

    @Autowired
    private YoukuDetailSpider youkuDetailSpider;

    @Test
    public void testProcessMain() throws Exception {
        Job job = new Job("https://list.youku.com/show/id_z4852efbfbd09efbfbd36.html");
        job.setCode("XMjQ3NTc0MDA4OA==");

        // request
        DelayRequest delayRequest = new DelayRequest(job);
        // page
        String htmlFile = "/html/youku/detail.html";
        String rawText = IoResourceHelper.readResourceContent(htmlFile);
        
        Page page = new Page();
        page.setRequest(delayRequest);
        page.setStatusCode(HttpStatus.OK.value());
        page.setRawText(rawText);
        
        youkuDetailSpider.process(page);

        ResultItems resultItems = page.getResultItems();
        Assert.notNull(resultItems, "Bad");

        List<Job> jobs = resultItems.get(Job.class.getSimpleName());
        Assert.notEmpty(jobs, "Bad");
    }
    
    @Test
    public void testProcessMain2() throws Exception {
        Job job = new Job("http://v.youku.com/v_show/id_XMjQ3NTc0MDA4OA==.html");
        job.setCode("XMjQ3NTc0MDA4OA==");

        // request
        DelayRequest delayRequest = new DelayRequest(job);
        // page
        String htmlFile = "/html/youku/detail.bigview.html";
        String rawText = IoResourceHelper.readResourceContent(htmlFile);
        
        Page page = new Page();
        page.setRequest(delayRequest);
        page.setStatusCode(HttpStatus.OK.value());
        page.setRawText(rawText);
        
        youkuDetailSpider.process(page);

        ResultItems resultItems = page.getResultItems();
        Assert.notNull(resultItems, "Bad");

        List<Job> jobs = resultItems.get(Job.class.getSimpleName());
        Assert.notEmpty(jobs, "Bad");
    }
    
    @Test
    public void testProcessMain3() throws Exception {
        Job job = new Job("http://v.youku.com/v_show/id_XMzU2Njg5MTEzNg==.html");
        job.setCode("XMzU2Njg5MTEzNg==");

        // request
        DelayRequest delayRequest = new DelayRequest(job);
        // page
        String htmlFile = "/html/youku/detail.20180427.html";
        String rawText = IoResourceHelper.readResourceContent(htmlFile);
        
        Page page = new Page();
        page.setRequest(delayRequest);
        page.setStatusCode(HttpStatus.OK.value());
        page.setRawText(rawText);
        
        youkuDetailSpider.process(page);

        ResultItems resultItems = page.getResultItems();
        Assert.notNull(resultItems, "Bad");

        List<Job> jobs = resultItems.get(Job.class.getSimpleName());
        Assert.notEmpty(jobs, "Bad");
    }
    
    @Test
    public void testProcessMain4() throws Exception {
        Job job = new Job("http://v.youku.com/v_show/id_XNTk5NzI1Mjg0.html");
        job.setCode("XNTk5NzI1Mjg0");

        // request
        DelayRequest delayRequest = new DelayRequest(job);
        // page
        String htmlFile = "/html/youku/detail.20180529.html";
        String rawText = IoResourceHelper.readResourceContent(htmlFile);
        
        Page page = new Page();
        page.setRequest(delayRequest);
        page.setStatusCode(HttpStatus.OK.value());
        page.setRawText(rawText);
        
        youkuDetailSpider.process(page);

        ResultItems resultItems = page.getResultItems();
        Assert.notNull(resultItems, "Bad");

        List<Job> jobs = resultItems.get(Job.class.getSimpleName());
        Assert.notEmpty(jobs, "Bad");
    }
    
    @Test
    public void testProcessMain5() throws Exception {
        Job job = new Job("https://v.youku.com/v_show/id_XMjUyNTAwNjI3Ng==.html");
        job.setCode("XMjUyNTAwNjI3Ng==");

        // request
        DelayRequest delayRequest = new DelayRequest(job);
        // page
        String htmlFile = "/html/youku/detail.bigview.2.html";
        String rawText = IoResourceHelper.readResourceContent(htmlFile);
        
        Page page = new Page();
        page.setRequest(delayRequest);
        page.setStatusCode(HttpStatus.OK.value());
        page.setRawText(rawText);
        
        youkuDetailSpider.process(page);

        ResultItems resultItems = page.getResultItems();
        Assert.notNull(resultItems, "Bad");

        List<Job> jobs = resultItems.get(Job.class.getSimpleName());
        Assert.notEmpty(jobs, "Bad");
    }
    
}
