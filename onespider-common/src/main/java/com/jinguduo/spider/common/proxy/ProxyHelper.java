package com.jinguduo.spider.common.proxy;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketImpl;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpHost;
import org.apache.http.ParseException;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import com.jinguduo.spider.common.keeper.ResourceKeeper;
import com.jinguduo.spider.common.keeper.ResourceKeeperBuilder;
import com.jinguduo.spider.common.type.TimingQuota;
import com.jinguduo.spider.data.table.Proxy;

import lombok.extern.apachecommons.CommonsLog;

@CommonsLog
public class ProxyHelper {
    
    public final static String PROXY_SOCKS = "proxy.socks";
    
    private final static TargetHost TARGET_HOST_1 = TargetHost.builder()
            .url("https://www.baidu.com").text("百度一下").build();
    
    private final static TargetHost TARGET_HOST_2 = TargetHost.builder()
            .url("https://v.qq.com").text("腾讯视频").build();
    
    public static boolean validateSocketConnection(Proxy proxy) {
        InetSocketAddress socketAddr = new InetSocketAddress(proxy.getHostName(), proxy.getPort());
        return validateSocketConnection(socketAddr);
    }
    
    // 建立socket连接的超时时间，
    //  超时抛出java.net.SocketTimeoutException
    private final static int SOCKET_CONNECTION_TIMEOUT = 1500;

