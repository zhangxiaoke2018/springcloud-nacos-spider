package com.jinguduo.spider.data.text;

import org.junit.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.Assert;

import com.jinguduo.spider.data.text.BarrageText;

@ActiveProfiles("test")
public class BarrageTextTests {

    @Test
    public void testSetContent() {
        BarrageText barrageText = new BarrageText();
        
        Assert.isNull(barrageText.getContent());
        
        barrageText.setContent("");
        Assert.notNull(barrageText.getContent());
        
        barrageText.setContent("    ");
        Assert.isTrue("".equals(barrageText.getContent()));
        
        barrageText.setContent("\n");
        Assert.isTrue("".equals(barrageText.getContent()));
        
        barrageText.setContent("\t\r\n");
        Assert.isTrue("".equals(barrageText.getContent()));
    }
}
