package com.jinguduo.spider.spider.kanmanhua;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Splitter;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.util.DateUtil;
import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.data.table.ComicBestSellingRank;
import com.jinguduo.spider.data.table.ComicOriginalBillboard;
import com.jinguduo.spider.webmagic.Page;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.jinguduo.spider.spider.kanmanhua.KanmanhuaSpider.XIAOMINGTAIJI_RANK_TYPE_MAP;
import static com.jinguduo.spider.spider.kanmanhua.KanmanhuaSpider.XIAOMINGTAIJI_SORT_TYPE_MAP;
import static com.jinguduo.spider.spider.kanmanhua.KanmanhuaSpider.XIAOMINGTAIJI_TIME_TYPE_MAP;

@Slf4j
@Worker
public class ComicKanmanhuaSpider extends CrawlSpider {

    //    private static final String SUPPORT_URL = "http://getconfig-globalapi.yyhao.com/app_api/v5/getcomicinfo_support/?comic_id=%s&give=1&platformname=android&productname=kmh";
    private static final String SUPPORT_URL = "http://comic.321mh.com/app_api/v5/getcomicinfo_influence/?rank_type=all&comic_id=%s&platformname=android&productname=kmh";
    private static final String BODY_URL = "http://getconfig-globalapi.yyhao.com/app_api/v5/getcomicinfo_body/?comic_id=%s&platformname=android&productname=kmh";

    private static final String COMMENT_URL = "http://community-hots.321mh.com/comment/count/?appId=1&commentType=2&ssid=%s&ssidType=0";

    private static final String HOT_COMMENT_TEXT_URL = "http://community-hots.321mh.com/comment/hotlist?appId=1&page=%s&pagesize=20&ssid=%s&contentType=0&ssidType=0";

    private static final String NEW_COMMENT_TEXT_URL = "http://community-hots.321mh.com/comment/newgets/?appId=1&page=%s&pagesize=20&ssid=%s&ssidType=0&sorttype=1&commentType=0&FatherId=0&isWater=0";


    private Site site = SiteBuilder.builder()
            .setDomain("rankdata-globalapi.321mh.com")
            .build();

    private PageRule rules = PageRule.build()
            .add("getRankDataDetials/", page -> billboardTask(page))
            .add("getRankDataDetials/\\?sort_type=all", page -> createTask(page))
            .add("getRankDataDetials/\\?sort_type=charge", page -> analyzeChangxiaoRank(page));

    //榜单保存
    private void billboardTask(Page page) {
        Job job = ((DelayRequest) page.getRequest()).getJob();
        String url = job.getUrl();
        JSONObject jsonObject = JSONObject.parseObject(page.getRawText());
        Integer status = jsonObject.getInteger("status");
        if (null == status || status != 0) {
            return;
        }

        String params = url.substring(url.indexOf("?") + 1, url.length());
        Map<String, String> paramMap = Splitter.on("&").withKeyValueSeparator("=").split(params);

        String sort_type = paramMap.get("sort_type");
        String rank_type = paramMap.get("rank_type");
        String time_type = paramMap.get("time_type");

        String billboardType =  XIAOMINGTAIJI_SORT_TYPE_MAP.get(sort_type)+ "/" + XIAOMINGTAIJI_RANK_TYPE_MAP.get(rank_type) + "/" + XIAOMINGTAIJI_TIME_TYPE_MAP.get(time_type);

        Integer platformId= 36;
        Date day = DateUtil.getDayStartTime(new Date());


        JSONArray datas = jsonObject.getJSONArray("data");
        List<ComicOriginalBillboard> list = new ArrayList<>();
        for (int i = 0; i < datas.size(); i++) {
            JSONObject data = (JSONObject) datas.get(i);
            String title = data.getString("comic_name");
            String code = "kan-" + data.getString("comic_id");
            Long count_day = data.getLong("count_day");
            Date billboardTime = new Date(count_day);

            ComicOriginalBillboard billboard = new ComicOriginalBillboard();
            billboard.setDay(day);
            billboard.setPlatformId(platformId);
            billboard.setBillboardType(billboardType);
            billboard.setRank(i+1);
            billboard.setCode(code);
            billboard.setName(title);
            billboard.setBillboardUpdateTime(billboardTime);
            list.add(billboard);
        }
        putModel(page,list);

    }

    private void analyzeChangxiaoRank(Page page) {
        Job job = ((DelayRequest) page.getRequest()).getJob();
        JSONObject jsonObject = JSONObject.parseObject(page.getRawText());
        Integer status = jsonObject.getInteger("status");
        if (null == status || status != 0) {
            return;
        }
        JSONArray datas = jsonObject.getJSONArray("data");

        Integer platformId= 36;

        Date day = DateUtil.getDayStartTime(new Date());

        int eachSize = datas.size() + 1;
        for (int i = 1; i < eachSize; i++) {
            JSONObject data = (JSONObject) datas.get(i - 1);
            String title = data.getString("comic_name");
            String code = "kan-" + data.getInteger("comic_id");
            Integer rise = data.getInteger("rise_rank");
            Integer riseStatus = rise > 0 ? 1 : rise < 0 ? -1 : 0;


            ComicBestSellingRank cr = new ComicBestSellingRank();
            cr.setPlatformId(platformId);
            cr.setDay(day);
            cr.setRank(i);
            cr.setCode(code);
            cr.setName(title);
            cr.setRiseStatus(riseStatus);
            cr.setRise(rise);
            putModel(page,cr);

        }

    }


    public void createTask(Page page) {
        Job job = ((DelayRequest) page.getRequest()).getJob();

        JSONObject json = JSONObject.parseObject(page.getJson().get());
        JSONArray list = json.getJSONArray("data");
        for (int i = 0; i < list.size(); i++) {
            JSONObject dataIn = list.getJSONObject(i);
            Integer comic_id = dataIn.getInteger("comic_id");

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

            try {
                //爬取热评文本 爬取20页
                for (int pageNum = 1; pageNum <= 5; pageNum++) {
                    String commentTextUrl = String.format(HOT_COMMENT_TEXT_URL, pageNum, comic_id);
                    Job job5 = new Job(commentTextUrl);
                    job5.setCode("kan-" + comic_id);
                    putModel(page, job5);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                //爬取最新评论文本 50页
                for (int pageNum = 1; pageNum <= 5; pageNum++) {
                    String commentTextUrl = String.format(NEW_COMMENT_TEXT_URL, pageNum, comic_id);
                    Job job6 = new Job(commentTextUrl);
                    job6.setCode("kan-" + comic_id);
                    putModel(page, job6);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
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