    public static boolean validateSocketConnection(InetSocketAddress socketAddress) {
        boolean isReachable = false;
        try (Socket socket = new Socket()) {
            socket.connect(socketAddress, SOCKET_CONNECTION_TIMEOUT);
            if (log.isDebugEnabled()) {
                log.debug("SUCCESS - connection established! Local: " 
                        + socket.getLocalAddress().getHostAddress() 
                        + " remote: " 
                        + socketAddress.getHostString());
            }
            isReachable = true;
            
        } catch (IOException e) {
            // ignore
            if (log.isDebugEnabled()) {
                log.debug("FAILRE - CAN not connect! remote: "
                        + socketAddress.getHostString());
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return isReachable;
    }

    // 从连接池中获取连接的超时时间，超过该时间未拿到可用连接，
    //   会抛出org.apache.http.conn.ConnectionPoolTimeoutException: Timeout waiting for connection from pool
    private final static int CONNECTION_REQUEST_TIMEOUT = 1000;
    
    // 连接上服务器(握手成功)的时间，超出该时间抛出connect timeout
    private final static int CONNECTION_TIMEOUT = 3000;
    
    // 服务器返回数据(response)的时间，超过该时间抛出read timeout
    private final static int SOCKET_TIMEOUT = 10000;
    
    private final static int MAX_REDIRECTS = 5;
    
    private static ResourceKeeper<CloseableHttpClient> keeper = ResourceKeeperBuilder.custom()
    		.quota(new TimingQuota(15, TimeUnit.MINUTES))
    		.resource(new HttpClientKeeper())
    		.build();
    
    private static CloseableHttpClient getHttpClient() {
    	return keeper.get();
    }
    
    public static boolean validateProxy(Proxy proxy) {
        boolean isReachabled = validateProxy(proxy, TARGET_HOST_1);
        if (isReachabled) {
            isReachabled = validateProxy(proxy, TARGET_HOST_2);
        }
        return isReachabled;
    }
    
    private static boolean validateProxy(Proxy proxy, TargetHost host) {
        boolean isReachable = false;
        switch (proxy.getPtype()) {
        case http:
        case https:
        case socks4:
        case socks5:
            isReachable = validateProxyImpl(proxy, proxy.getPtype(), host);
            break;
        case socks:
            isReachable = validateSocksProxy(proxy, host);
            break;
        case unknown:
        default:
            isReachable = validateSocksProxy(proxy, host);
            if (!isReachable) {
                isReachable = validateProxyImpl(proxy, ProxyType.http, host);
            }
        }
        return isReachable;
    }

    private static boolean validateSocksProxy(Proxy proxy, TargetHost host) {
        boolean isReachable = false;
        proxy.setPtype(ProxyType.socks4);  // for #ProxyHelper.setUseSocks4()#
        isReachable = validateProxyImpl(proxy, ProxyType.socks4, host);
        if (!isReachable) {
            proxy.setPtype(ProxyType.unknown);
            isReachable = validateProxyImpl(proxy, ProxyType.socks5, host);
        }
        return isReachable;
    }
    
    private static boolean validateProxyImpl(Proxy proxy, ProxyType ptype, TargetHost host) {
        boolean isReachable = false;
        HttpGet request = null;
        try {
            HttpHost target = HttpHost.create(host.getUrl());
            HttpClientContext context = HttpClientContext.create();
            Builder requestConfig = RequestConfig.custom()
                    .setConnectionRequestTimeout(CONNECTION_REQUEST_TIMEOUT)
                    .setConnectTimeout(CONNECTION_TIMEOUT)
                    .setSocketTimeout(SOCKET_TIMEOUT)
                    .setMaxRedirects(MAX_REDIRECTS)
                    .setCookieSpec(CookieSpecs.IGNORE_COOKIES);
            
            switch (ptype) {
            case http:
            case https:
                requestConfig.setProxy(createHttpHost(proxy));
                break;
            case socks4:
            case socks5:
                context.setAttribute(PROXY_SOCKS, proxy);
                break;
            default:
                log.error("The ptype maybe error");
                return isReachable;
            }
            
            request = new HttpGet("/");
            request.addHeader("Connection", "close");
            request.setConfig(requestConfig.build());
            
            if (log.isDebugEnabled()) {
                log.debug("Request " + request.getRequestLine() + " to " + target + " via " + proxy);
            }

            try (CloseableHttpResponse response = getHttpClient().execute(target, request, context)) {
                if (response.getStatusLine().getStatusCode() == 200) {
                    isReachable = validateContent(response, host.getText());
                    if (isReachable) {
                        proxy.setPtype(ptype);
                    }
                }
            }
        } catch (IOException e) {
            // ignore
            if (log.isDebugEnabled()) {
                log.debug(e.getMessage(), e);
            }
        } finally {
            if (request != null) {
                request.releaseConnection();
                request.abort();
            }
        }
        return isReachable;
    }
    
    private static boolean validateContent(CloseableHttpResponse response, String text) {
        int i = -1;
        try {
            String s = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
            i = s.indexOf(text);
        } catch (ParseException | IOException e) {
            log.error(e.getMessage(), e);
        } finally {
            EntityUtils.consumeQuietly(response.getEntity());
        }
        return i > 0;
    }

    public static Socket createSocket(Proxy proxy) {
        Socket socket = proxy.getSocket();
        if (socket != null && !socket.isClosed()) {
            return socket;
        }
        InetSocketAddress socketAddress = new InetSocketAddress(proxy.getHostName(), proxy.getPort());
        java.net.Proxy p = new java.net.Proxy(java.net.Proxy.Type.SOCKS, socketAddress);
        socket = new Socket(p);
        try {
            socket.setSoTimeout(SOCKET_TIMEOUT);
            socket.setTcpNoDelay(true);
            socket.setReuseAddress(false);
            socket.setKeepAlive(false);
        } catch (SocketException e) {
            log.error(e.getMessage(), e);
        }
        if (proxy.getPtype() == ProxyType.socks4) {
            setUseSocks4(socket);
        }
        proxy.setSocket(socket);
        return socket;
    }

    /**
     * http://bugs.java.com/view_bug.do?bug_id=6964547
     * @param socket
     */
    private static void setUseSocks4(Socket socket) {
        try {
          Field sockImplField = Socket.class.getDeclaredField("impl");
          sockImplField.setAccessible(true);
          SocketImpl socksimpl  = (SocketImpl) sockImplField.get(socket);
          Class<? extends SocketImpl> clazzSocksImpl  =  socksimpl.getClass();
          Method setSockVersion  = clazzSocksImpl.getDeclaredMethod("setV4");
          setSockVersion.setAccessible(true);
          if(null != setSockVersion){
              setSockVersion.invoke(socksimpl);
          }
          sockImplField.set(socket, socksimpl);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
    
    public static HttpHost createHttpHost(Proxy proxy) {
        try {
            return new HttpHost(InetAddress.getByName(proxy.getHostName()),
                    proxy.getHostName(),
                    proxy.getPort(),
                    null);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }
}
