package com.jinguduo.spider.common.type;

import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.Assert;

import com.jinguduo.spider.common.type.TimedRateQuota;

@ActiveProfiles("test")
public class TimedRateQuotaTests {
    
    private int duration = 5;
    private int limit = 3;

    @Test
    public void testIsAboved() throws InterruptedException {
        TimedRateQuota quota = new TimedRateQuota(duration, TimeUnit.SECONDS, limit);
        
        String key = "key";
        
        for (int i = 0; i < limit; i++) {
            Assert.isTrue(quota.isAboved(key) == false);
        }
        Assert.isTrue(quota.isAboved(key) == true);
        Assert.isTrue(quota.isAboved(key) == true);
        
        Thread.sleep(TimeUnit.SECONDS.toMillis(duration));

        for (int i = 0; i < limit; i++) {
            Assert.isTrue(quota.isAboved(key) == false);
        }
        Assert.isTrue(quota.isAboved(key) == true);
    }
}
