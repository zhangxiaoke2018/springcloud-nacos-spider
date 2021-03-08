package com.jinguduo.spider.spider.letv;


import lombok.extern.slf4j.Slf4j;

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
import com.jinguduo.spider.common.util.RegexUtil;
import com.jinguduo.spider.data.table.VipEpisode;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.selector.Html;
import com.jinguduo.spider.webmagic.selector.Selectable;

@Worker
@Slf4j
public class LePageSpider extends CrawlSpider {

    //综艺分集
    private final static String ZONG_YI_LIST_URL = "http://d.api.m.le.com/detail/getPeriod?pid=%s&platform=pc";
    //剧分集
    private final static String SHOW_LIST_URL = "http://d.api.m.le.com/card/dynamic?id=%s&cid=%s&vid=%s&platform=pc&type=episode";//vid是分集的id
    //总播放量，评论量
    private final static String TOTAL_COUNT_URL = "http://v.stat.letv.com/vplay/queryMmsTotalPCount?pid=%s&cid=%s&_=1468218514489";
    //分集(网大)播放量，评论量
//    private final static String EPI_COUNT_URL = "http://v.stat.letv.com/vplay/queryMmsTotalPCount?vid=%s";
    //评论文本
    private final static String COMMENT_CONTENT_URL = "http://api.my.le.com/vcm/api/list?rows=20&page=1&listType=1&xid=%s&pid=%s";
    //弹幕文本
    private final static String BARRAGE_CONTENT_URL = "http://cdn.api.my.letv.com/danmu/list?vid=%s&cid=%s&start=0&getcount=1";
    
    private Site sites = SiteBuilder.builder().setDomain("www.le.com").build();

    private PageRule rules = PageRule.build()
            .add("(zongyi|tv|comic)/\\d*\\.", page -> netVariety(page))
            .add("/ptv/vplay/", page -> netMovie(page));

    public void netVariety(Page page) {
        Job oldJob = ((DelayRequest) page.getRequest()).getJob();
        
        if(oldJob.getUrl().contains("error")){
            return;
        }
        String type = RegexUtil.getDataByRegex(oldJob.getUrl(), "com/(\\S+)/\\d+\\.html$", 1);
        if(StringUtils.isBlank(type)){
            return;
        }
        
        Selectable script = page.getHtml().xpath("//head/script").nodes().stream().filter(f->f.get().contains("var __INFO__")).findFirst().orElse(null);
        
        if(script==null){
            return;
        }
        
        String cid = script.regex("cid:\"(\\d?)\"}").get();
        if(StringUtils.isBlank(cid)){
            cid = script.regex("cid:(\\d+?)").get();
        }
        String pid = script.regex("pid:\"(.*?)\"").get();
        if(StringUtils.isBlank(pid)){
            pid = script.regex("pid:\\'(.*?)\\'").get();
        }
        if (StringUtils.isBlank(cid) || StringUtils.isBlank(pid)) {
            log.error("LePageSpider netVariety Get cid or pid from url:["+page.getRequest().getUrl()+"] is null!");
            return;
        }
        
        //电视剧,动漫
        if(StringUtils.equalsIgnoreCase(type, "comic")||StringUtils.equalsIgnoreCase(type, "tv")){
            //从页面拿某集的url获得vid
            String firstUrl = page.getHtml().$("#first_videolist .j_all_tuwen .d_img:eq(0) a:eq(0)","href").get();
            if(StringUtils.isNotBlank(firstUrl)){
                String vid = firstUrl.substring(firstUrl.lastIndexOf("/")+1, firstUrl.lastIndexOf("."));
                Job newJob = DbEntityHelper.deriveNewJob(oldJob, String.format(SHOW_LIST_URL,pid,cid,vid));
                newJob.setCode(pid);
                putModel(page,newJob);
            }else{
                log.error("LePageSpider netVariety get url empty from aTag:"+firstUrl+" url:"+oldJob.getUrl());
            }
        }else{
          //综艺
            Job newJob = DbEntityHelper.deriveNewJob(oldJob, String.format(ZONG_YI_LIST_URL,pid));
            newJob.setCode(pid);
            putModel(page,newJob);
        }
        Job job = this.totalCountJob(page,cid,pid);
        putModel(page,job);
    }

    /**
     * 网大详情页：播放量，评论量
     * @param page
     */
    private void netMovie(Page page){

        Job oldJob = ((DelayRequest) page.getRequest()).getJob();
        
        Html html = page.getHtml();
        String pid = html.regex("pid: (\\d*)",1).get();
        String cid = html.regex("cid: (\\d*)",1).get();
        String vid = html.regex("vid: (\\d*)",1).get();
        
        String payIco = html.$(".juji_cntBox").$(".pay_ico").get();
        String tryLook = html.regex("trylook: (\\d*)",1).get();//试看时间
        if(StringUtils.isBlank(payIco)&&StringUtils.isNotBlank(tryLook)&&Integer.valueOf(tryLook)>0){
            // vip标志
            VipEpisode vip = new VipEpisode();
            vip.setCode(oldJob.getCode());
            vip.setPlatformId(oldJob.getPlatformId());
            putModel(page, vip);
        }
        if (StringUtils.isBlank(vid)){
            vid = oldJob.getCode();
        }
            Job newJob = DbEntityHelper.deriveNewJob(oldJob,String.format(TOTAL_COUNT_URL,pid,cid));
            putModel(page,newJob);
            
            Job danmuJob = DbEntityHelper.deriveNewJob(oldJob, String.format(BARRAGE_CONTENT_URL, vid, cid));
            danmuJob.setFrequency(FrequencyConstant.BARRAGE_TEXT);
            putModel(page,danmuJob);
        //评论文本
        createCommentContentJob(page,oldJob, vid, pid);
    }

    private void createCommentContentJob(Page page,Job oldJob, String vid, String pid) {
        if(StringUtils.isNotBlank(pid) &&StringUtils.isNotBlank(vid)) {
                Job commentComtentJob = DbEntityHelper.deriveNewJob(oldJob, String.format(COMMENT_CONTENT_URL,vid,pid));
                commentComtentJob.setFrequency(FrequencyConstant.COMMENT_TEXT);
                putModel(page,commentComtentJob);
        }
    }

    //总量任务
    private Job totalCountJob(Page page , String cid,String pid ){
        Job oldJob = ((DelayRequest) page.getRequest()).getJob();
            Job newJob = DbEntityHelper.deriveNewJob(oldJob,String.format(TOTAL_COUNT_URL,pid,cid));
            newJob.setCode(oldJob.getCode());
            return newJob;
    }

    @Override
    public PageRule getPageRule() {
        return this.rules;
    }

    @Override
    public Site getSite() {
        return sites;
    }

}
