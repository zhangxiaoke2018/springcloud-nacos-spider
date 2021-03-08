package com.jinguduo.spider.spider.bodongComic;

import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.webmagic.Page;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by lc on 2018/9/10
 */
@Worker
public class BodongIndexSpider extends CrawlSpider {

    private static final String BASE_URL = "https://comicapp.vip.qq.com/cgi-bin/comicapp_async_cgi?fromWeb=1&param=%s";
    private static final String PAGE_PARAM = "{\"0\":{\"module\":\"comic_category_mt_svr\",\"method\":\"GetComicCategoryListV2\",\"param\":{\"categoryId\":0,\"sort\":1,\"freeId\":0,\"page\":%s,\"type\":0,\"idList\":[],\"start\":0,\"end\":1000}}}";



    private Site site = SiteBuilder.builder()
            .setDomain("boodo.qq.com")
            .addHeader("Origin", "https://bodong.vip.qq.com")
            .addSpiderListener(new BodongComicDownLoaderListener())
            .build();


    private PageRule rules = PageRule.build()
            .add(".", page -> createPageJob(page));

    private void createPageJob(Page page) throws UnsupportedEncodingException {
            Job job = ((DelayRequest) page.getRequest()).getJob();
            for (int i = 1; i < 10; i++) {
                String pageParam = String.format(PAGE_PARAM, String.valueOf(i));
                pageParam = URLEncoder.encode(pageParam, "utf-8");
                String pageUrl = String.format(BASE_URL, pageParam);
                Job pageJob = new Job(pageUrl);
                DbEntityHelper.derive(job, pageJob);
                putModel(page, pageJob);
            }
    }

    @Override
    public PageRule getPageRule() {
        return rules;
    }

    @Override
    public Site getSite() {
        return site;
    }
}
