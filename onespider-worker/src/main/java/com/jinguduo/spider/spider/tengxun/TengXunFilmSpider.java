package com.jinguduo.spider.spider.tengxun;


import java.util.List;

import lombok.extern.apachecommons.CommonsLog;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Lists;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.constant.FrequencyConstant;
import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.webmagic.Page;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * 废弃：film 请求是跳转页面
 */
@Deprecated
@Worker
@CommonsLog
public class TengXunFilmSpider extends CrawlSpider {

    /** film : get commentId for get commment count by api */
    final static String COMMENT_CID_URL = "http://ncgi.video.qq.com/fcgi-bin/video_comment_id?otype=json&op=%s&cid=%s";
    
    final static String COMMENT_VID_URL = "http://ncgi.video.qq.com/fcgi-bin/video_comment_id?otype=json&op=%s&vid=%s";

    private static final String MOVIE_PC_URL = "http://data.video.qq.com/fcgi-bin/data?tid=70&&appid=10001007&appkey=e075742beb866145&callback=jQuery19109213305850191142_1468217242170&low_login=1&idlist=%s&otype=json&_=1468217242171";

    private final String playCountOne = "https://data.video.qq.com/fcgi-bin/data?tid=376&&appid=20001212&appkey=b4789ed0ec69d23a&otype=json&&callback=jQuery19106671459599489511_1484404891342&idlist=%s";

    private PageRule rules = PageRule.build().add("cover",page -> processNetMovie(page));

    private Site site = SiteBuilder.builder().setDomain("film.qq.com").build();

    private void processNetMovie(Page page){

        Job job = ((DelayRequest) page.getRequest()).getJob();

        String url = page.getUrl().get();//任务链接
        String code = job.getCode();//任务Code
        List<Job> jobList = Lists.newArrayList();//任务保存list
        final int op = 3;

        //都生成播放量，稳定后再删除无效的api
        try {
            //play job
         /*   Job newJob = new Job(String.format(MOVIE_PC_URL,job.getCode()));
            DbEntityHelper.derive(job,newJob);
            newJob.setCode(code);
*/
            //play job @2017.01.14
            //https://union.video.qq.com/fcgi-bin/data?tid=376
            //https://union.video.qq.com/fcgi-bin/data?tid=613
            //更新日志：
            // 1.api更新两个播放量，暂用613
            // 2.换成https
            String vid = page.getHtml().regex("vid: \"(.*?)\"",1).get();
            if (StringUtils.isBlank(vid)){
                vid = page.getHtml().regex("vid:\"(.*?)\"",1).get();
            }
            Job job1 = new Job(String.format(playCountOne,vid));
            DbEntityHelper.derive(job,job1);
           // newJob.setCode(code);
            //add job
            jobList.add(job1);
          //  jobList.add(newJob);
        } catch (Exception e) {
            log.error("transformation url fail ,check url is valid : ["+url+"]");
            log.error(e.getMessage(), e);
        }
            //TODO 备用判断(若该判断还是有错，可以再加一条判断，综艺的id长度一般为15，剧的id一般为11. 没办法的办法再用)
            Job commentIdJob = null;
            if(code.length()==15){
                commentIdJob = new Job(String.format(COMMENT_CID_URL,op,code));
            }else if(code.length()==11){
                //例如动漫之类页面的code其实是vid，平成上列的vid api url会异常
                commentIdJob = new Job(String.format(COMMENT_VID_URL,op,code));
            }
            if(commentIdJob!=null){
                DbEntityHelper.derive(job,commentIdJob);
                commentIdJob.setCode(code);
                commentIdJob.setFrequency(FrequencyConstant.COMMENT_BEFOR_PROCESS);

                jobList.add(commentIdJob);
            }

        if(jobList.isEmpty()){
            return;
        }
        putModel(page,jobList);
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
