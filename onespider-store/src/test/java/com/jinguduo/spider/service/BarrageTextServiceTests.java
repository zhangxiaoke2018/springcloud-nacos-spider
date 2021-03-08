package com.jinguduo.spider.service;

import java.io.IOException;
import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.jinguduo.spider.StoreMainApplication;
import com.jinguduo.spider.data.text.BarrageText;


@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class BarrageTextServiceTests {

    @Autowired
    private BarrageTextService barrageTextService;

    @Test
    public void testWrite() throws IOException {
        BarrageText barrageText = new BarrageText();
        barrageTextService.save(Arrays.asList(barrageText));
       
    }

}
