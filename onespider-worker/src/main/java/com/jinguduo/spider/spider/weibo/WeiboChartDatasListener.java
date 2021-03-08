package com.jinguduo.spider.spider.weibo;

import java.time.LocalDate;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Maps;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.listener.SpiderListener;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.Request;
import com.jinguduo.spider.webmagic.Task;
import com.jinguduo.spider.webmagic.selector.PlainText;

public class WeiboChartDatasListener  implements SpiderListener {

    private Map<String,String> httpHeader = Maps.newHashMap();
    {
        initHttper(httpHeader);
    }
    private final static String CHART_REFERER = "http://data.weibo.com/index/hotword?wid=%s&wname=%s";
//    private final static String CHART_URL = "http://data.weibo.com/index/ajax/getchartdata?wid=%s&sdate=%s&edate=%s&__rnd=%s";
    private final static String CHART_URL = "http://data.weibo.com/index/ajax/newindex/getchartdata?wid=%s&dateGroup=1month&sdate=%s&edate=%s&__rnd=%s";
    private final static String HOT_REFERER = "http://data.weibo.com/index";

    @Override
    public void onRequest(Request req, Task task) {

        String url = getUrl(req);
        Site site = (Site)task.getSite();
        String wid = new PlainText(url).regex("getchartdata\\?wid=(\\d*)&",1).get();
        String wname = new PlainText(url).regex("wname=(.*?)&month",1).get();

        if (StringUtils.isNotBlank(wid)){
            getChartProcess(req,site,wid,wname);
        } else {
            getWidProcess(req,site,url);
        }
        httpHeader.entrySet().stream().forEach(s -> {
            site.addHeader(s.getKey(),s.getValue());
        });
     }

    private void getWidProcess(Request req, Site site, String url) {
        req.setUrl(url+"&_t=0&__rnd="+System.currentTimeMillis());
        site.addHeader("Referer", HOT_REFERER);
    }

    private void getChartProcess(Request req, Site site, String wid, String wname) {
        site.getHeaders().clear();
        LocalDate edate = LocalDate.now().minusDays(1);
        LocalDate sdate = LocalDate.now().minusDays(30);
        String referer = String.format(CHART_REFERER,wid,wname);
        //通过页面按钮“对比”发送请求
        //如果还是不稳定，则删除拼接url，用原始url【http://data.weibo.com/index/ajax/getchartdata?wid=1061511050000433140&month=default】，需要改写解析数据部分!!!
        req.setUrl(
                String.format(
                        CHART_URL,
                        wid,
                        sdate,
                        edate,
                        System.currentTimeMillis()
                )
        );
        site.addHeader("Referer", referer);
    }

    private String getUrl(Request req) {
        Job job = ((DelayRequest) req).getJob();
        return StringUtils.isNotBlank(job.getUrl())?job.getUrl():"";
    }

    @Override
    public void onResponse(Request request, Page page, Task task) {
        // no-op
    }

    @Override
    public void onError(Request req, Exception e, Task task) {

    }

    private void initHttper(Map<String, String> httpHeader) {
        httpHeader.put("Pragma","no-cache");
        httpHeader.put("Accept-Encoding","gzip, deflate, sdch");
        httpHeader.put("Accept-Language","zh-CN,zh;q=0.8");
        //httpHeader.put("User-Agent","Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.143 Safari/537.36");
        httpHeader.put("Content-Type","application/x-www-form-urlencoded");
        httpHeader.put("Accept","*/*");
        httpHeader.put("Cache-Control","no-cache");
        httpHeader.put("X-Requested-With","XMLHttpRequest");
        httpHeader.put("Cookie","WEB3=1a27ef96cd9c2e4d5d9a43d9ef97e5cc; WBStorage=02e13baf68409715|undefined; _s_tentry=-; Apache=3187181157863.7056.1496380057232; SINAGLOBAL=3187181157863.7056.1496380057232; ULV=1496380057238:1:1:1:3187181157863.7056.1496380057232:; PHPSESSID=vii2309b1ifmcdcddquqhar766");
        httpHeader.put("Connection","keep-alive");
        httpHeader.put("Referer","http://data.weibo.com/index/hotword");
    }

    @Override
    public void onStart(Task task) {
        // no-op
    }

    @Override
    public void onExit(Task task) {
        // no-op
    }

}
