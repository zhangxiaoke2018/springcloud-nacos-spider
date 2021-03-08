package com.jinguduo.spider.spider.manmanmanhua;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.util.DateHelper;
import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.data.table.ComicMmmh;
import com.jinguduo.spider.webmagic.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;

/**
 * Created by lc on 2018/12/7
 */
@Worker
@Slf4j
public class ComicMmmhOpenSpider extends CrawlSpider {

    private static final String COMIC_DETAIL_URL = "https://m.manmanapp.com/comic-%s.html";

    private static final String INIT_OPEN_URL = "https://open.manmanapp.com/guduo/guduomedia?sign=%s&time=%s";

    //对接的key，固定参数   3D2Sl%9W3gKCVa^a
    private static final String MMMH_KEY = "3D2Sl%9W3gKCVa^a";

    private Site site = SiteBuilder.builder().setDomain("open.manmanapp.com").build();

    /*https://open.manmanapp.com/guduo/guduomedia*/
    private PageRule rules = PageRule.build()
            .add("createTask", page -> createTask(page))
            .add("guduomedia", page -> getRank(page));

    private void createTask(Page page) {
        Job job = ((DelayRequest) page.getRequest()).getJob();

        Long time = System.currentTimeMillis() / 1000L;
        String sign = DigestUtils.md5Hex(MMMH_KEY + time);

        String url = String.format(INIT_OPEN_URL, sign, String.valueOf(time));
        //漫画数据jsonurl
        Job newJob = new Job(url);
        DbEntityHelper.derive(job, newJob);
        newJob.setCode(sign);
        putModel(page, newJob);
    }

    private void getRank(Page page) {
        Job job = ((DelayRequest) page.getRequest()).getJob();
        if (StringUtils.equals("error", page.getRawText())) {
            return;
        }
        JSONObject jsonObject = JSONObject.parseObject(page.getRawText());
        if (1 != jsonObject.getInteger("status")) {
            return;
        }
        JSONArray data = jsonObject.getJSONArray("data");
        Date today = DateHelper.getTodayZero(Date.class);
        for (Object comicInfo : data) {
            JSONObject comic = (JSONObject) comicInfo;
            Integer id = comic.getInteger("id");
            Integer comments = comic.getInteger("comments");
            Integer likes = comic.getInteger("likes");
            Integer reads = comic.getInteger("reads");

            ComicMmmh mmmh = new ComicMmmh();
            mmmh.setCode("mmmh-"+id);
            mmmh.setDay(today);
            mmmh.setCommentNum(comments);
            mmmh.setLikesNum(likes);
            mmmh.setReadsNum(reads);
            putModel(page, mmmh);

            String comicUrl = String.format(COMIC_DETAIL_URL, id);
            Job newJob = new Job(comicUrl);
            DbEntityHelper.derive(job, newJob);
            newJob.setMethod("GET");
            newJob.setCode("mmmh-" + id);
            putModel(page, newJob);

        }


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
