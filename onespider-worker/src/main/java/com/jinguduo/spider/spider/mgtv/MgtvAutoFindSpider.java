package com.jinguduo.spider.spider.mgtv;


import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.Lists;

import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.constant.CommonEnum;
import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.common.util.RegexUtil;
import com.jinguduo.spider.data.table.AutoFindLogs;
import com.jinguduo.spider.data.table.Category;
import com.jinguduo.spider.data.table.Show;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.selector.Html;
import com.jinguduo.spider.webmagic.selector.Selectable;

@Worker
@Slf4j
public class MgtvAutoFindSpider extends CrawlSpider {

    private Site site = SiteBuilder.builder().setDomain("list.mgtv.com").build();
    
    private PageRule rule = PageRule.build().add("", page -> processPage(page));
    
    private static final String DETAIL_URL = "https://www.mgtv.com/h/%s.html";

    private static final String KID_ANIME="https://list.mgtv.com/10/a1---a1------c1-%s---.html?channelId=10";

    public void processPage(Page page) {

        Html html = page.getHtml();
        
        List<Selectable> nodes = html.xpath("//div[@class='m-result-list']/ul/li").nodes();
        
        if(CollectionUtils.isEmpty(nodes)){
            log.warn("MgtvAutoFindSpider result is empty!");
            return;
        }
        
        Job oldJob = ((DelayRequest)page.getRequest()).getJob();
        String jobUrl = oldJob.getUrl();
        Integer type = Integer.valueOf(RegexUtil.getDataByRegex(jobUrl, "channelId=(\\d+)", 1));

        for (Selectable node : nodes) {
            save(page, node, oldJob, type);
        }
    }

    private void save(Page page, Selectable n, Job oldJob, Integer type) {
        String url = n.xpath("a[@class='u-title']//@href").get();
        if (url.startsWith("//")) {
            url = "http:" + url;
        } else if (url.startsWith("www")) {
            url = "http://" + url;
        }
        String title = n.xpath("a[@class='u-title']//text()").get();
        String mark = n.xpath("i[@class='mark-v']//text()").get();
        if(StringUtils.isNotBlank(mark)&&StringUtils.equals(mark.trim(),"预告")){
            return;
        }
        
        if(title.contains("DVD版")||title.contains("网络版")||title.endsWith("CUT")){
            return;
        }
        
        String code = RegexUtil.getDataByRegex(url, "/b/(\\d+)/",1);
        
        if(StringUtils.isBlank(code)){
            return;
        }
        
        Show show = new Show();
        if(type==1){
            //综艺
            show.setCategory(Category.TV_VARIETY.name());
            url = String.format(DETAIL_URL, code);
        }else if(type==2){
            //电视剧
            show.setCategory(Category.TV_DRAMA.name());
            url = String.format(DETAIL_URL, code);
            AutoFindLogs afl = new AutoFindLogs(title,Category.TV_DRAMA.name(),CommonEnum.Platform.MG_TV.getCode(),url,code);
            putModel(page, afl);
        }else if(type==3){
            //网大
            show.setCategory(Category.NETWORK_MOVIE.name());
        }else if (type==10){
            //少儿动漫
            show.setCategory(Category.KID_ANIME.name());
        }
        show.setName(title);
        show.setCode(code);
        show.setPlatformId(CommonEnum.Platform.MG_TV.getCode());
        show.setParentId(0);
        show.setUrl(url);
        show.setSource(3);//3-代表自动发现的剧
        putModel(page, show);
        
//        Job newJob = DbEntityHelper.deriveNewJob(oldJob, url);
//        newJob.setCode(code);
//        putModel(page, newJob);
    }

    @Override
    public Site getSite() {
        return this.site;
    }

    @Override
    public PageRule getPageRule() {
        return this.rule;
    }
}
