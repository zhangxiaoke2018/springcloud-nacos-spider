package com.jinguduo.spider.cluster.downloader;

import java.util.Map;

import org.apache.http.auth.AuthState;
import org.apache.http.auth.ChallengeState;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.springframework.util.StringUtils;

import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.common.proxy.ProxyHelper;
import com.jinguduo.spider.common.proxy.ProxyType;
import com.jinguduo.spider.data.table.Proxy;
import com.jinguduo.spider.webmagic.Request;
import com.jinguduo.spider.webmagic.utils.HttpConstant;
import com.jinguduo.spider.webmagic.utils.UrlUtils;

/**
 * @author code4crafter@gmail.com
 *         Date: 17/3/18
 *         Time: 11:28
 *
 * @since 0.7.0
 */
public class HttpUriRequestConverter {

    public HttpClientRequestContext convert(Request request, Site site, Proxy proxy) {
        HttpClientRequestContext httpClientRequestContext = new HttpClientRequestContext();
        httpClientRequestContext.setHttpUriRequest(convertHttpUriRequest(request, site, proxy));
        httpClientRequestContext.setHttpClientContext(convertHttpClientContext(request, site, proxy));
        httpClientRequestContext.setProxy(proxy);
        return httpClientRequestContext;
    }

    private HttpClientContext convertHttpClientContext(Request request, Site site, Proxy proxy) {
        HttpClientContext httpContext = new HttpClientContext();
        if (proxy != null) {
            if (proxy.getPtype() == ProxyType.http
                    && proxy.getUsername() != null) {
                AuthState authState = new AuthState();
                authState.update(new BasicScheme(ChallengeState.PROXY), new UsernamePasswordCredentials(proxy.getUsername(), proxy.getPassword()));
                httpContext.setAttribute(HttpClientContext.PROXY_AUTH_STATE, authState);
                
            } else if (proxy.getPtype() == ProxyType.socks5
                    || proxy.getPtype() == ProxyType.socks4) {
                httpContext.setAttribute(ProxyHelper.PROXY_SOCKS, proxy);
            }
        }
        if (request.getCookies() != null && !request.getCookies().isEmpty()) {
            CookieStore cookieStore = new BasicCookieStore();
            for (Map.Entry<String, String> cookieEntry : request.getCookies().entrySet()) {
                BasicClientCookie cookie1 = new BasicClientCookie(cookieEntry.getKey(), cookieEntry.getValue());
                cookie1.setDomain(UrlUtils.removePort(UrlUtils.getDomain(request.getUrl())));
                cookieStore.addCookie(cookie1);
            }
            httpContext.setCookieStore(cookieStore);
        }
        return httpContext;
    }

    private HttpUriRequest convertHttpUriRequest(Request request, Site site, Proxy proxy) {
        RequestBuilder requestBuilder = selectRequestMethod(request).setUri(UrlUtils.fixIllegalCharacterInUrl(request.getUrl()));
        if (site.getHeaders() != null) {
            for (Map.Entry<String, String> headerEntry : site.getHeaders().entrySet()) {
                requestBuilder.addHeader(headerEntry.getKey(), headerEntry.getValue());
            }
        }

        String cookieSpecs = site.getCookieSpecs();
        if (!StringUtils.hasText(cookieSpecs)) {
            cookieSpecs = CookieSpecs.STANDARD;
        }
        
        RequestConfig.Builder requestConfigBuilder = RequestConfig.custom()
                // 从连接池中获取连接的超时时间，超过该时间未拿到可用连接，
                //   会抛出org.apache.http.conn.ConnectionPoolTimeoutException:
                //     Timeout waiting for connection from pool
                .setConnectionRequestTimeout(5000)
                // 连接上服务器(握手成功)的时间，超出该时间抛出connect timeout
                .setConnectTimeout(site.getTimeOut() / 2)
                // 服务器返回数据(response)的时间，超过该时间抛出read timeout
                .setSocketTimeout(site.getTimeOut())
                .setCookieSpec(cookieSpecs);

        if (proxy != null 
                && (proxy.getPtype() == ProxyType.http || proxy.getPtype() == ProxyType.https)) {
            requestConfigBuilder.setProxy(ProxyHelper.createHttpHost(proxy));
        }
        requestBuilder.setConfig(requestConfigBuilder.build());
        HttpUriRequest httpUriRequest = requestBuilder.build();
        if (request.getHeaders() != null && !request.getHeaders().isEmpty()) {
            for (Map.Entry<String, String> header : request.getHeaders().entrySet()) {
                httpUriRequest.addHeader(header.getKey(), header.getValue());
            }
        }
        return httpUriRequest;
    }

    private RequestBuilder selectRequestMethod(Request request) {
        String method = request.getMethod();
        if (method == null || method.equalsIgnoreCase(HttpConstant.Method.GET)) {
            //default get
            return RequestBuilder.get();
        } else if (method.equalsIgnoreCase(HttpConstant.Method.POST)) {
            return addFormParams(RequestBuilder.post(),request);
        } else if (method.equalsIgnoreCase(HttpConstant.Method.HEAD)) {
            return RequestBuilder.head();
        } else if (method.equalsIgnoreCase(HttpConstant.Method.PUT)) {
            return addFormParams(RequestBuilder.put(), request);
        } else if (method.equalsIgnoreCase(HttpConstant.Method.DELETE)) {
            return RequestBuilder.delete();
        } else if (method.equalsIgnoreCase(HttpConstant.Method.TRACE)) {
            return RequestBuilder.trace();
        }
        throw new IllegalArgumentException("Illegal HTTP Method " + method);
    }

    private RequestBuilder addFormParams(RequestBuilder requestBuilder, Request request) {
        if (request.getRequestBody() != null) {
            ByteArrayEntity entity = new ByteArrayEntity(request.getRequestBody().getBody());
            entity.setContentType(request.getRequestBody().getContentType());
            requestBuilder.setEntity(entity);
        }
        return requestBuilder;
    }

}
