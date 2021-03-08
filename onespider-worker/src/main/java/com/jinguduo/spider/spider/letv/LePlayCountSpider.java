package com.jinguduo.spider.spider.letv;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.exception.AntiSpiderException;
import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.data.table.BarrageLog;
import com.jinguduo.spider.data.table.CommentLog;
import com.jinguduo.spider.data.table.ShowLog;
import com.jinguduo.spider.webmagic.Page;

@Worker
public class LePlayCountSpider extends CrawlSpider {

    private Site sites = SiteBuilder.builder().setDomain("v.stat.letv.com").build();

    PageRule rule = PageRule.build()
            .add("queryMmsTotalPCount\\?pid=\\d*", page -> total(page))//http://v.stat.letv.com/vplay/queryMmsTotalPCount?pid=%s
            .add("queryMmsTotalPCount\\?vid=\\d*", page -> epi(page));//http://v.stat.letv.com/vplay/queryMmsTotalPCount?vid=%s

    /**
     * 总量抓取
     * @param page
     * @throws AntiSpiderException 
     */
    private void total(Page page) throws AntiSpiderException {
        Job oldJob = ((DelayRequest) page.getRequest()).getJob();
            if(StringUtils.isNotBlank(page.getRawText())&&StringUtils.contains(page.getRawText(), "DOCTYPE")){
                throw new AntiSpiderException("LePlayCountSpider commentLog get raw text not json " + oldJob.getUrl());
            }
            JSONObject jsonObject = JSONObject.parseObject(page.getRawText());
    
            /** 评论数 = 评论数+回复数 */
            Integer commentCount = (null != jsonObject.getInteger("preply") ? jsonObject.getInteger("preply") : 0) + (null != jsonObject.getInteger("pcomm_count") ? jsonObject.getInteger("pcomm_count") : 0);
            Integer barrageCount = (null != jsonObject.getInteger("pdm_count") ? jsonObject.getInteger("pdm_count") : 0);
            Long playCount = (null != jsonObject.getLong("plist_play_count") ? jsonObject.getLong("plist_play_count") : 0);
            
            CommentLog commentLog = new CommentLog(commentCount);
            DbEntityHelper.derive(oldJob, commentLog);
            putModel(page,commentLog);
    
            BarrageLog barrageLog = new BarrageLog(barrageCount);
            DbEntityHelper.derive(oldJob, barrageLog);
            putModel(page,barrageLog);
            
            ShowLog showLog = new ShowLog();
            DbEntityHelper.derive(oldJob, showLog);
            showLog.setPlayCount(playCount);
            putModel(page,showLog);
    }

    public void epi(Page page) throws AntiSpiderException {
        Job oldJob = ((DelayRequest) page.getRequest()).getJob();

            if(StringUtils.isNotBlank(page.getRawText())&&StringUtils.contains(page.getRawText(), "DOCTYPE")){
            	throw new AntiSpiderException("LePlayCountSpider commentLog get raw text not json " + oldJob.getUrl());
            }
            
            JSONObject jsonObject = JSONObject.parseObject(page.getRawText());
            
            /** 评论数 = 评论数+回复数 */
            Integer commentCount = (null != jsonObject.getInteger("vreply") ? jsonObject.getInteger("vreply") : 0) + (null != jsonObject.getInteger("vcomm_count") ? jsonObject.getInteger("vcomm_count") : 0);
            Integer barrageCount = (null != jsonObject.getInteger("vdm_count") ? jsonObject.getInteger("vdm_count") : 0);
            Long playCount = (null != jsonObject.getLong("media_play_count") ? jsonObject.getLong("media_play_count") : 0);
            
            CommentLog commentLog = new CommentLog(commentCount);
            DbEntityHelper.derive(oldJob, commentLog);
            putModel(page,commentLog);
    
            BarrageLog barrageLog = new BarrageLog(barrageCount);
            DbEntityHelper.derive(oldJob, barrageLog);
            putModel(page,barrageLog);
            
            ShowLog showLog = new ShowLog();
            DbEntityHelper.derive(oldJob, showLog);
            showLog.setPlayCount(playCount);
            putModel(page,showLog);
    }

    @Override
    public Site getSite() {
        return sites;
    }
    
    @Override
    public PageRule getPageRule() {
        return rule;
    }
}
