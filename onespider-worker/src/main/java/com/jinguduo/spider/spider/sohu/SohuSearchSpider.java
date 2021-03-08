package com.jinguduo.spider.spider.sohu;


import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.config.CookieSpecs;
import org.springframework.util.Base64Utils;

import com.google.common.collect.Lists;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.spider.listener.UserAgentSpiderListener;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.code.FetchCodeEnum;
import com.jinguduo.spider.common.constant.CommonEnum;
import com.jinguduo.spider.data.table.AutoFindLogs;
import com.jinguduo.spider.data.table.Category;
import com.jinguduo.spider.data.table.Show;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.selector.Html;
import com.jinguduo.spider.webmagic.selector.Selectable;

@Worker
@Slf4j
public class SohuSearchSpider extends CrawlSpider {

    private static Site site = SiteBuilder.builder()
            .setDomain("so.tv.sohu.com")
            .setCookieSpecs(CookieSpecs.IGNORE_COOKIES)
            .addSpiderListener(new UserAgentSpiderListener())
            .build();

    private PageRule rule = PageRule.build()
            .add("\\.html", page -> find(page));//http://so.tv.sohu.com/list_p11_p2_p3_p4-1_p5_p6_p73_p80_p9_2d2_p10_p11_p12_p13.html
            //.add("/mts\\?wd=", page -> search(page));//http://so.tv.sohu.com/mts?wd=%E4%BD%A0%E6%98%AF%E6%88%91%E5%85%84%E5%BC%9F
    
    private static final String SOHU_DETAIL_URL = "http://tv.sohu.com/item/%s.html";

    /***
     * 自动发现剧<strong>EnterProcess</strong>
     * 筛选条件：保存【今天、昨天】的所有剧，生成 Show 和 中间job ，最终完成其他逻辑
     * http://so.tv.sohu.com/list_p11_p2_p3_p4-1_p5_p6_p73_p80_p9_2d2_p10_p11_p12_p13.html   最新网大
     * http://so.tv.sohu.com/list_p1101_p2_p31000_p42017_p5_p6_p73_p8_p91_p10_p11_p12_p13.html   最新剧
     * http://so.tv.sohu.com/list_p1106_p2_p31000_p4_p5_p6_p77_p8_p91_p10_p11_p12_p13.html   最热综艺
     * @param page
     */
    private void find(Page page) {

        Html html = page.getHtml();
        List<Selectable> nodes = html.$(".st-list").xpath("//li").nodes();
        
        if (CollectionUtils.isEmpty(nodes)){
            log.error("no result from url : ["+page.getUrl()+"]");
            return;
        }
        
        Job job = ((DelayRequest) page.getRequest()).getJob();
        
        List<Show> shows = Lists.newArrayList();
        List<AutoFindLogs> findLogs = Lists.newArrayList();
        
        nodes.parallelStream().forEach(s -> save(s, job, shows ,findLogs));
        
        if(CollectionUtils.isNotEmpty(findLogs)){
            putModel(page,findLogs);
        }
        if(CollectionUtils.isNotEmpty(shows)){
            putModel(page,shows);
        }
    }

    private void save(Selectable s, Job old, List<Show> shows,List<AutoFindLogs> findLogs) {

        Selectable a = s.xpath("//strong/a");

        //基础数据  http://film.sohu.com/album/9214028.html?channeled=1200110001
        String title = a.xpath("a/@title").get().replaceAll("\\s*", "");
        String _s_k = a.xpath("a/@_s_k").get();
        String _s_c = a.xpath("a/@_s_c").get();//100-电影  101-电视剧  106综艺 
        String hrefUrl = fixUrl(a.xpath("a/@href").get());
        
        if(title.contains("DVD版")||title.contains("网络版")||title.endsWith("CUT")){
            return;
        }
        
        if(StringUtils.isBlank(_s_c)){
            return;
        }
        Integer type = Integer.valueOf(_s_c);
        
        if(StringUtils.isNotBlank(_s_k)){
            //生成详情页url
            String url = String.format(SOHU_DETAIL_URL, Base64Utils.encodeToString(_s_k.getBytes()));
            if(type==101){
                url = hrefUrl;
            }
            
            String code = FetchCodeEnum.getCode(url);
            
            if(StringUtils.isBlank(code)){
                return;
            }
            
            Show show = new Show(title,code,CommonEnum.Platform.SO_HU.getCode(),0);
            show.setUrl(url);
            if(type==100){
                show.setCategory(Category.NETWORK_MOVIE.name());
            }else if(type==101){
                show.setCategory(Category.TV_DRAMA.name());
                findLogs.add(new AutoFindLogs(title,Category.TV_DRAMA.name(),CommonEnum.Platform.SO_HU.getCode(),url,code));
            }else if(type==106){
                show.setCategory(Category.TV_VARIETY.name());
            }
            show.setSource(3);//3-代表自动发现的剧
            shows.add(show);
        }
    }


    private String fixUrl(String href) {
        if (StringUtils.isNotBlank(href)&&!href.startsWith("http:")) {
            href = "http:"+href;
        }
        return href;
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
