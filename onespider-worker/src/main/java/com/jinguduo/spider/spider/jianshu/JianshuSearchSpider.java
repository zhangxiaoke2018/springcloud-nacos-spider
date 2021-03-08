package com.jinguduo.spider.spider.jianshu;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.data.table.bookProject.JianshuBookLogs;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.utils.UrlUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by lc on 2020/1/8
 */
@Worker
@Slf4j
public class JianshuSearchSpider extends CrawlSpider {

    //q = keyword && page < 5 && order_by (default/top/published_at/commented_at)
    private static final String JIANSHU_BOOK_SEARCH_INIT_URL = "https://www.jianshu.com/search/do?q=%s&type=note&page=%s&order_by=%s";

    private static final int MAX_PAGE_NUM = 4;

    private static final List<String> ORDER_TYPE_LIST = new ArrayList<String>() {{
        add("top");
        add("published_at");
        add("commented_at");
    }};

    private Site site = SiteBuilder.builder()
            .setDomain("www.jianshu.com")
            .addHeader("accept", "application/json")
            .addCookie("_m7e_session_core", "MINI_SPIDER")
            .build();

    private PageRule rules = PageRule.build()
            .add("/search/do", page -> getSearchList(page));

    //POST 方法才会进入这个里边
    private void getSearchList(Page page) {
        Job oldJob = ((DelayRequest) page.getRequest()).getJob();

        JSONObject jsonObject = JSONObject.parseObject(page.getRawText());

        String key = jsonObject.getString("q");

        JSONArray entries = jsonObject.getJSONArray("entries");

        for (int i = 0; i < entries.size(); i++) {
            JSONObject searchResult = entries.getJSONObject(i);

            String title = searchResult.getString("title");
            title = title.replaceAll("\\<.*?>", "");

            Integer likeCount = searchResult.getInteger("likes_count");
            Integer viewsCount = searchResult.getInteger("views_count");
            Integer commentCount = searchResult.getInteger("public_comments_count");
            //简书code
            String slug = searchResult.getString("slug");
            Date shareTime = searchResult.getDate("first_shared_at");
            JianshuBookLogs jb = new JianshuBookLogs();
            jb.setKeyword(key);
            jb.setCode(slug);
            jb.setTitle(title);
            jb.setLikes(likeCount);
            jb.setViews(viewsCount);
            jb.setComments(commentCount);
            jb.setShareTime(shareTime);
            putModel(page, jb);
        }

        //分裂任务
        //https://www.jianshu.com/search/do?q=%s&type=note&page=%s&order_by=%s
        //if page = 1 >> page = (2/3/4)
        //order_by = default >> order_by = (top/published_at/commented_at)
        String oldUrl = oldJob.getUrl();
        Map<String, String> allParam = UrlUtils.getAllParam(oldUrl);
        String q = allParam.get("q");
        String pageNum = allParam.get("page");
        String order_by = allParam.get("order_by");

        if ("1".equals(pageNum)) {
            if ("default".equals(order_by)) {
                for (String orderType : ORDER_TYPE_LIST) {
                    createNextJob(page, oldJob.getCode(), q, pageNum, orderType);
                }
            }
            for (int i = 2; i < MAX_PAGE_NUM; i++) {
                createNextJob(page, oldJob.getCode(), q, String.valueOf(i), order_by);
            }
        }
    }


    private void createNextJob(Page page, String code, String q, String pageNum, String orderType) {
        String nextUrl = String.format(JIANSHU_BOOK_SEARCH_INIT_URL, q, pageNum, orderType);
        Job nextJob = new Job(nextUrl);
        nextJob.setMethod("POST");
        nextJob.setCode(code);
        putModel(page, nextJob);
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
