package com.jinguduo.spider.cluster.downloader.handler;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jinguduo.spider.cluster.downloader.HttpClientRequestContext;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.Request;
import com.jinguduo.spider.webmagic.Task;
import com.jinguduo.spider.webmagic.selector.PlainText;
import com.jinguduo.spider.webmagic.utils.CharsetUtils;
import com.jinguduo.spider.webmagic.utils.HttpClientUtils;

public interface PageHandler extends ResponseHandler<Page> {
	
	Logger log = LoggerFactory.getLogger(PageHandler.class);
    
    abstract byte[] getContent(String charset, HttpResponse httpResponse) throws IOException;

    @Override
    default Page doHandle(Request request, HttpClientRequestContext requestContext, String charset, HttpResponse httpResponse, Task task) throws IOException {
        byte[] bytes = getContent(charset, httpResponse);
        String contentType = httpResponse.getEntity().getContentType() == null ? "" : httpResponse.getEntity().getContentType().getValue();
        Page page = new Page();
        page.setBytes(bytes);
        if (!request.isBinaryContent()){
            if (charset == null) {
                charset = getHtmlCharset(contentType, bytes);
            }
            page.setCharset(charset);
            if (bytes != null) {
                page.setRawText(new String(bytes, charset));
            } else {
                page.setRawText(null);
            }
        }
        page.setUrl(new PlainText(request.getUrl()));
        page.setRequest(request);
        page.setStatusCode(httpResponse.getStatusLine().getStatusCode());
        page.setDownloadSuccess(true);
        page.setHeaders(HttpClientUtils.convertHeaders(httpResponse.getAllHeaders()));
        return page;
    }
    
    default String getFinalHttpLocation(Request request, HttpClientRequestContext requestContext) {
    	try {
    		HttpClientContext ctx = requestContext.getHttpClientContext();
    		List<URI> redirectLocations = ctx.getRedirectLocations();
    		URI location = URIUtils.resolve(new URI(request.getUrl()), ctx.getTargetHost(), redirectLocations);
    		return location != null ? location.toString() : null;
    	} catch (URISyntaxException e) {
    		log.error(e.getMessage(), e);
    	}
    	return null;
    }
    
    default String getHtmlCharset(String contentType, byte[] contentBytes) throws IOException {
        String charset = CharsetUtils.detectCharset(contentType, contentBytes);
        if (charset == null) {
            charset = Charset.defaultCharset().name();
            //log.warn("Charset autodetect failed, use {} as charset. Please specify charset in Site.setCharset()", Charset.defaultCharset());
        }
        return charset;
    }
}
