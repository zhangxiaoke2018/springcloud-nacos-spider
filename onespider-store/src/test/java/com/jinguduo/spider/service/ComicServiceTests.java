package com.jinguduo.spider.service;

import com.jinguduo.spider.StoreMainApplication;
import com.jinguduo.spider.data.table.Comic;

import lombok.extern.apachecommons.CommonsLog;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @DATE 07/08/2017 17:53
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@CommonsLog
public class ComicServiceTests {

    @Autowired
    private ComicService comicService;


    @Test
    public void insertOrUpdateTest(){

        Comic comic = new Comic();
        comic.setPlatformId(51);
        comic.setCode("wb-69000");
        comic.setIntro("adfadfadfadsfadsfadsf");

        comicService.insertOrUpdate(comic);

        Comic c = new Comic();
        c.setSubject("水晶鞋");
        c.setCode("wb-69000");
        c.setIntro("adfadfadfadsfadsfadsf");

        Comic comic1 = comicService.insertOrUpdate(c);

        Assert.isTrue(comic1.getSubject().equals("妖魔鬼怪"));

        System.out.println(comic1);


    }



}
