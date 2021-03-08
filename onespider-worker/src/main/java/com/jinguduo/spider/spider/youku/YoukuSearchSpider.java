package com.jinguduo.spider.spider.youku;

import java.util.List;

import lombok.extern.apachecommons.CommonsLog;

import org.apache.commons.lang3.StringUtils;

import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.code.FetchCodeEnum;
import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.common.util.NumberHelper;
import com.jinguduo.spider.data.table.ShowLog;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.selector.Html;
import com.jinguduo.spider.webmagic.selector.Selectable;

/**
 * 
 * 若详情页无播放量，从搜索页抓取(例如：超次元偶像)
 * 
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @author liuxinglong
 * @DATE 2017年6月26日 上午11:40:14
 *
 */
@Worker
@CommonsLog
public class YoukuSearchSpider extends CrawlSpider {

    private Site site = SiteBuilder.builder().setDomain("www.soku.com").build();

    private PageRule rules = PageRule.build()
            .add("", page -> pageProcess(page));//页面处理

    private void pageProcess(Page page) {
        
        Job oldJob = ((DelayRequest) page.getRequest()).getJob();
        
        Html html = page.getHtml();
        List<Selectable> nodes = html.xpath("//div[@class='sk-express']//div[@class='s_dir']").nodes();
        for (Selectable selectable : nodes) {
            String url = selectable.xpath("//div[@class='s_info']//div[@class='s_intr']//div[@class='info_cont']//a/@href").get();
            if(StringUtils.isBlank(url)||!StringUtils.equals(oldJob.getCode(), FetchCodeEnum.getCode(url))){
                continue;
            }
            String playCountStr = selectable.xpath("//div[@class='s_play']/span[@class='num']/a/text()").get();
            if (StringUtils.isNotBlank(playCountStr)) {
                Long vv = (long) (NumberHelper.bruteParse(playCountStr, 0f)*10000L);
                ShowLog showLog = new ShowLog();
                DbEntityHelper.derive(oldJob, showLog);
                showLog.setCode(oldJob.getCode());
                showLog.setPlayCount(vv);
                putModel(page,showLog);
            }
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
