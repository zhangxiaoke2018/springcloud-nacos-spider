package com.jinguduo.spider.spider.maoyan;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.data.table.Category;
import com.jinguduo.spider.data.table.MaoyanBox;
import com.jinguduo.spider.webmagic.Page;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * I'm a cute code dog
 * User: LingChen
 * Date:2018/1/19
 * Time:17:50
 */
@Worker
@Slf4j
@SuppressWarnings("all")
public class MaoyanSpider extends CrawlSpider {


    private Site site = SiteBuilder.builder().setDomain("box.maoyan.com").build();

    private static final Long DAILY_MSEC = 24 * 60 * 60 * 1000L;

    private static String INIT_DATA_URL = "http://box.maoyan.com/proseries/api/netmovie/boxRank.json?date=%s";

    /**
     * 时间：http://box.maoyan.com/proseries/api/netmovie/dateRange.json
     * 分账：http://box.maoyan.com/proseries/api/netmovie/boxRank.json?date=20180117
     */
    private PageRule rules = PageRule.build()
            .add("/dateRange", page -> getDates(page))
            .add("/boxRank", page -> analyze(page));

    private void getDates(Page page) {

        Job job = ((DelayRequest) page.getRequest()).getJob();

        JSONObject json = JSONObject.parseObject(page.getJson().get());

        Boolean success = json.getBoolean("success");
        if (success != true) return;

        JSONObject data = json.getJSONObject("data");
        JSONObject dateRange = data.getJSONObject("dateRange");
        String startDateStr = dateRange.getString("startDate");
        String endDateStr = dateRange.getString("endDate");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat urlSdf = new SimpleDateFormat("yyyyMMdd");
        Date startDate = null;
        Date endDate = null;
        try {
            startDate = sdf.parse(startDateStr);
            endDate = sdf.parse(endDateStr);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return;
        }
        //第一次抓取多天的分账数据
        /*for (;startDate.getTime() < endDate.getTime(); ) {
            //需要的时间
            startDate = new Date(startDate.getTime() + DAILY_MSEC);
            String urlDateStr = null;
            try {
                urlDateStr = urlSdf.format(startDate);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
            String dataUrl = String.format(INIT_DATA_URL, urlDateStr);
            Job newJob = new Job(dataUrl);
            DbEntityHelper.derive(job, newJob);
            putModel(page, newJob);
        }*/

        //以后只抓取7天的分账数据
        for (int i = 6; i >= 0; i--) {
            //需要的时间
            startDate = new Date(endDate.getTime() - DAILY_MSEC * i);
            String urlDateStr = null;
            try {
                urlDateStr = urlSdf.format(startDate);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
            String dataUrl = String.format(INIT_DATA_URL, urlDateStr);
            Job newJob = new Job(dataUrl);
            DbEntityHelper.derive(job, newJob);
            putModel(page, newJob);
        }


    }


    private void analyze(Page page) {
        Job job = ((DelayRequest) page.getRequest()).getJob();
        String url = job.getUrl();

        Date day = this.getMaoyanDateByUrl(url);
        String category = this.getCategoryByUrl(url);

        JSONObject json = JSONObject.parseObject(page.getJson().get());

        Boolean success = json.getBoolean("success");
        if (success != true) return;

        JSONArray data = json.getJSONArray("data");

        for (int i = 0; i < data.size(); i++) {
            JSONObject dataIn = data.getJSONObject(i);
            String dailyBoxStr = dataIn.getString("dailyBox");
            Integer movieId = dataIn.getInteger("movieId");
            String movieName = dataIn.getString("movieName");
            Integer releaseDays = dataIn.getInteger("releaseDays");
            String sumBox = dataIn.getString("sumBox");
            String weeklyBox = dataIn.getString("weeklyBox");
            BigDecimal bd = new BigDecimal(dailyBoxStr);
            Double v = bd.doubleValue();
            Double v1 = v * 10000;
            int dailyBox = v1.intValue();

            MaoyanBox box = new MaoyanBox();
            box.setCategory(category);
            box.setDay(day);
            box.setDailyBox(dailyBox);
            box.setMovieId(movieId);
            box.setMovieName(movieName);
            box.setReleaseDays(releaseDays);
            box.setSumBox(str2Int(sumBox));
            box.setWeeklyBox(str2Int(weeklyBox));
            putModel(page, box);

        }

    }

    private String getCategoryByUrl(String url) {
        //http://box.maoyan.com/proseries/api/netmovie/boxRank.json?date=20171231
        String cagetoryStr = StringUtils.substring(url, StringUtils.indexOf(url, "api/") + 4, StringUtils.indexOf(url, "/boxRank"));
        String cagetory = null;
        switch (cagetoryStr) {
            case "netmovie":
                cagetory = Category.NETWORK_MOVIE.name();
                break;
            default:
                cagetory = cagetoryStr;
                break;
        }
        return cagetory;
    }

    private Date getMaoyanDateByUrl(String url) {
        //http://box.maoyan.com/proseries/api/netmovie/boxRank.json?date=20171231
        String dateStr = StringUtils.substring(url, StringUtils.indexOf(url, "date=") + 5, url.length());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        try {
            Date parse = sdf.parse(dateStr);
            return parse;
        } catch (ParseException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * 反格式化数字
     */
    public static Integer str2Int(String num) {
        if (StringUtils.isBlank(num)) {
            return 0;
        }

        Integer number = 0;
        try {
            int index = num.indexOf("万");
            if (index != -1) {
                String numFormat = num.substring(0, index);
                BigDecimal bd = new BigDecimal(numFormat);
                Double v = bd.doubleValue();
                Double v1 = v * 10000;
                number = v1.intValue();
            }
        } catch (Exception e) {
            log.error("maoyan  数字格式化错误-->{}", num);
        }
        return number;
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
