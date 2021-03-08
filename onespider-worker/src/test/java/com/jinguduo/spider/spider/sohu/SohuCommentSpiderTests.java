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
import com.jinguduo.spider.data.table.CommentLog;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.ResultItems;

@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class SohuCommentSpiderTests {

    @Autowired
    private SohuCommentSpider sohuCommentSpider;

    @Test
    public void testCommentCount() throws Exception {
        String VIDEO_LIST_URL = "http://changyan.sohu.com/api/2/topic/load?client_id=cyqyBluaj&topic_url=http://tv.sohu.com/20160504/n447639692.shtml";
        //loading job
        Job job = new Job(VIDEO_LIST_URL);
        //simulate request
        DelayRequest dr = new DelayRequest(job);
        
        Page page = new Page();
        page.setRequest(dr);
        page.setStatusCode(HttpStatus.OK.value());
        String RAW_TEXT = IoResourceHelper.readResourceContent("/json/SohuCommentCountApiSpiderTests.json");
        page.setRawText(RAW_TEXT);

        sohuCommentSpider.process(page);

        ResultItems resultItems = page.getResultItems();
        
        List<CommentLog> commentLogs = resultItems.get(CommentLog.class.getSimpleName());
        Assert.notEmpty(commentLogs, "Bad");
    }

}
