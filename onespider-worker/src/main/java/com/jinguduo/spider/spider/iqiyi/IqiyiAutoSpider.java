package com.jinguduo.spider.spider.iqiyi;


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
import com.jinguduo.spider.common.constant.CommonEnum;
import com.jinguduo.spider.common.constant.FrequencyConstant;
import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.common.util.Md5Util;
import com.jinguduo.spider.data.table.AutoFindLogs;
import com.jinguduo.spider.data.table.Category;
import com.jinguduo.spider.data.table.Show;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.selector.Html;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by csonezp on 2016/10/25.
 */
@Worker
@CommonsLog
public class IqiyiAutoSpider extends CrawlSpider {


    List<String> JAPAN_ANIME_URLS = Lists.newArrayList(
            //日漫
            "https://pcw-api.iqiyi.com/search/video/videolists?access_play_control_platform=14&channel_id=4&data_type=1&from=pcw_list&is_album_finished=&is_purchase=&key=&market_release_date_level=&mode=4&pageNum=1&pageSize=48&site=iqiyi&source_type=&three_category_id=38;must,30220;must&without_qipu=1"
    );

    private Site site = SiteBuilder.builder().setDomain("list.iqiyi.com").build();

    private PageRule rules = PageRule.build()
            .add("/www/4/38", page -> generateJapanAnimePage(page));

    private void generateJapanAnimePage(Page page) {

        Job oldJob = ((DelayRequest) page.getRequest()).getJob();


        for (String url : JAPAN_ANIME_URLS) {
            Job newJob = new Job(url);

            DbEntityHelper.derive(oldJob, newJob);
            String code = Md5Util.getMd5(url);
            newJob.setCode(code);
            putModel(page, newJob);
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

}