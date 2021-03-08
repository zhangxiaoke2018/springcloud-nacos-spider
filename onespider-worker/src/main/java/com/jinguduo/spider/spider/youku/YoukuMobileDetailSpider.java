package com.jinguduo.spider.spider.youku;


import java.util.ArrayList;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.exception.AntiSpiderException;
import com.jinguduo.spider.common.exception.PageBeChangedException;
import com.jinguduo.spider.common.exception.QuickException;
import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.common.util.RegexUtil;
import com.jinguduo.spider.data.table.AdLinkedVideoInfos;
import com.jinguduo.spider.data.table.ShowLog;
import com.jinguduo.spider.webmagic.Page;

/**
 * 获取分集播放量
 *   <p> http://detail.mobile.youku.com/shows/efbfbd61237fefbfbdef/reverse/videos
 *
 */
@Worker
public class YoukuMobileDetailSpider extends CrawlSpider {

    private Site site = SiteBuilder.builder()
    		.setDomain("detail.mobile.youku.com")
    		.addSpiderListener(new YoukuMobileAppUrlSpiderListener())
    		.build();

    private PageRule rules = PageRule.build()
    		.add(".", page -> processPlayCount(page));

    private void processPlayCount(Page page) throws QuickException {
        Job job = ((DelayRequest) page.getRequest()).getJob();
        JSONObject resp = page.getJson().toObject(JSONObject.class);
        if("success".equals(resp.getString("status"))){
        	ArrayList<JSONObject> results = resp.getObject("results", new TypeReference<ArrayList<JSONObject>>(){});
        	if (results != null && !results.isEmpty()) {
				for (JSONObject r : results) {
					Long pc = r.getLong("total_pv");
					String videoId = r.getString("videoid");
					if (pc != null && videoId != null) {
						ShowLog showLog = DbEntityHelper.derive(job, new ShowLog());
						showLog.setPlayCount(pc);
						showLog.setCode(videoId);
						putModel(page, showLog);
					}
				}
			} else {
				throw new PageBeChangedException(page.getRawText());
			}
        } else {
        	throw new AntiSpiderException(page.getRawText());
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
