package com.jinguduo.spider.spider.letv;


import java.util.List;

import lombok.extern.apachecommons.CommonsLog;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Lists;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.code.FetchCodeEnum;
import com.jinguduo.spider.common.constant.CommonEnum;
import com.jinguduo.spider.common.constant.FrequencyConstant;
import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.common.util.RegexUtil;
import com.jinguduo.spider.data.table.AutoFindLogs;
import com.jinguduo.spider.data.table.Category;
import com.jinguduo.spider.data.table.Show;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.selector.Html;
import com.jinguduo.spider.webmagic.selector.Selectable;

@Worker
@CommonsLog
public class LeSearchSpider extends CrawlSpider {

    private static Site site = SiteBuilder.builder()
            .setDomain("list.le.com")
            .setUserAgent("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.143 Safari/537.36")
            .build();

    private PageRule rule = PageRule.build().add("listn",page -> find(page));

    /***
     * 自动发现剧
     * 
     * URL:http://list.le.com/listn/c1_t-1_a-1_y-1_s5_lg-1_ph-1_md_o1_d1_p.html
     * 该连接默认最新，不再指定年份
     * @param page
     */
    private void find(Page page) {

        Html html = page.getHtml();
        List<Selectable> dls = Lists.newArrayList();

        //layout div
        List<Selectable> layouts = html.xpath("//div[@class='pubu_list']/div[@class='list_seo']/div[@class='layout']").nodes();

        if (CollectionUtils.isEmpty(layouts)) {
            log.error("download page no any result!");
            return;
        }
        
        Job job = ((DelayRequest) page.getRequest()).getJob();
        String jobUrl = job.getUrl();
        Integer type = Integer.valueOf(RegexUtil.getDataByRegex(jobUrl, "listn/c(\\d+)_", 1));
        
        List<Show> shows = Lists.newArrayList();
        List<Job> jobs = Lists.newArrayList();
        List<AutoFindLogs> findLogs = Lists.newArrayList();
        
        layouts.stream().forEach(lay -> dls.addAll(lay.xpath("dl").nodes()));

        dls.stream().forEach(s -> save(s,job,shows,findLogs,jobs,type));

        if(CollectionUtils.isNotEmpty(shows)){
            putModel(page,shows);
        }
        if(CollectionUtils.isNotEmpty(findLogs)){
            putModel(page,findLogs);
        }
        if(CollectionUtils.isNotEmpty(jobs)){
            putModel(page,jobs);
        }
    }

    /** 保存 */
    private void save(Selectable s, Job old, List<Show> shows, List<AutoFindLogs> findLogs, List<Job> jobs,Integer type) {

        Selectable a = s.xpath("//dd/p/a");

        //基础数据  http://film.sohu.com/album/9214028.html?channeled=1200110001
        String title = a.xpath("a/@title").get();
        String url = a.xpath("a/@href").replace("http://www.letv.com","http://www.le.com").get();
        String code = FetchCodeEnum.getCode(url);
        
        if(StringUtils.isBlank(code)){
            return;
        }
        
        if(title.contains("DVD版")||title.contains("网络版")||title.endsWith("CUT")){
            return;
        }

        //show
        Show show = new Show(title,code,CommonEnum.Platform.LE_TV.getCode(),0);
        show.setUrl(url);
        show.setSource(3);//3-代表自动发现的剧
        if(type==1){
            show.setCategory(Category.NETWORK_MOVIE.name());
        }else if(type==2){
            show.setCategory(Category.TV_DRAMA.name());
            findLogs.add(new AutoFindLogs(title,Category.TV_DRAMA.name(),CommonEnum.Platform.LE_TV.getCode(),url,code));
        }

        //Job
        Job newJob = DbEntityHelper.deriveNewJob(old, url);
        newJob.setCode(code);
        newJob.setFrequency(FrequencyConstant.GENERAL_SHOW_INFO);

        shows.add(show);
        jobs.add(newJob);
    }



    @Override
    public PageRule getPageRule() {
        return rule;
    }

    /**
     * get the site settings
     *
     * @return site
     * @see Site
     */
    @Override
    public Site getSite() {
        return site;
    }
}
