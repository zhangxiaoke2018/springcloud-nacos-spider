package com.jinguduo.spider.common.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.List;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import com.ning.compress.gzip.OptimizedGZIPOutputStream;

import lombok.extern.apachecommons.CommonsLog;

/**
 * RestTemplate with both gzip compression and decompression
 * 
 * 
 */
@CommonsLog
public class GzipRestTemplate extends RestTemplate {

    public GzipRestTemplate() {
        super();
        init();
    }

    public GzipRestTemplate(ClientHttpRequestFactory clientHttpRequestFactory) {
        this();
        super.setRequestFactory(clientHttpRequestFactory);
    }
    
    public GzipRestTemplate(List<HttpMessageConverter<?>> messageConverters) {
        super(messageConverters);
        init();
    }

    @Override
    protected ClientHttpRequest createRequest(URI url, HttpMethod method) throws IOException {
        ClientHttpRequest request = super.createRequest(url, method);
        
        // for request gzip
        request.getHeaders().add("Accept-Encoding", "gzip,deflate");
        request.getHeaders().add("Accept", "charset=UTF-8");
        
        if (logger.isDebugEnabled()) {
            logger.debug("Created " + method.name() + " request for \"" + url + "\"");
        }
        return request;
    }
    
    public void init() {
        List<ClientHttpRequestInterceptor> interceptors = this.getInterceptors();
        interceptors.add(new GzipRequestIntercepter());
    }
    
    private final static int DATA_LENGTH = 10 * 1024;
    
    static class GzipRequestIntercepter implements ClientHttpRequestInterceptor {

        @Override
        public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
                throws IOException {
            if (body.length > DATA_LENGTH) {
                body = compress(body);
                // for request body compression
                request.getHeaders().add("Content-Encoding", "gzip");
                //request.getHeaders().setContentType(MediaType.APPLICATION_OCTET_STREAM);
            }
            
            return execution.execute(request, body);
        }

        private byte[] compress(byte[] body) {
            try {
                ByteArrayOutputStream out = new ByteArrayOutputStream(DATA_LENGTH);
                OutputStream gzip = new OptimizedGZIPOutputStream(out);
                gzip.write(body);
                gzip.close();
                return out.toByteArray();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
            return body;
        }
    }
}
