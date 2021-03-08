package com.jinguduo.spider.cluster.downloader.handler;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.springframework.util.CollectionUtils;

import com.gargoylesoftware.htmlunit.CookieManager;
import com.gargoylesoftware.htmlunit.ProxyConfig;
import com.gargoylesoftware.htmlunit.StringWebResponse;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.WebResponseData;
import com.gargoylesoftware.htmlunit.html.HTMLParser;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.util.Cookie;
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import com.google.common.collect.Sets;
import com.jinguduo.spider.cluster.downloader.HttpClientRequestContext;
import com.jinguduo.spider.common.proxy.ProxyType;
import com.jinguduo.spider.data.table.Proxy;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.Request;
import com.jinguduo.spider.webmagic.Task;

import lombok.extern.apachecommons.CommonsLog;

@CommonsLog
public class HtmlRenderingPageHandler extends WebMagicPageHandler {
    
    private Set<String> blockedHostnames = Sets.newHashSet(
            "www.google-analytics.com",
            "hm.baidu.com",
            "pagead2.googlesyndication.com",
            "stats.g.doubleclick.net");
    
    @Override
    public Page doHandle(Request request, HttpClientRequestContext requestContext, String charset, HttpResponse httpResponse, Task task) throws IOException {
        Page page = super.doHandle(request, requestContext, charset, httpResponse, task);
        
        String html = page.getRawText();
        if (html != null) {
            try (WebClient client = new FilteredWebClient(blockedHostnames);) {
                // proxy
                Proxy proxy = getProxy(requestContext);
                if (proxy != null) {
                    boolean isSocks = proxy.getPtype() == ProxyType.socks4 || proxy.getPtype() == ProxyType.socks5;
                    ProxyConfig proxyConfig = new ProxyConfig(proxy.getHostName(), proxy.getPort(), isSocks);
                    client.getOptions().setProxyConfig(proxyConfig);
                }
                // Cookie
                CookieStore cookieStore = requestContext.getHttpClientContext().getCookieStore();
                if (cookieStore != null && !CollectionUtils.isEmpty(cookieStore.getCookies())) {
                    List<Cookie> cookies = Cookie.fromHttpClient(cookieStore.getCookies());
                    CookieManager cookieManager = new CookieManager();
                    for (Cookie c: cookies) {
                        cookieManager.addCookie(c);
                    }
                    client.setCookieManager(cookieManager);
                }
                // Rendering
                final Charset cs = charset == null ? StandardCharsets.UTF_8 : Charset.forName(charset);
                StringWebResponse webResp = new StringWebResponse(html, cs, new URL(request.getUrl()));
                HtmlPage htmlPage = HTMLParser.parseHtml(webResp, client.getCurrentWindow());
                page.setRawText(htmlPage.asXml());
            } catch (Exception e) {
                log.error("The HtmlRendering is failed.", e);
            }
        }
        
        return page;
    }
    
    public HtmlRenderingPageHandler addBlockedHostname(String hostname) {
        blockedHostnames.add(hostname);
        return this;
    }
    
    static class FilteredWebClient extends WebClient {
        private static final long serialVersionUID = -3579823361063513995L;
        
        Set<String> blockedHostnames = null;
        
        public FilteredWebClient() {
            super();
        }
        
        public FilteredWebClient(Set<String> blockedHostnames) {
            this();
            this.blockedHostnames = blockedHostnames;
        }
        
        @Override
        public WebResponse loadWebResponse(final WebRequest webRequest) throws IOException {
            URL url = webRequest.getUrl();
            if (blockedHostnames == null 
                    || blockedHostnames.isEmpty()
                    || !blockedHostnames.contains(url.getHost())) {
                return super.loadWebResponse(webRequest);
            }
            if (log.isDebugEnabled()) {
                log.debug("Skip Request URL:" + url.toString());
            }
            return createBlankWebResponse(webRequest);
        }
        
        protected WebResponse createBlankWebResponse(final WebRequest wr) throws IOException {
            final List<NameValuePair> headers = new ArrayList<>();
            headers.add(new NameValuePair("content-type", "text; charset=" + UTF_8));
            final byte[] body = "\r\n".getBytes(UTF_8);
            final WebResponseData wrd = new WebResponseData(body, 200, "OK", headers);
            return new WebResponse(wrd, wr.getUrl(), wr.getHttpMethod(), 0);
        }
    }
}
