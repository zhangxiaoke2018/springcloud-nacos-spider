package com.jinguduo.spider.spider.iqiyi;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.InflaterInputStream;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;

import com.jinguduo.spider.cluster.downloader.handler.PageHandler;

@Slf4j
public class IqiyiBarrageSpiderPageHandler implements PageHandler {

    @Override
    public byte[] getContent(String charset, HttpResponse response) throws IOException {
        if (response.getStatusLine().getStatusCode() == 200
                && isZipCompressed(response)) {
            try (InputStream is = response.getEntity().getContent();) {
                return decompress(is);
            }
        } else {
            return IOUtils.toByteArray(response.getEntity().getContent());
        }
    }

    final static String OCTET_STREAM = "application/octet-stream";
    private boolean isZipCompressed(HttpResponse response) {
        Header contentType = response.getEntity().getContentType();
        if (contentType == null) {
            return false;
        }
        return OCTET_STREAM.equalsIgnoreCase(contentType.getValue());
    }

    public byte[] decompress(InputStream is) {
        final int BUF_SIZE = 1024;
        try (
                InflaterInputStream zip = new InflaterInputStream(is);
                ByteArrayOutputStream out = new ByteArrayOutputStream(BUF_SIZE);
                ) {
            byte[] buf = new byte[BUF_SIZE];
            int len;
            while ((len = zip.read(buf, 0, BUF_SIZE)) != -1) {
                out.write(buf, 0, len);
            }
            return out.toByteArray();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

}
