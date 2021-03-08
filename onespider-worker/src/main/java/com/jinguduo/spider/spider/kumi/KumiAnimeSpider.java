package com.jinguduo.spider.spider.kumi;



import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.constant.FrequencyConstant;
import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.webmagic.Page;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 09/11/2016 6:07 PM
 */
@Worker
public class KumiAnimeSpider extends CrawlSpider {

    private static final Logger log = LoggerFactory.getLogger(KumiAnimeSpider.class);

    private Site site = SiteBuilder.builder().setDomain("www.kumi.cn").build();//http://www.kumi.cn/donghua/85063.html

    private final String PLAY_COUNT_URL = "http://list.kumi.cn/num.php?contentid=%s";

    private PageRule rules = PageRule.build()
            .add("/donghua/",page -> processAnime(page));

    private void processAnime(Page page){

        log.debug("process kumi anime " + page.getUrl().get());

        Job oldJob = ((DelayRequest) page.getRequest()).getJob();
        if (oldJob == null) {
            return;
        }

        Document document = page.getHtml().getDocument();

        String contentid = document.getElementById("SOHUCS").attr("sid");

            Job newJob = new Job(String.format(PLAY_COUNT_URL,contentid));

            DbEntityHelper.derive(oldJob, newJob);
            newJob.setFrequency(FrequencyConstant.GENERAL_PLAY_COUNT);

            putModel(page,newJob);
    }



    @Override
    public PageRule getPageRule() {
        return this.rules;
    }

    @Override
    public Site getSite() {
        return this.site;
    }
}
