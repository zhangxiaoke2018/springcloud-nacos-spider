package com.jinguduo.spider.spider.iqiyi;

import com.alibaba.fastjson.JSONArray;
import com.jinguduo.spider.common.constant.FrequencyConstant;
import com.jinguduo.spider.common.exception.AntiSpiderException;
import com.jinguduo.spider.data.table.ShowLog;
import lombok.extern.apachecommons.CommonsLog;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.spider.listener.UserAgentSpiderListener;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.data.table.CommentLog;
import com.jinguduo.spider.webmagic.Page;

/**
 * 抓取iqiyi 泡泡评论量任务
 * 
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @author liuxinglong
 * @DATE 2017年4月14日 下午2:41:46
 *
 */
@Worker
@CommonsLog
public class IqiyiPaoPaoCommentSpider extends CrawlSpider {

    private Site site = SiteBuilder.builder()
            .setDomain("paopao.iqiyi.com")
            .addSpiderListener(new UserAgentSpiderListener())
            .build();
    
    private PageRule rules = PageRule.build()
            .add("basic_wall.action",page -> getPaoPaoCommentCount(page))
            .add("home.action", page -> getTotalPlayCount(page))
            ;


    //公共变量
    private static String NORMAL_RESCODE = "A00000";
    private static String NOT_ONLINE_RESCODE = "P00100";

    // 不精确的
    private final static String PLAY_COUNT_URL_MAYBE = "http://iface2.iqiyi.com/views/3.0/player_tabs?app_k=204841020bd16e319191769268fb56ee&app_v=6.8.3&platform_id=11&dev_os=4.4.4&dev_ua=Nexus+5&net_sts=1&qyid=358239054455227&secure_p=GPad&secure_v=1&core=1&dev_hw=%7B%22mem%22%3A%22457.3MB%22%2C%22cpu%22%3A0%2C%22gpu%22%3A%22%22%7D&scrn_sts=0&scrn_res=1080,1794&scrn_dpi=480&page_part=2&album_id=";


    /***
     * @title 抓取爱奇艺泡泡评论数量
     * @param page
     */
    private void getPaoPaoCommentCount(Page page) {
        Job job = ((DelayRequest) page.getRequest()).getJob();

        String rawText = page.getRawText();
        if(StringUtils.isBlank(rawText)){
            log.error("response body is null");
            return;
        }
        JSONObject jsonObject = JSONObject.parseObject(rawText);

        if( NORMAL_RESCODE.equals(jsonObject.getString("code"))){
            if ( null != jsonObject.getJSONObject("data") ){
                Integer commentCount = jsonObject.getJSONObject("data").getInteger("feedCount");
                CommentLog commentLog = new CommentLog(commentCount);
                DbEntityHelper.derive(job, commentLog);
                commentLog.setJobId(666);//特殊值，表示iqiyi泡泡评论量
                putModel(page,commentLog);
            }
        }else if(NOT_ONLINE_RESCODE.equals(jsonObject.getString("code"))){
            //圈子未上线
            log.debug("code:"+job.getCode()+":圈子未上线!url:"+page.getRequest().getUrl());
            return ;
        }else{
            log.warn("response error code :"+jsonObject.getString("code"));
        }
    }

    /**
     *
     * @param page
     */
    private void getTotalPlayCount(Page page) {
        Job job = ((DelayRequest) page.getRequest()).getJob();

        String rawText = page.getRawText();

        JSONObject jsonObject = JSONObject.parseObject(rawText);
        String code = jsonObject.getString("code");
        if( NORMAL_RESCODE.equals(code)){
            JSONObject data = jsonObject.getJSONObject("data");
            JSONArray headVideos = data.getJSONArray("headVideo");
            if(headVideos == null || headVideos.size() == 0){
                // 有泡泡圈，但是不显示播放量，进行不精确抓取
                //log.info("有泡泡圈，但是不显示播放量"+job.getCode());
            } else {
                JSONObject hv = headVideos.getJSONObject(0);
                Long playCount = hv.getLong("playCount");
                ShowLog showLog = new ShowLog();
                showLog.setPlayCount(playCount);
                DbEntityHelper.derive(job, showLog);
                putModel(page, showLog);
            }

        } else if("P010009".equals(code) || NOT_ONLINE_RESCODE.equals(code)){
            // 圈子未上线
            log.debug("圈子未上线"+job.getCode());
        }
        else {
            throw new AntiSpiderException("paopao play count fetch error" + page.getRequest().getUrl());
        }


    }



    @Override
    public PageRule getPageRule() {
        return rules;
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
