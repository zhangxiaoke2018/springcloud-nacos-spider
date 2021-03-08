package com.jinguduo.spider.service;

import com.jinguduo.spider.StoreMainApplication;
import com.jinguduo.spider.data.table.Customer360Logs;
import com.jinguduo.spider.data.table.Index360Logs;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

/**
 * I'm a cute code dog
 * User: LingChen
 * Date:2017/7/31
 * Time:14:44
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class Customer360ServiceTests {
    @Autowired
    Customer360LogService customer360LogService;

    @Test
    public void test() {

          System.out.println("********");

            Customer360Logs c = new Customer360Logs();
            c.setCode("63990");
            c.setProvince("全国1");
            c.setDay(new Date());
            c.setMaleRatio((byte) 108);
            customer360LogService.save(c);
        System.out.println("finish");

    }
}
