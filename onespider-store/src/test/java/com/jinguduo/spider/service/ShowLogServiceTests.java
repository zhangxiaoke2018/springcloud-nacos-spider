package com.jinguduo.spider.service;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

import com.jinguduo.spider.StoreMainApplication;
import com.jinguduo.spider.data.table.ShowLog;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 16/7/20 下午3:05
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class ShowLogServiceTests {

    @Autowired
    private ShowLogService showLogService;

    @Before
    public void setUp(){
        ShowLog showLog = new ShowLog();
        showLog.setCode("a");
        showLog.setPlayCount(4564L);
        ShowLog showLog2 = new ShowLog();
        showLog2.setCode("a");
        showLog2.setPlayCount(4345564L);
        showLogService.insert(showLog);
        showLogService.insert(showLog2);
    }

    @Test
    public void testContent(){
        Assert.notNull(showLogService);}

    @Test
    public void testTop24(){
        List<ShowLog> showLogs = showLogService.find("a");
        System.out.println(showLogs);
    }

}
