package com.jinguduo.spider.spider.iqiyi;


import com.jinguduo.spider.cluster.engine.SpiderEngine;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.pipeline.TestPipeline;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.data.table.CommentLog;
import com.jinguduo.spider.data.table.ShowLog;
import com.jinguduo.spider.webmagic.ResultItems;
import lombok.extern.apachecommons.CommonsLog;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

import java.util.List;

/**
 * 
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @author liuxinglong
 * @DATE 2017年4月14日 下午3:01:54
 *
 */
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
@CommonsLog
public class IqiyiTotalPlayCountSpiderIT {

    @Autowired
    private IqiyiTotalPlayCountSpider iqiyiTotalPlayCountSpider;

    @Before
    public void setup()  {

    }

    @Test
    public void testContext() {
        Assert.notNull(iqiyiTotalPlayCountSpider);
    }



    @Test
    public void testTotalPlayCount()  {
        TestPipeline testPipeline = new TestPipeline();
        Job job = new Job();
        job.setUrl("http://iface2.iqiyi.com/views/3.0/player_tabs?app_k=204841020bd16e319191769268fb56ee&app_v=6.8.3&platform_id=11&dev_os=4.4.4&dev_ua=Nexus+5&net_sts=1&qyid=358239054455227&secure_p=GPad&secure_v=1&core=1&dev_hw=%7B%22mem%22%3A%22457.3MB%22%2C%22cpu%22%3A0%2C%22gpu%22%3A%22%22%7D&scrn_sts=0&scrn_res=1080,1794&scrn_dpi=480&page_part=2&album_id=205642201");
        job.setCode("123456");
        DelayRequest delayRequest = new DelayRequest(job);
        SpiderEngine.create(iqiyiTotalPlayCountSpider).addPipeline(testPipeline).addRequest(delayRequest).run();

        ResultItems resultItems = testPipeline.getResultItems();
        List<ShowLog> showLogs = resultItems.get(ShowLog.class.getSimpleName());
        Assert.notNull(showLogs);
        log.debug(showLogs);
    }
}
