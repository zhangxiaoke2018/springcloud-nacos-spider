package com.jinguduo.spider.common.net;

import java.io.IOException;
import java.net.Socket;

import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.protocol.HttpContext;

import com.jinguduo.spider.common.proxy.ProxyHelper;
import com.jinguduo.spider.data.table.Proxy;

import lombok.extern.apachecommons.CommonsLog;

@CommonsLog
public class HttpConnectionSocketFactory extends PlainConnectionSocketFactory {

    public static final HttpConnectionSocketFactory INSTANCE = new HttpConnectionSocketFactory();

    public static HttpConnectionSocketFactory getSocketFactory() {
        return INSTANCE;
    }

    public HttpConnectionSocketFactory() {
        super();
    }

    @Override
    public Socket createSocket(final HttpContext context) throws IOException {
        Proxy proxy = (Proxy) context.getAttribute(ProxyHelper.PROXY_SOCKS);
        if (proxy == null) {
            return new Socket();
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Use Proxy: " + proxy.toString());
            }
            return ProxyHelper.createSocket(proxy);
        }
    }
}
