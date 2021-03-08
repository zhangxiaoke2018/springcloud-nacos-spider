package com.jinguduo.spider.spider.premproxy;

import java.util.List;

import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.spider.listener.UserAgentSpiderListener;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.constant.ProxyState;
import com.jinguduo.spider.common.proxy.ProxyType;
import com.jinguduo.spider.data.table.Proxy;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.selector.Selectable;

//@Worker
public class PremProxySpider extends CrawlSpider {

    private Site site = SiteBuilder.builder()
            .setDomain("premproxy.com")
            // user-agent 动态化
            .addSpiderListener(new UserAgentSpiderListener())
            .setUserAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_6) AppleWebKit/603.3.8 (KHTML, like Gecko) Version/10.1.2 Safari/603.3.8")
            .build();

    private PageRule rule = PageRule.build()
            .add("/", page -> getProxyList(page));
    
    
    private void getProxyList(Page page) {
        List<Selectable> rows = page.getHtml().xpath("//div[@id='proxylist']//tbody/tr").nodes();
        for (Selectable row: rows) {
            List<Selectable> defines = row.xpath("//td/text()").nodes();
            String host = defines.get(0).get().trim();
            String type = defines.get(1).get().trim().toLowerCase();
            Proxy p = new Proxy();
            p.setHost(host);
            p.setPtype(ProxyType.valueOf(type));
            p.setState(ProxyState.Pending);
            putModel(page, p);
        }
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
