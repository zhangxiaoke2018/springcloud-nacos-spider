package com.jinguduo.spider.spider.fiction;

import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.constant.CommonEnum;
import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.common.util.Md5Util;
import com.jinguduo.spider.webmagic.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.util.Date;

/**
 * Created by lc on 2019/7/1
 */
@Worker
@Slf4j
@SuppressWarnings("all")
public class SQWebSpider extends CrawlSpider {

    //原创月票榜  1 ：男生 or 女生（uriencode） / 2:时间 （2019-7）
    private static final String MONTHLY_RANK_URL = "http://read.xiaoshuo1-sm.com/novel/i.php?interest=%s&p=1&do=is_rank_list&size=200&page=1&type=15&rank=3&month=%s";
    //新书榜 1 ：男生 or 女生（uriencode）
    private static final String NEW_BOOK_RANK_URL = "http://read.xiaoshuo1-sm.com/novel/i.php?interest=%s&p=1&do=is_rank_list&size=200&page=1&status=0&type=4";

    private static final String[] PARAM_OF_SEXS = {
            "男生", "女生"
    };

    private Site site = new SiteBuilder()
            .setDomain("t.shuqi.com")
            .build();


    private PageRule rule = PageRule.build()
            .add("t.shuqi.com$", this::mainCreateRankJob);

    private void mainCreateRankJob(Page page) {
        //创建一堆任务
        Date date = new Date();
        String dateStr = DateFormatUtils.format(date, "yyyy-MM");//2019-7 or 2019-07
        for (String sex : PARAM_OF_SEXS) {
            //月票榜
            createJob(page,String.format(MONTHLY_RANK_URL, sex, dateStr));
            //新书榜
            createJob(page,String.format(NEW_BOOK_RANK_URL, sex));
        }


    }

    private void createJob(Page page, String url) {
        Job oldJob = ((DelayRequest) page.getRequest()).getJob();
        Job job = DbEntityHelper.derive(oldJob, new Job(url));
        job.setCode(Md5Util.getMd5(url));
        job.setPlatformId(CommonEnum.Platform.SQ.getCode());
        putModel(page, job);
    }

    @Override
    public Site getSite() {
        // TODO Auto-generated method stub
        return site;
    }

    @Override
    public PageRule getPageRule() {
        // TODO Auto-generated method stub
        return rule;
    }

}
