package com.jinguduo.spider.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

import com.jinguduo.spider.StoreMainApplication;
import com.jinguduo.spider.data.table.Seed;

import lombok.extern.apachecommons.CommonsLog;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@CommonsLog
public class SeedServiceTests {

    @Autowired
    private SeedService seedService;

    @Test
    public void testContent(){
        Assert.notNull(seedService);}

//    @Before
    public void setUp(){
    }

    @Test
    public void testFindCode(){

        String code = seedService.findCode("http://tieba.baidu.com/sdfklj=sdf&workd=awerfl#sdfsdfasf");
        String code2 = seedService.findCode("http://tieba.baidu.com/sdfklj=sdf&workd=awerfl#sdfae5tgdfsdsf");
        log.info(code);
        log.info(code2);
        Assert.isTrue(!code.equals(code2));

    }
    @Test
    public void testAddSeed(){

        String url = "http://tieba.baidu.com/f?ie=utf-8&kw=侯京健 &fr=search";

        Seed  seed = new Seed();
        seed.setUrl(url);
        seed.setCode("aaaaaaaaaaa");
        seed.setFrequency(100);
        seed.setPlatformId(1);

        Seed s = seedService.addSeed(seed);

        Assert.isTrue("http://tieba.baidu.com/f?ie=utf-8&kw=%E4%BE%AF%E4%BA%AC%E5%81%A5&fr=search".equals(s.getUrl()));

        log.info(s);

    }







}
