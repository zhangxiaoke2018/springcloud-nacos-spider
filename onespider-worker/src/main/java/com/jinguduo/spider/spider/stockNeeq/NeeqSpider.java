package com.jinguduo.spider.spider.stockNeeq;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.data.table.StockBulletin;
import com.jinguduo.spider.webmagic.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by lc on 2019/4/8
 * <p>
 * demo
 * http://www.neeq.com.cn/disclosureInfoController/infoResult.do?callback=JSON&disclosureType=5&page=0&companyCd=430002&startTime=2019-03-09&endTime=2019-04-08
 * http://www.neeq.com.cn/disclosureInfoController/infoResult.do?callback=JSON&disclosureType=5&page=0&companyCd=%s&startTime=%s
 */
@Worker
@Slf4j
public class NeeqSpider extends CrawlSpider {


    private Site site = SiteBuilder.builder()
            .setDomain("www.neeq.com.cn")
            .addHeader("Referer", "http://www.neeq.com.cn/disclosure/announcement.html")
            .addHeader("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 11_0 like Mac OS X) AppleWebKit/604.1.38 (KHTML, like Gecko) Version/11.0 Mobile/15A372 Safari/604.1")
            .build();

    private PageRule rules = PageRule.build()
            .add("/disclosureInfoController", page -> analyze(page));


    private static final String prefix = "http://www.neeq.com.cn";


    private void analyze(Page page) {
        String rawText = page.getRawText();
        String jsonStr = StringUtils.removeEnd(StringUtils.removeStart(rawText, "JSON("), ")");

        JSONArray jsonArray = JSONObject.parseArray(jsonStr);
        List<StockBulletin> list = new ArrayList<>();
        for (Object o : jsonArray) {
            JSONObject jsonObject = (JSONObject) o;
            JSONObject listInfo = jsonObject.getJSONObject("listInfo");
            if (null == listInfo) continue;

            JSONArray contents = listInfo.getJSONArray("content");
            for (Object inObj : contents) {
                JSONObject content = (JSONObject) inObj;
                String code = content.getString("disclosureCode");
                String path = content.getString("destFilePath");

                String title = content.getString("disclosureTitle");
                String companyCd = content.getString("companyCd");
                JSONObject upDate = content.getJSONObject("upDate");
                Long time = upDate.getLong("time");
                Date updateTime = new Date(time);

                if (StringUtils.isEmpty(code) || StringUtils.isEmpty(path) || StringUtils.isEmpty(title) || StringUtils.isEmpty(companyCd) || null == upDate)
                    continue;

                StockBulletin sb = new StockBulletin(code, companyCd, title, prefix + path, updateTime);
                list.add(sb);
            }
        }

        putModel(page, list);


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
