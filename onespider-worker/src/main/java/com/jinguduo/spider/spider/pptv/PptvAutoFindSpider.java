package com.jinguduo.spider.spider.pptv;


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
import com.jinguduo.spider.common.code.FetchCodeEnum;
import com.jinguduo.spider.common.constant.CommonEnum;
import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.common.util.RegexUtil;
import com.jinguduo.spider.data.table.AutoFindLogs;
import com.jinguduo.spider.data.table.Category;
import com.jinguduo.spider.data.table.Show;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.selector.Html;
import com.jinguduo.spider.webmagic.selector.Selectable;

/**
 * 
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @author liuxinglong
 * @DATE 2017年8月31日 上午11:20:29
 *
 */
@Worker
@Slf4j
public class PptvAutoFindSpider extends CrawlSpider {

    private Site site = SiteBuilder.builder().setDomain("list.pptv.com").build();
    
    private PageRule rule = PageRule.build().add("", page -> processPage(page));
    
    public void processPage(Page page) {

        Html html = page.getHtml();
        
        List<Selectable> nodes = html.$(".ui-resp-pics").xpath("//ul/li").nodes();
        
        if(CollectionUtils.isEmpty(nodes)){
            log.error("PptvAutoFindSpider result is empty!");
            return;
        }
        
        Job oldJob = ((DelayRequest)page.getRequest()).getJob();
        String jobUrl = oldJob.getUrl();
        Integer type = Integer.valueOf(RegexUtil.getDataByRegex(jobUrl, "type=(\\d+)&", 1));
        
        List<Show> shows = Lists.newArrayList();
        List<Job> jobs = Lists.newArrayList();
        List<AutoFindLogs> findLogs = Lists.newArrayList();
         
        nodes.stream().forEach(n->save(n,oldJob,jobs,findLogs,shows,type));

        if(CollectionUtils.isNotEmpty(jobs)){
            putModel(page, jobs);
        }
        if(CollectionUtils.isNotEmpty(findLogs)){
            putModel(page,findLogs);
        }
        if(CollectionUtils.isNotEmpty(shows)){
            putModel(page, shows);
        }
    }

    private void save(Selectable n, Job oldJob, List<Job> jobs, List<AutoFindLogs> findLogs, List<Show> shows,Integer type) {
        String url = n.xpath("a//@href").get();
        String title = n.xpath("a//@title").get();
        
        if(title.contains("DVD版")||title.contains("网络版")||title.endsWith("CUT")||title.contains("我是剧大大")){
            return;
        }
        
        String code = FetchCodeEnum.getCode(url);
        
        if(StringUtils.isBlank(code)){
            return;
        }
        
        Show show = new Show(title,code,CommonEnum.Platform.PPTV.getCode(),0);
        if(type==1){
            //电影
            show.setCategory(Category.NETWORK_MOVIE.name());
        }else if(type==2){
            //电视剧
            show.setCategory(Category.TV_DRAMA.name());
            findLogs.add(new AutoFindLogs(title,Category.TV_DRAMA.name(),CommonEnum.Platform.PPTV.getCode(),url,code));
        }
        show.setUrl(url);
        show.setSource(3);//3-代表自动发现的剧
        shows.add(show);
        
        Job newJob = DbEntityHelper.deriveNewJob(oldJob, url);
        newJob.setCode(code);
        
        jobs.add(newJob);
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
