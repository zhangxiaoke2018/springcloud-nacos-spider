package com.jinguduo.spider.common.thread;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.Assert;

import com.jinguduo.spider.common.thread.Timeout;

import lombok.extern.apachecommons.CommonsLog;

@CommonsLog
@ActiveProfiles("test")
public class TimeoutTests {

    @Test
    public void testRunning() throws InterruptedException, ExecutionException, TimeoutException {
        Timeout.execute(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }, 3, TimeUnit.SECONDS);
    }
    
    
    @Test
    public void testTimeout() throws InterruptedException, ExecutionException, TimeoutException {
        try {
            Timeout.execute(new Runnable() {
                public void run() {
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        log.error(e.getMessage(), e);
                    }
                }
            }, 1, TimeUnit.SECONDS);
            
        } catch (Exception e) {
            Assert.isInstanceOf(TimeoutException.class, e);
        }
    }
}
