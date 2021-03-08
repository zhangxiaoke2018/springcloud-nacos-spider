package com.jinguduo.spider.core.downloader;

import org.junit.Test;

import com.jinguduo.spider.webmagic.Request;
import com.jinguduo.spider.webmagic.Site;
import com.jinguduo.spider.webmagic.downloader.HttpClientRequestContext;
import com.jinguduo.spider.webmagic.downloader.HttpUriRequestConverter;
import com.jinguduo.spider.webmagic.utils.UrlUtils;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author code4crafter@gmail.com
 *         Date: 2017/7/22
 *         Time: 下午5:29
 */
public class HttpUriRequestConverterTest {

    @Test
    public void test_illegal_uri_correct() throws Exception {
        HttpUriRequestConverter httpUriRequestConverter = new HttpUriRequestConverter();
        HttpClientRequestContext requestContext = httpUriRequestConverter.convert(new Request(UrlUtils.fixIllegalCharacterInUrl("http://bj.zhongkao.com/beikao/yimo/##")), Site.me(), null);
        assertThat(requestContext.getHttpUriRequest().getURI()).isEqualTo(new URI("http://bj.zhongkao.com/beikao/yimo/#"));
    }
}