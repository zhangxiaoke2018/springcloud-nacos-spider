package com.jinguduo.spider.spider.iqiyi;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
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
import com.jinguduo.spider.data.table.Category;
import com.jinguduo.spider.data.table.Show;
import com.jinguduo.spider.data.table.ShowPopularLogs;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.utils.UrlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Worker
@Slf4j
public class IqiyiHotPlayTimesSpider extends CrawlSpider {


    // Iqiyi 返回Code(sucess)标志
    private static String NORMAL_RESCODE = "A00000";
    private static Integer IQIYI_PLATFORM_ID = 2;

    //iqiyi channel_id_map

    private Site site = SiteBuilder.builder().setDomain("pcw-api.iqiyi.com").build();

    PageRule rule = PageRule.build()
            .add("/hotplaytimes", page -> analyzeHotCount(page))
            .add("/videolists", page -> autoFind(page));


    /**
     * 自动发现
     * https://pcw-api.iqiyi.com/search/video/videolists?access_play_control_platform=14&channel_id=4&data_type=1&from=pcw_list&is_album_finished=&is_purchase=&key=&market_release_date_level=&mode=4&pageNum=1&pageSize=48&site=iqiyi&source_type=&three_category_id=38;must,30220;must&without_qipu=1
     */
    private void autoFind(Page page) {
        Job oldJob = ((DelayRequest) page.getRequest()).getJob();
        String oldUrl = oldJob.getUrl();
        JSONObject json = JSONObject.parseObject(page.getJson().get());
        if (!NORMAL_RESCODE.equals(json.getString("code"))) {
            return;
        }
        JSONObject data = json.getJSONObject("data");
        JSONArray dataArray = data.getJSONArray("list");
        List<Job> jobs = new ArrayList<>();
        List<Show> shows = new ArrayList<>();
        for (int i = 0; i < dataArray.size(); i++) {
            JSONObject jsonObject = dataArray.getJSONObject(i);
            this.save(jsonObject,oldJob,Category.JAPAN_ANIME,shows,jobs);
        }

        //分页
        String pageNum = UrlUtils.getParam(oldUrl, "pageNum");
        if ("1".equals(pageNum)) {
            Integer pageTotal = data.getInteger("pageTotal");
            String prefix = "pageNum=";
            pageTotal = Math.min(pageTotal,10);
            for (int i = 2; i <= pageTotal; i++) {
                String newUrl = StringUtils.replace(oldUrl, "pageNum=1", prefix + i);
                Job pageJob = new Job(newUrl);
                DbEntityHelper.derive(oldJob, pageJob);
                String code = Md5Util.getMd5(newUrl);
                pageJob.setCode(code);
                putModel(page, pageJob);
            }
        }
        putModel(page,shows);
        putModel(page,jobs);

    }


    /**
     * 热度
     */
    private void analyzeHotCount(Page page) {
        Job job = ((DelayRequest) page.getRequest()).getJob();

        JSONObject json = JSONObject.parseObject(page.getJson().get());
        if (!NORMAL_RESCODE.equals(json.getString("code"))) {
            return;
        }
        JSONArray datas = json.getJSONArray("data");
        for (Object dataObj : datas) {
            JSONObject data = (JSONObject) dataObj;
            Long hot = data.getLong("hot");

            //log.info("Iqiyi HotCount get success! code" + job.getCode() + "; hotCount:" + hot);

            //save
            ShowPopularLogs logs = new ShowPopularLogs();
            logs.setCode(job.getCode());
            logs.setPlatformId(IQIYI_PLATFORM_ID);
            logs.setHotCount(hot);
            putModel(page, logs);
        }
    }

    /** 保存 */
    private void save(JSONObject s, Job old, Category category,List<Show> shows, List<Job> jobs) {

        String name = s.getString("name");
        String url = s.getString("playUrl");
        String code = s.getString("albumId");
        if(name.contains("DVD版")||name.contains("网络版")||name.endsWith("CUT")){
            return;
        }
        if(StringUtils.isBlank(code)){
            return;
        }
        Show show = new Show(name,code,CommonEnum.Platform.I_QI_YI.getCode(),0);
        if (url.indexOf("?") > 0) {
            url = url.substring(0, url.indexOf("?"));  // 带参数的页面模板不一样
        }
        show.setUrl(url);
        show.setSource(3);//3-代表自动发现的剧
        show.setCategory(category.name());
        //Job
        Job newJob = DbEntityHelper.deriveNewJob(old,url);
        newJob.setCode(code);
        newJob.setFrequency(FrequencyConstant.GENERAL_SHOW_INFO);

        shows.add(show);
        jobs.add(newJob);
    }

    @Override
    public PageRule getPageRule() {
        return rule;
    }

    /**
     * get the site settings
     *
     * @return site
     * @see Site
     */
    @Override
    public Site getSite() {
        return site;
    }
}
