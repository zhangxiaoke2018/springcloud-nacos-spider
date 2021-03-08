package com.jinguduo.spider.spider.youku;


import com.jinguduo.spider.common.exception.AntiSpiderException;
import lombok.extern.apachecommons.CommonsLog;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;

import com.alibaba.fastjson.JSONObject;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.util.RegexUtil;
import com.jinguduo.spider.data.table.Show;
import com.jinguduo.spider.webmagic.Page;

import java.util.Date;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @author liuxinglong
 * @DATE 2017年5月3日 下午2:17:27
 */
@Worker
@CommonsLog
public class YoukuApiSpider extends CrawlSpider {

    private Site site = SiteBuilder.builder().setDomain("api.m.youku.com").build();

    private PageRule rules = PageRule.build()
            .add("getshowlist", page -> createVarietyEpiShow(page));

    private void createVarietyEpiShow(Page page) {
        Job oldJob = ((DelayRequest) page.getRequest()).getJob();

        String code = RegexUtil.getDataByRegex(oldJob.getUrl(), "vid=(([0-9]|[a-z]|[A-Z]|[+,/,=])*)&", 1);
        JSONObject o = new JSONObject();

        try {
            o = JSONObject.parseObject(page.getRawText());
        } catch (Exception ex){
//            throw new AntiSpiderException("json解析异常:" + page.getUrl().get());
            // pass
        }

        if (StringUtils.equals(o.getString("msg"), "success")) {
            JSONObject data = o.getJSONObject("data");
            Integer epi = 0;
            JSONObject j = (JSONObject) data.getJSONArray("items").get(0);
            String title = j.getString("title");
            if (title.contains("预告") || title.contains("明日精彩")) {
                return;
            }
            Date publishTime = j.getDate("publishtime");
            String cate = data.getString("cate");
            if("综艺".equals(cate)){
                epi = Integer.valueOf(DateFormatUtils.format(publishTime, "yyyyMMdd"));
            } else {
                epi = j.getInteger("show_videoseq");
            }

            //北京女子图鉴做特殊处理
            if (code.equals("XMzUyMDMxMjgzNg==")) {
                epi = 1;
            }
            String showName = title;

            Show show = new Show();
            show.setParentId(oldJob.getPlatformId());

            show.setDepth(2);
            show.setName(showName);
            show.setCode(code);
            show.setEpisode(epi);
            show.setReleaseDate(publishTime);

            show.setParentId(oldJob.getShowId());
            show.setParentCode(oldJob.getCode());
            putModel(page, show);
            log.debug("create youku variety shows success," + showName + "--[" + epi + "],url:" + oldJob.getUrl());
        } else {
            log.debug("create youku variety shows fail, errMsg:" + o.getString("msg") + ",url:" + oldJob.getUrl());
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
