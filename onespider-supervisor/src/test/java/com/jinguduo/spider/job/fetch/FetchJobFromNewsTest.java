package com.jinguduo.spider.job.fetch;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

/**
 *  @Author: gaozl
 *  @Date: 2020/10/15 2:16 下午
 *
 */
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@SpringBootTest
public class FetchJobFromNewsTest {
    @Autowired
    private  FetchJobFromNews fetchJobFromNews;
    @Test
    public void testFetchJobFromNews01(){
        fetchJobFromNews.process();
    }
}
