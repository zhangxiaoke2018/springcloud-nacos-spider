package com.jinguduo.spider.spider.proxydb;

import java.util.List;

import org.springframework.util.StringUtils;

import com.google.common.collect.Lists;
import com.jinguduo.spider.cluster.downloader.handler.HtmlRenderingPageHandler;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.spider.listener.UserAgentSpiderListener;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.constant.ProxyState;
import com.jinguduo.spider.common.proxy.ProxyType;
import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.common.util.NumberHelper;
import com.jinguduo.spider.data.table.Proxy;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.selector.Selectable;

import lombok.extern.apachecommons.CommonsLog;

@CommonsLog
@Worker
public class ProxyDbSpider extends CrawlSpider {
    
    private Site site = SiteBuilder.builder()
            .setDomain("proxydb.net")
            .setPageHandler(new HtmlRenderingPageHandler())
            // user-agent 动态化
            .addSpiderListener(new UserAgentSpiderListener())
            .setUserAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_6) AppleWebKit/603.3.8 (KHTML, like Gecko) Version/10.1.2 Safari/603.3.8")
            .build();

    private PageRule rule = PageRule.build()
            .add("/", page -> pageDown(page))
            .add("protocol=socks4", page -> getSocks4ProxyList(page))
            .add("protocol=socks5", page -> getSocks5ProxyList(page))
            .add("protocol=https", page -> getHttpProxyList(page))
            .add("protocol=http&", page -> getHttpProxyList(page));
    
    private void getSocks4ProxyList(Page page) {
        List<Proxy> model = getProxyListImpl(page, ProxyType.socks4);
        putModel(page, model);
    }
    
    private void getSocks5ProxyList(Page page) {
        List<Proxy> model = getProxyListImpl(page, ProxyType.socks5);
        putModel(page, model);
    }
    
    private void getHttpProxyList(Page page) {
        List<Proxy> model = getProxyListImpl(page, ProxyType.http);
        putModel(page, model);
    }
    
    private List<Proxy> getProxyListImpl(Page page, ProxyType ptype) {
        List<Proxy> proxies = null;
        Selectable selects = page.getHtml().xpath("//div[@class='table-responsive']/table/tbody/tr/td/a/text()");
        if (selects != null) {
            proxies = Lists.newArrayList();
            for (String s : selects.all()) {
            	if (!s.contains(":")) {
            		log.warn("The host maybe bad! " + s);
					continue;
				}
                Proxy p = new Proxy();
                p.setPtype(ptype);
                p.setHost(s.trim());
                p.setState(ProxyState.Pending);
                proxies.add(p);
            }
        }
        return proxies;
    }
    
    private final static String HOSTNAME = "http://proxydb.net";
    
    private void pageDown(Page page) {
        List<Selectable> pages = page.getHtml().xpath("//nav/ul/li/a").nodes();
        if (pages == null || pages.isEmpty() || pages.size() != 2) {
            // 页面改版，只有 <Previous page> 和 <Next page> 两个按钮 
            return;
        }
        Selectable nextPage = pages.get(1);
        String url = nextPage.xpath("//a/@href").get();
        if (!StringUtils.hasText(url)) {
            return;
        }
        final Job currJob = ((DelayRequest) page.getRequest()).getJob();
        String nextPageUrl = HOSTNAME + url;
        Job nJob = DbEntityHelper.derive(currJob, new Job(nextPageUrl));
        putModel(page, nJob);
    }

    @Override
    public Site getSite() {
        return site;
    }

    @Override
    public PageRule getPageRule() {
        return rule;
    }

}
