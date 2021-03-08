package com.jinguduo.spider.spider.piaofang;

import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.data.table.BoxOfficeLogs;
import com.jinguduo.spider.webmagic.Page;

import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
@Deprecated
//@Worker
@CommonsLog
public class ChinaBoxOfficeSpider extends CrawlSpider {
    //头号玩家
    private String url="http://www.cbooo.cn/m/657862";

    private Site site= SiteBuilder.builder().setDomain("www.cbooo.cn").build();

    private PageRule rules=PageRule.build().add("/m/",page -> getBoxOffice(page));

    private  void getBoxOffice(Page page){
        Job job = ((DelayRequest) page.getRequest()).getJob();
        if (job == null) {
            log.error("job is null");
            return;
        }
        Document document = page.getHtml().getDocument();
        Elements ziliaokuElements = document.getElementsByClass("ziliaoku");
        if (null == ziliaokuElements || ziliaokuElements.isEmpty()){
            log.error(" no zilioaku elements");
            return;
        }
        Element ziliaoku = ziliaokuElements.get(0);
        Elements spanElements = ziliaoku.getElementsByClass("m-span");
        if (null == spanElements || spanElements.isEmpty()){
            log.error(" no m-span elements");
            return;
        }
        long todayBoxOffice=0;
        long allBoxOffice=0;
        for (Element inElemet:spanElements) {
            String todayString=null;
            String allString=null;
            String text = inElemet.text();//.data();
            if (StringUtils.contains(text,"今日")){
                todayString=text.replace("今日实时票房<br />","");
               if(todayString.indexOf("万") != -1){
                    Double d=Double.parseDouble(PatternString(todayString))*10000;
                    todayBoxOffice=d.longValue();

               }
            }else if(StringUtils.contains(text,"累计")){
                allString=text.replace("累计票房<br />","");
                if(allString.indexOf("万") != -1){
                    Double d=Double.parseDouble(PatternString(allString))*10000;
                    allBoxOffice=d.longValue();
                }

            }
        }

        BoxOfficeLogs boxOffice=new BoxOfficeLogs();
        boxOffice.setAllBoxOffice(allBoxOffice);
        boxOffice.setTodayBoxOffice(todayBoxOffice);
        boxOffice.setCode(job.getCode());
        putModel(page,boxOffice);
    }

    public static String PatternString(String boxOffice){
        Pattern p= Pattern.compile("(\\d+\\.\\d+)");
        Matcher m=p.matcher(boxOffice);
        if(m.find()){
            boxOffice = m.group(1) == null ? "" : m.group(1);
        }else{
            p=Pattern.compile("(\\d+)");
            m=p.matcher(boxOffice);
            if(m.find()){
                boxOffice=m.group(1) == null ?"":m.group(1);
            }else{
                boxOffice="";
            }
        }
        return boxOffice;
    }


    @Override
    public PageRule getPageRule() {
        return this.rules;
    }

    @Override
    public Site getSite() {
        return this.site;
    }

}
