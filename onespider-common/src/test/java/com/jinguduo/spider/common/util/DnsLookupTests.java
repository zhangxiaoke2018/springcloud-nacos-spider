package com.jinguduo.spider.common.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.Assert;

import lombok.extern.apachecommons.CommonsLog;

@CommonsLog
@ActiveProfiles("test")
public class DnsLookupTests {

    @Test
    public void testResolveQq1() throws UnknownHostException {
        InetAddress[] addresses = DnsLookup.resolve("qq.com", "114.114.114.114");
        Assert.notEmpty(addresses, "DnsLookup Result maybe null.");
        for (InetAddress addr : addresses) {
            log.info(addr.toString());
        }
    }
    
    @Test
    public void testResolveQq2() throws UnknownHostException {
        InetAddress[] addresses = DnsLookup.resolve("v.qq.com", "114.114.114.114");
        Assert.notEmpty(addresses, "DnsLookup Result maybe null.");
        for (InetAddress addr : addresses) {
            log.info(addr.toString());
        }
    }
    
    @Test
    @Ignore("bad")
    public void testResolvePptv() throws UnknownHostException {
        InetAddress[] addresses = DnsLookup.resolve("v.pptv.com", "114.114.114.114");
        Assert.notEmpty(addresses, "DnsLookup Result maybe null.");
        for (InetAddress addr : addresses) {
            log.info(addr.toString());
        }
    }
    
    @Test
    public void testResolveBaidu() throws UnknownHostException {
        for (int i = 0; i < 10; i++) {
            InetAddress[] addresses = DnsLookup.resolve("www.baidu.com", "114.114.114.114");
            Assert.notEmpty(addresses, "DnsLookup Result maybe null.");
            for (InetAddress addr : addresses) {
                log.info(addr.toString());
            }
        }
    }
}
