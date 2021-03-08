package com.jinguduo.spider.common.msg;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PushbackInputStream;

import javax.servlet.FilterChain;
import javax.servlet.ReadListener;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.filter.OncePerRequestFilter;

import com.ning.compress.gzip.OptimizedGZIPInputStream;

import lombok.extern.apachecommons.CommonsLog;

/**
 * 
 *
 */
@CommonsLog
public class GzipDecompressFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String contentEncoding = request.getHeader("Content-Encoding");
        if (contentEncoding != null && contentEncoding.indexOf("gzip") > -1) {
            
            PushbackInputStream pushbackInputStream = new PushbackInputStream(request.getInputStream(), 2);
            byte [] head = new byte[2];
            int len = pushbackInputStream.read(head);
            pushbackInputStream.unread(head, 0, len); //push back
            
            InputStream streamWrapper;
            if ((head[0] == (byte) 0x1f && head[1] == (byte) 0x8b)) {
                streamWrapper = new OptimizedGZIPInputStream(pushbackInputStream);
            } else {
                streamWrapper = pushbackInputStream;
            }
            
            request = new HttpServletRequestWrapper(request) {
                
                @Override
                public ServletInputStream getInputStream() throws IOException {
                    return new DecompressServletInputStream(streamWrapper);
                }
                
                @Override
                public BufferedReader getReader() throws IOException {
                    return new BufferedReader(new InputStreamReader(streamWrapper));
                }
            };
        }
        filterChain.doFilter(request, response);
    }

    public static class DecompressServletInputStream extends ServletInputStream {
        private InputStream inputStream;

        public DecompressServletInputStream(InputStream input) {
            inputStream = input;

        }

        @Override
        public int read() throws IOException {
            return inputStream.read();
        }

        @Override
        public boolean isFinished() {
            try {
                return inputStream.available() <= 0;
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
            return true;
        }

        @Override
        public boolean isReady() {
            try {
                return inputStream.available() > 0;
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
            return false;
        }

        @Override
        public void setReadListener(ReadListener listener) {
            // no-op
        }

    }
}
