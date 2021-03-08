package com.jinguduo.spider.spider.douban;

import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.spider.listener.UserAgentSpiderListener;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.type.SequenceCounterQuota;
import com.jinguduo.spider.data.table.bookProject.DoubanBook;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.utils.UrlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * Created by lc on 2020/1/10
 */
@Worker
@Slf4j
public class DoubanBookSearchSpider extends CrawlSpider {


    private Site site = SiteBuilder.builder()
            .setDomain("search.douban.com")
            .addDownloaderListener(new DoubanRandomCookieDownloaderListener()
                    .addAbnormalStatusCode(403, 404, 500, 501, 503)
                    .setQuota(new SequenceCounterQuota(5))
                    .setProbability(0.2))
            // user-agent 动态化
            .addSpiderListener(new UserAgentSpiderListener()).build();

    private PageRule rules = PageRule.build()
            .add("subject_search", page -> searchResultProcess(page));

    private void searchResultProcess(Page page) {
        Job oldJob = ((DelayRequest) page.getRequest()).getJob();
        //获取到已解密的
        List<String> urlList = null;
        try {
            String plaintext = DoubanBookDecodeBodyUtil.getPlainText(page.getRawText());
            urlList = DoubanBookDecodeBodyUtil.getDetailUrlList(plaintext);
        } catch (Exception e) {
            return;
        }

        //此步骤生成任务，并保存至store
        String isbn = UrlUtils.getParam(oldJob.getUrl(), "search_text");

        for (String doubanBookUrl : urlList) {
            DoubanBook db = new DoubanBook();


            db.setIsbn(isbn);
            db.setPlatformId(oldJob.getPlatformId());
            db.setUrl(doubanBookUrl);
            db.setCode(oldJob.getCode());
            putModel(page, db);

//            Job detailJob = new Job(doubanBookUrl);
//            detailJob.setCode(code);
//            putModel(page, detailJob);
        }

    }


    @Override
    public PageRule getPageRule() {
        return rules;
    }

    @Override
    public Site getSite() {
        return this.site;
    }
}
