package com.jinguduo.spider.common.net;

import java.io.IOException;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContexts;

import com.jinguduo.spider.common.proxy.ProxyHelper;
import com.jinguduo.spider.data.table.Proxy;

import lombok.extern.apachecommons.CommonsLog;

@CommonsLog
public class FakedHttpsConnectionSocketFactory extends SSLConnectionSocketFactory {
    
    public final static FakedHttpsConnectionSocketFactory INSTANCE = new FakedHttpsConnectionSocketFactory();
    
    public static FakedHttpsConnectionSocketFactory getSocketFactory() {
        return INSTANCE;
    }
    
    public FakedHttpsConnectionSocketFactory() {
        this(createFakedSslContext());
    }

    public FakedHttpsConnectionSocketFactory(final SSLContext sslContext) {
        // You may need this verifier if target site's certificate is not secure
        super(sslContext);
    }

    @Override
    public Socket createSocket(final HttpContext context) throws IOException {
        Proxy proxy = (Proxy) context.getAttribute(ProxyHelper.PROXY_SOCKS);
        if (proxy == null) {
            return super.createSocket(context);
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Use Proxy: " + proxy.toString());
            }
            return ProxyHelper.createSocket(proxy);
        }
    }

//    @Override
//    public Socket connectSocket(int connectTimeout, Socket socket, HttpHost host, InetSocketAddress remoteAddress,
//                                InetSocketAddress localAddress, HttpContext context) throws IOException {
//        // Convert address to unresolved
//        InetSocketAddress unresolvedRemote = InetSocketAddress
//                .createUnresolved(host.getHostName(), remoteAddress.getPort());
//        return super.connectSocket(connectTimeout, socket, host, unresolvedRemote, localAddress, context);
//    }
    
    /**
     * 忽略SSL证书
     * @return
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     */
    protected static SSLContext createFakedSslContext() {
        SSLContext sc;
        try {
            sc = SSLContext.getInstance("TLSv1.2");
            FakedX509TrustManager trustManager = new FakedX509TrustManager();
            sc.init(null, new TrustManager[] { trustManager }, null);
            return sc;
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            log.error(e.getMessage(), e);
            return SSLContexts.createDefault();
        }
    }
    /**
     * 实现一个X509TrustManager接口，用于绕过验证，不用修改里面的方法
     */
    static class FakedX509TrustManager implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }
        
        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }
        
        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    };
}
