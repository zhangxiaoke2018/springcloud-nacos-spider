package com.jinguduo.spider.spider.kuaidaili;

import com.google.common.collect.Sets;
import com.jinguduo.spider.cluster.downloader.handler.HtmlRenderingPageHandler;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.*;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.constant.ProxyState;
import com.jinguduo.spider.common.exception.PageBeChangedException;
import com.jinguduo.spider.common.proxy.ProxyType;
import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.data.table.Proxy;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.selector.Html;
import com.jinguduo.spider.webmagic.selector.Selectable;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

/**
 * I'm a cute code dog
 * User: LingChen
 * Date:2017/8/30
 * Time:16:20
 */
@Slf4j
@Worker
public class KuaidailiSpider extends CrawlSpider {

    private static final String KUAIDAILI_BASE_URL = "http://www.kuaidaili.com/free/inha/%s/";

    private Site site = SiteBuilder.builder()
            .setDomain("www.kuaidaili.com")
            .setPageHandler(new HtmlRenderingPageHandler())
            .addDownloaderListener(new KuaidailiDownloaderListener())
            .setAcceptStatCode(Sets.newHashSet(521, 200))
            .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.96 Safari/537.36")
            .build();

    private PageRule rules = PageRule.build()
            .add("/free/", page -> analyze(page));

    /**
     * 解析基础页
     * @throws UnsupportedEncodingException 
     * @throws ScriptException 
     * @throws NoSuchMethodException 
     * @throws PageBeChangedException 
     */
    private void analyze(Page page) throws UnsupportedEncodingException, NoSuchMethodException, ScriptException, PageBeChangedException {
        Job job = ((DelayRequest) page.getRequest()).getJob();
        String url = job.getUrl();
        Map<String, List<String>> headers = page.getHeaders();
        List<String> setCookieList = headers.get("Set-Cookie");
        Html html = page.getHtml();
        if (null != setCookieList && setCookieList.size() == 1) {
			String cookie = this.getCookie(html, setCookieList);
            if (StringUtils.isBlank(cookie)) {
                return;
            } else {
                String newUrl = null;
                newUrl = url + "#" + URLEncoder.encode(cookie, "UTF-8");
                Job job2 = new Job(newUrl);
                DbEntityHelper.derive(job, job2);
                putModel(page, job2);
                return;
            }
        }
        /**
         * 如果是第一页，则循环生成翻页任务
         */
        if (StringUtils.contains(url, "/free/inha/1/")) {
            for (int i = 2; i <= 10; i++) {
                String newUrl = String.format(KUAIDAILI_BASE_URL, i);
                Job job2 = new Job(newUrl);
                DbEntityHelper.derive(job, job2);
                putModel(page, job2);
            }
        }

        List<Selectable> rows = html.xpath("//div[@id='list']/table/tbody/tr").nodes();
        if (null == rows || rows.isEmpty()) {
            throw new PageBeChangedException(page.getRawText());
        }
        for (Selectable row : rows) {
        	String ip = row.xpath("//td[@data-title='IP']/text()").get().trim();
        	String port = row.xpath("//td[@data-title='PORT']/text()").get().trim();
        	if (StringUtils.isBlank(port)) {
        		log.warn("The port maybe bad! " + ip);
				continue;
			}
        	String type = row.xpath("//td[@data-title='类型']/text()").get();
        	type = StringUtils.isBlank(type) ? "http" : type;
            Proxy proxy = new Proxy();
            proxy.setHost(ip + ":" + port);
            proxy.setPtype(ProxyType.valueOf(type.trim().toLowerCase()));
            proxy.setState(ProxyState.Pending);
            putModel(page, proxy);
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


    private String getCookie(Html html, List<String> setCookieList) throws NoSuchMethodException, ScriptException {
        String htmlStr = html.toString();
        String jsStr = htmlStr.substring(htmlStr.indexOf("function"), htmlStr.indexOf("</script>"));
        String js = jsStr.replace("eval(\"qo=eval;qo(po);\")", "return po");
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("js");
        engine.eval(js);
        Invocable invocable = (Invocable) engine;
        String jiamiFunName = js.substring(js.indexOf("function") + 9, js.indexOf("("));
        String jiamiParamer = htmlStr.substring(htmlStr.indexOf(jiamiFunName + "(") + jiamiFunName.length() + 1, htmlStr.indexOf(")\","));
        String result = (String) invocable.invokeFunction(js.substring(js.indexOf("function") + 9, js.indexOf("(")), jiamiParamer);
        String clear = result.substring(result.indexOf("_ydclearance=") + 13, result.indexOf(";"));
        String initCookie = setCookieList.stream().filter(p -> p.contains("yd_cookie")).findFirst().orElse(null);
        String yd_cookie = initCookie.substring(initCookie.indexOf("yd_cookie="), initCookie.indexOf(";"));
        String cookie = yd_cookie + ";" + "_ydclearance=" + clear + ";";
        return cookie;
    }
}
