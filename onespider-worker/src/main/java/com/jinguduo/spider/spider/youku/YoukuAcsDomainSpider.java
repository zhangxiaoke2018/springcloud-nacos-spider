package com.jinguduo.spider.spider.youku;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.constant.FrequencyConstant;
import com.jinguduo.spider.common.exception.AntiSpiderException;
import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.data.table.ShowPopularLogs;
import com.jinguduo.spider.data.text.BarrageText;
import com.jinguduo.spider.webmagic.Page;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.util.CollectionUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;

/**
 *  2019.1.18 优酷热度替换播放量
 *  author: xk
 * */
@Worker
@CommonsLog
public class YoukuAcsDomainSpider extends CrawlSpider {

    private final int INCREASE_SCOPE = 1;//请求增长区间

    private final static int MAX_SCOPE = 1 * 60;

    private final static String NEW_DANMU_DATA = "{\"pid\":0,\"ctype\":10004,\"sver\":\"3.1.0\",\"cver\":\"v1.0\",\"ctime\":%s,\"guid\":\"EtW3F0kASSgCAXLz3WlAIEkl\",\"vid\":\"%s\",\"mat\":%s,\"mcount\":1,\"type\":1}";
    private final static String NEW_DANMU_URL="https://acs.youku.com/h5/mopen.youku.danmu.list/1.0/?jsv=2.5.1&appKey=24679788&api=mopen.youku.danmu.list&v=1.0&type=originaljson&dataType=jsonp&timeout=20000&jsonpIncPrefix=utility";



    private Site site = SiteBuilder.builder()
            .setDomain("acs.youku.com")
            .addSpiderListener(new YoukuAcsDomainDownLoaderListener())
            .addHeader("Content-Type","application/x-www-form-urlencoded")
            .setUserAgent("Mozilla/5.0 (iPhone; CPU iPhone OS 10_0 like Mac OS X) AppleWebKit/602.1.38 (KHTML, like Gecko) Version/10.0 Mobile/14A300 Safari/602.1")
            .build();

    private PageRule rules = PageRule.build()
            .add("/h5/mtop.youku.haixing.play.h5.detail/", page -> pageProcess(page))//页面处理
            .add("/h5/mopen.youku.danmu.list/",page -> barrageProcess(page));


    public void pageProcess(Page page){
        Job oldJob = ((DelayRequest) page.getRequest()).getJob();
        JSONObject data = JSONObject.parseObject(page.getRawText()).getJSONObject("data");
        JSONObject moduleResult = data.getJSONObject("moduleResult");
        if(moduleResult == null){
            throw new AntiSpiderException("youkuHotCount------Document : moduleResult is null 。 code :"+oldJob.getCode());
        }
        try{
            JSONArray modules = moduleResult.getJSONArray("modules");
            JSONObject ob = (JSONObject) modules.get(1);
            JSONArray  components= (JSONArray) ob.get("components");
            JSONObject C = (JSONObject) components.get(0);
            JSONObject itemResult = (JSONObject) C.get("itemResult");
            JSONObject item = (JSONObject) itemResult.get("item");
            JSONObject item1 = (JSONObject) item.get("1");
            Long totalVc = item1.getLong("totalVv");
            if ("电影".equals(data.getJSONObject("extra").getString("showCategory"))) {
                String videoId = data.getJSONObject("extra").getString("videoId");
                ShowPopularLogs movie = new ShowPopularLogs();
                movie.setCode(videoId);
                movie.setPlatformId(3);
                movie.setHotCount(totalVc);
                putModel(page, movie);
            }else {
                String showId = "z" + data.getJSONObject("extra").getString("showId");
                ShowPopularLogs s = new ShowPopularLogs();
                s.setCode(showId);
                s.setPlatformId(3);
                s.setHotCount(totalVc);
                putModel(page, s);
            }
        }catch (Exception ex) {
            log.error("youku_hotcount exception:", ex);
        }


    }
    public void barrageProcess(Page page) throws UnsupportedEncodingException {
        Job oldJob = ((DelayRequest) page.getRequest()).getJob();
        JSONObject data = JSONObject.parseObject(page.getRawText()).getJSONObject("data");
        String result = data.getString("result");
        JSONObject barrageJson = JSONObject.parseObject(result);
        Integer currentMat = 0;
        try {
            String url = URLDecoder.decode(oldJob.getUrl(),"UTF-8");
            String datas = url.substring(url.indexOf("&data=")+6,url.length());
            JSONObject dataJson = JSONObject.parseObject(datas);
            currentMat = dataJson.getInteger("mat");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        List<JSONObject> list = (List)barrageJson.getJSONObject("data").getJSONArray("result");
        if (!CollectionUtils.isEmpty(list)) {
            List<BarrageText> danmakuLogsList = Lists.newArrayListWithCapacity(list.size());
            list.stream().forEach(j -> analysis(page, danmakuLogsList, oldJob, j));
            putModel(page, danmakuLogsList);
        }
        //log.info("youkuBarrage begin ! code -> "+oldJob.getCode() + " Page :"+currentMat);
        next(page, oldJob, list, currentMat);
    }

    private void next(Page page, Job oldJob, List<JSONObject> list ,Integer current) throws UnsupportedEncodingException {

        if (current >= MAX_SCOPE && (list == null || list.isEmpty())) {
            return;
        }
        List<Job> jobs = Lists.newArrayList();
        final int next_scope = INCREASE_SCOPE + current;

        //弹幕新
        String danmuDatas = URLEncoder.encode(String.format(NEW_DANMU_DATA, System.currentTimeMillis(),oldJob.getCode(),next_scope), "UTF-8");
        Job newDanmuJob = new Job(NEW_DANMU_URL + "&data=" + danmuDatas);
        newDanmuJob.setCode(oldJob.getCode());
        newDanmuJob.setFrequency(FrequencyConstant.BARRAGE_TEXT);
        jobs.add(newDanmuJob);
        putModel(page,jobs);
    }



    private void analysis(Page page, List<BarrageText> danmakuLogsList, Job oldJob, JSONObject j) {
        long precision = 1000;
        BarrageText barrageText = new BarrageText(
                j.getString("id"),
                j.getString("uid"),
                "",
                j.getLong("playat") / precision,
                j.getTimestamp("createtime"),
                0,
                j.getString("content")
        );
        oldJob.setPlatformId(3);
        DbEntityHelper.derive(oldJob,barrageText);
        danmakuLogsList.add(barrageText);
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
