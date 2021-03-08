package com.jinguduo.spider.common.proxy;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import org.apache.http.HttpHost;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.Assert;

import com.jinguduo.spider.data.table.Proxy;

@ActiveProfiles("test")
public class ProxyHelperTests {

    @Test
    public void testValidate() throws NumberFormatException, UnknownHostException {
        InetSocketAddress host = new InetSocketAddress(InetAddress.getByName("139.129.105.205"), 80);
        boolean v = ProxyHelper.validateSocketConnection(host);
        Assert.isTrue(v, "bad");
    }
    
    @Ignore("handwork")
    @Test
    public void testValidateProxy() throws NumberFormatException, UnknownHostException {
        Proxy proxy = new Proxy();
        proxy.setHost("119.5.0.60:808");
        boolean v = ProxyHelper.validateProxy(proxy);
        Assert.isTrue(v, "bad");
    }
    
    @Ignore("long time")
    @Test
    public void testSpeed() throws IOException {
        long ts = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            InetSocketAddress host = new InetSocketAddress(InetAddress.getByName("www.baidu.com"), 80);
            //HttpHost host = new HttpHost(InetAddress.getByName("139.129.105.205"), Integer.valueOf("80"));
            boolean v = ProxyHelper.validateSocketConnection(host);
            Assert.isTrue(v, "bad");
        }
        System.out.println(System.currentTimeMillis() - ts);
    }

    @Ignore("handwork")
    @Test
    public void testHttpHost() {
        HttpHost host = new HttpHost("58.52.201.117", 8080);
        Assert.notNull(host.getAddress(), "bad");
        Assert.notNull(host.getAddress().getHostAddress(), "bad");
    }
    
    @Test
    public void testProxyBean() {
        Proxy proxy = new Proxy();
        proxy.setHost("127.0.0.1:808");
        
        Assert.isTrue("127.0.0.1".equals(proxy.getHostName()), "bad");
        Assert.isTrue(808 == proxy.getPort(), "bad");
    }
}
