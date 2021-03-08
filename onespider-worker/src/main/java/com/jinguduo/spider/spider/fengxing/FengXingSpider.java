package com.jinguduo.spider.spider.fengxing;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.constant.FrequencyConstant;
import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.common.util.NumberHelper;
import com.jinguduo.spider.data.table.ShowLog;
import com.jinguduo.spider.data.table.VipEpisode;
import com.jinguduo.spider.webmagic.Page;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 16/7/14 下午4:09
 */
@Worker
public class FengXingSpider extends CrawlSpider {
    
    //final static String LIST_URL = "http://q1.fun.tv/ajax/vod_panel/%s"; 风行没有分集播放量、评论或弹幕，所以生成分集信息也无用

    final static String COMMENT_URL = "http://api1.fun.tv/comment/display/gallery/%s?pg=1&isajax=1";

    private Site site = SiteBuilder.builder().setDomain("www.fun.tv").build();

    private PageRule rules = PageRule.build()
            .add("vplay",page -> processMain(page));

    //播放页 抓取总播放量,评论文本
    private void processMain(Page page){
        Job oldJob = ((DelayRequest) page.getRequest()).getJob();

        String code = page.getHtml().xpath("//*[@id=\"play-list-layout\"]/div[@class='sub-layout']/h3/a/@href").regex("/subject/(\\d*)/",1).get();
        if ( StringUtils.isBlank(code) ) {
            String jobCode = oldJob.getCode();
            Matcher matcher = Pattern.compile("\\w-(\\d*)").matcher(jobCode);
            if(matcher.find()){
                code = matcher.group(1);
            }
        }
        String vipId = page.getHtml().$("#vip-pay-guide").get();
        if(StringUtils.isNotBlank(vipId)){
            // vip标志
            VipEpisode vip = new VipEpisode();
            vip.setCode(oldJob.getCode());
            vip.setPlatformId(oldJob.getPlatformId());
            putModel(page, vip);
        }
        
        //总播放量
        String playcount = page.getHtml().xpath("//*[@id=\"playerWrap\"]/div[2]/div[2]/a").replace(",", "").replace(" ","").regex("播放：(\\d*)", 1).get();
        ShowLog showLog = new ShowLog();
        DbEntityHelper.derive(oldJob,showLog);
        showLog.setPlayCount(NumberHelper.parseLong(playcount,-1));
        putModel(page,showLog);

        //风行的评论只有总评论没有分集评论 这里关联深度1的code  若以后真的有需求加上风行详细分集数据再补充
        Job commentJob = DbEntityHelper.deriveNewJob(oldJob,String.format(COMMENT_URL,code));
        commentJob.setFrequency(FrequencyConstant.COMMENT_COUNT);
        putModel(page,commentJob);
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
