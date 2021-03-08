package com.jinguduo.spider.common.util;

import org.junit.Test;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
public class GzipRestTemplateTests {

    @Test
    public void testGzipRequest() {
        GzipRestTemplate restTemplate = new GzipRestTemplate();
        Byte[] bigObject = new Byte[8000];
        restTemplate.postForObject("http://www.baidu.com/gzip", bigObject, String.class);
    }
}
