package com.jinguduo.spider.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.jinguduo.spider.StoreMainApplication;

import lombok.extern.apachecommons.CommonsLog;

/**
 * Created by jack on 2016/12/21.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@CommonsLog
public class ActorServiceTests {

    @Autowired
    ActorService actorService;

    @Test
    public void testExistActor(){
        boolean boo = actorService.exist("9cc8d46af29f4f9c56b98197ccb9e677", "许景媛");
        log.warn("exist actor:" + boo);
    }
}
