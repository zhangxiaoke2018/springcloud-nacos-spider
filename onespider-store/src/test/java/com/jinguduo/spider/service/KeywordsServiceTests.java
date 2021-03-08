package com.jinguduo.spider.service;

import com.jinguduo.spider.StoreMainApplication;
import com.jinguduo.spider.data.table.Keywords;
import com.jinguduo.spider.data.table.RKeywordsLinked;
import com.jinguduo.spider.db.repo.KeyWordsRepo;
import com.jinguduo.spider.db.repo.RKeywordsLinkedRepo;

import lombok.extern.apachecommons.CommonsLog;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@CommonsLog
public class KeywordsServiceTests {

    @Autowired
    private KeywordsService keywordsService;

    @Autowired
    private KeyWordsRepo keyWordsRepo;

    @Autowired
    private RKeywordsLinkedRepo rKeywordsLinkedRepo;

    @Test
    public void testContent(){
        Assert.notNull(keywordsService);}

//    @Before
    public void setUp(){
    }

    @Test
    public void testFindByLinkedId(){
        Keywords k1 = new Keywords();
        k1.setKeyword("老九门");

        Keywords k2 = new Keywords();
        k2.setKeyword("老九门番外篇");

        Integer r1Id = keyWordsRepo.save(k1).getId();
        Integer r2Id = keyWordsRepo.save(k2).getId();

        RKeywordsLinked rk1 = new RKeywordsLinked();
        rk1.setLinkedId(123);
        rk1.setKeywordsId(r1Id);

        RKeywordsLinked rk2 = new RKeywordsLinked();
        rk2.setLinkedId(123);
        rk2.setKeywordsId(r2Id);

        rKeywordsLinkedRepo.save(rk1);
        rKeywordsLinkedRepo.save(rk2);


        List<Keywords> keywordses = keywordsService.findByLinkedId(123);

        Assert.isTrue(keywordses.size() == 3);

        keywordses.forEach(k -> log.info(k.toString()));

    }

    @Test
    public void testInsertOrUpdate(){

        Keywords k1 = new Keywords();
        k1.setKeyword("老九门");
        k1.setLinkedId(123);
        k1.setCategory(1);
        Integer k2Id = keyWordsRepo.save(k1).getId();
        rKeywordsLinkedRepo.save(new RKeywordsLinked(k1.getLinkedId(),k2Id));

        Keywords k2 = new Keywords();
        k2.setKeyword("老九门番外篇");
        k2.setLinkedId(123);
        k2.setGreatest(1);

        Keywords keywords = keywordsService.insertOrUpdate(k2);

        keywordsService.del(k2Id, 123);

        List<Keywords> keywordses = keywordsService.findByLinkedId(123);
        Assert.isTrue(keywordses.size() == 1);
        Assert.isTrue(keywordses.get(0).getGreatest() == 1);
        keywordses.forEach(k -> log.info(k.toString()));

    }





}
