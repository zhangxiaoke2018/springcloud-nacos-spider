package com.jinguduo.spider.spider.letv;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

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
import com.jinguduo.spider.common.util.HttpHelper;
import com.jinguduo.spider.data.table.AutoFindLogs;
import com.jinguduo.spider.data.table.Category;
import com.jinguduo.spider.data.table.Show;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.selector.Html;
import com.jinguduo.spider.webmagic.selector.Selectable;

@Worker
@Slf4j
public class LeBannerSpider extends CrawlSpider{
    
    private Site site = SiteBuilder.builder().setDomain("tv.le.com").build();
    
    private PageRule rule = PageRule.build().
            add("", page -> processBannerDrama(page)); 
    
    private void processBannerDrama(Page page){
        Html html = page.getHtml();

        //layout div
        List<Selectable> nodes = html.xpath("//div[@class='focus_nav']/ul/li").nodes();

        if (CollectionUtils.isEmpty(nodes)) {
            log.error("download page no any result!");
            return;
        }
        
        Job job = ((DelayRequest) page.getRequest()).getJob();
        
        List<Show> shows = Lists.newArrayList();
        List<Job> jobs = Lists.newArrayList();
        List<AutoFindLogs> findLogs = Lists.newArrayList();
        
        nodes.stream().forEach(s -> save(s,job,shows,findLogs,jobs));

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
    
    private void save(Selectable s, Job old, List<Show> shows, List<AutoFindLogs> findLogs, List<Job> jobs) {

        Selectable a = s.xpath("//a");

        String href = a.xpath("a/@href").replace("http://www.letv.com","http://www.le.com").get();
        if(StringUtils.isNotBlank(href)){
            String res = HttpHelper.get(href, "UTF-8");
            Document doc = Jsoup.parse(res);

            String script = doc.getElementsByTag("script").get(1).html().replace(" ","").replace("　","");

            String pid = "";
            Pattern pidPattern = Pattern.compile("pid:(\\d+),");
            Matcher pidMatcher = pidPattern.matcher(script);
            if(pidMatcher.find()) {
                pid = pidMatcher.group(1);
            }
            String url = String.format("http://www.le.com/tv/%s.html", pid);

            String title = "";
            Pattern titlePattern = Pattern.compile("pTitle:\"(.*?)\",");
            Matcher titleMatcher = titlePattern.matcher(script);
            if(titleMatcher.find()) {
                title = titleMatcher.group(1);
            }

            String code = FetchCodeEnum.getCode(url);
            
            if(StringUtils.isBlank(code)){
                return;
            }
            
            //show
            Show show = new Show(title,code,CommonEnum.Platform.LE_TV.getCode(),0);
            show.setUrl(url);
            show.setCategory(Category.TV_DRAMA.name());
            show.setSource(3);//3-代表自动发现的剧
            findLogs.add(new AutoFindLogs(title,Category.TV_DRAMA.name(),CommonEnum.Platform.LE_TV.getCode(),url,code));
            
            //Job
            Job newJob = DbEntityHelper.deriveNewJob(old, url);
            newJob.setCode(code);
            newJob.setFrequency(FrequencyConstant.GENERAL_SHOW_INFO);
            
            shows.add(show);
            jobs.add(newJob);
        }
        
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
