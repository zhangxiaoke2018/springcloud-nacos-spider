package com.jinguduo.spider.common.net;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.Assert;

import com.jinguduo.spider.common.net.FractionDnsResolver;

import lombok.extern.apachecommons.CommonsLog;

@CommonsLog
@ActiveProfiles("test")
public class FractionDnsResolverTests {

    @Test
    public void testResolve() throws UnknownHostException {
        FractionDnsResolver resolver = new FractionDnsResolver();
        InetAddress[] address = resolver.resolve("v.qq.com");
        Assert.notEmpty(address, "The address maybe null.");
        for (InetAddress addr : address) {
            log.info(addr.toString());
        }
    }
}
