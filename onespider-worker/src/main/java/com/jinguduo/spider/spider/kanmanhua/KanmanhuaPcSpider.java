package com.jinguduo.spider.spider.kanmanhua;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.util.DateUtil;
import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.data.table.ComicBanner;
import com.jinguduo.spider.webmagic.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Date;

/**
 * Created by lc on 2019/8/12
 */
@Slf4j
@Worker
public class KanmanhuaPcSpider extends CrawlSpider {


    private static final String SUPPORT_URL = "http://comic.321mh.com/app_api/v5/getcomicinfo_influence/?rank_type=all&comic_id=%s&platformname=android&productname=kmh";
    private static final String BODY_URL = "http://getconfig-globalapi.yyhao.com/app_api/v5/getcomicinfo_body/?comic_id=%s&platformname=android&productname=kmh";
    private static final String COMMENT_URL = "http://community-hots.321mh.com/comment/count/?appId=1&commentType=2&ssid=%s&ssidType=0";

    private Site site = SiteBuilder.builder()
            .setDomain("www.kanman.com")
            .build();

    private PageRule rules = PageRule.build()
            .add("/api/getComicList",page -> processAllComic(page))
            .add("www.kanman.com$", page -> processHome(page));

    private void processHome(Page page) {
        Document document = page.getHtml().getDocument();

        String codePrefix = "kan-";
        Integer platformId = 36;
        Date day = DateUtil.getDayStartTime(new Date());
        String source = "HOME";


        Elements items = document.getElementsByClass("menu-item");
        for (Element item : items) {
            Element aTag = item.getElementsByTag("a").first();
            String href = aTag.attr("href");
            String name = aTag.text();
            String code = codePrefix + StringUtils.replace(href, "/", "");
            ComicBanner cb = new ComicBanner(code, platformId, day, name, source);

            putModel(page, cb);
        }
    }


    /**
     *
     * https://www.kanman.com/api/getComicList/?product_id=1&productname=kmh&platformname=pc
     * 所有漫画
     * */
    private void processAllComic(Page page) {
        Job job = ((DelayRequest) page.getRequest()).getJob();
        //返回数据为json格式转换为map
        JSONObject jsonObject = JSONObject.parseObject(page.getJson().get());
        Integer status = jsonObject.getInteger("status");
        if (0 != status)return;
        JSONArray datas = jsonObject.getJSONArray("data");

        for (int i = 0; i < datas.size(); i++) {
            JSONObject data = datas.getJSONObject(i);
            String comic_id = data.getString("comic_id");

            String supportUrl = String.format(SUPPORT_URL, comic_id);
            Job job2 = new Job(supportUrl);
            DbEntityHelper.derive(job, job2);
            job2.setCode("kan-" + comic_id);
            putModel(page, job2);

            String bodyUrl = String.format(BODY_URL, comic_id);
            Job job3 = new Job(bodyUrl);
            DbEntityHelper.derive(job, job3);
            job3.setCode("kan-" + comic_id);
            putModel(page, job3);

            String commentUrl = String.format(COMMENT_URL, comic_id);
            Job job4 = new Job(commentUrl);
            DbEntityHelper.derive(job, job4);
            job4.setCode("kan-" + comic_id);
            putModel(page, job4);

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
