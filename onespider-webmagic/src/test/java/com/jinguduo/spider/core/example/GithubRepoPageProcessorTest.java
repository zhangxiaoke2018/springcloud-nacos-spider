package com.jinguduo.spider.core.example;

import org.junit.Test;

import com.jinguduo.spider.core.downloader.MockGithubDownloader;
import com.jinguduo.spider.webmagic.ResultItems;
import com.jinguduo.spider.webmagic.Spider;
import com.jinguduo.spider.webmagic.Task;
import com.jinguduo.spider.webmagic.pipeline.Pipeline;
import com.jinguduo.spider.webmagic.processor.example.GithubRepoPageProcessor;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author code4crafter@gmail.com
 *         Date: 16/1/19
 *         Time: 上午7:27
 */
public class GithubRepoPageProcessorTest {

    @Test
    public void test_github() throws Exception {
        Spider.create(new GithubRepoPageProcessor()).addPipeline(new Pipeline() {
            @Override
            public void process(ResultItems resultItems, Task task) {
                assertThat(((String) resultItems.get("name")).trim()).isEqualTo("webmagic");
                assertThat(((String) resultItems.get("author")).trim()).isEqualTo("code4craft");
            }
        }).setDownloader(new MockGithubDownloader()).test("https://github.com/code4craft/webmagic");
    }
}
