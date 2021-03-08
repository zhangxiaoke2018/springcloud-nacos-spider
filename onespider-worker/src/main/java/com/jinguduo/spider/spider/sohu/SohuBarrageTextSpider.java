package com.jinguduo.spider.spider.sohu;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.spider.listener.UserAgentSpiderListener;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.constant.FrequencyConstant;
import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.data.table.CommentLog;
import com.jinguduo.spider.data.text.BarrageText;
import com.jinguduo.spider.data.text.CommentText;
import com.jinguduo.spider.webmagic.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.config.CookieSpecs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

/**
 * Created by gsw on 2016/12/26.
 */
@Worker
@Slf4j
public class SohuBarrageTextSpider extends CrawlSpider {

    private static Logger logger = LoggerFactory.getLogger(SohuBarrageTextSpider.class);

    private final static int INCREASE_SCOPE = 30;//请求增长区间

    private final static String COMMENT_CONTENT_URL = "https://api.my.tv.sohu.com/comment/api/v1/load?topic_id=%s&page_size=50&page_no=1";

    private Site site = SiteBuilder.builder()
            .setDomain("api.danmu.tv.sohu.com")
            .setCharset("UTF-8")
            .setCookieSpecs(CookieSpecs.IGNORE_COOKIES)
            .addSpiderListener(new UserAgentSpiderListener())
            .build();

    private PageRule rule = PageRule.build()
            .add("danmu", page -> getBarrageContent(page));



    private void getBarrageContent(Page page) {
        Job job = ((DelayRequest) page.getRequest()).getJob();
        String json_str = page.getRawText();

        //1.把弹幕对象放到list中
        List<JSONObject> jsonList = null;
        JSONObject jsonObject = null;
        if (null != json_str && !"".equals(json_str)) {
            jsonObject = JSON.parseObject(json_str);
            JSONObject json = jsonObject.getJSONObject("info");
            if (null == json || StringUtils.isEmpty(jsonObject.getString("info"))
                    || "{}".equals(jsonObject.getString("info"))) {
                return;
            }
            String jsonStr = json.getString("comments");
            if (StringUtils.isEmpty(jsonStr)
                    || "{}".equals(jsonStr) || "[]".equals(jsonStr)) {
                return;
            }
            jsonList = (List) json.getJSONArray("comments");
        }
        //2.遍历jsonList集合，得到每个弹幕实体对应的弹幕文本
        if (CollectionUtils.isNotEmpty(jsonList)) {
            List<BarrageText> dList = Lists.newArrayListWithCapacity(jsonList.size());

            jsonList.stream().forEach(json -> analysis(page, dList, job, json));
            //    createNextJob(page,job,jsonObject);
            putModel(page, dList);

        }
    }

    /**
     * 解析弹幕数据并封装到弹幕实体中
     * 搜狐视频弹幕没有分页，弹幕创建时间未确定
     * v = 10代表第10秒钟
     *
     * @param page
     * @param dList
     * @param job
     * @param json
     */
    private void analysis(Page page, List<BarrageText> dList, Job job, JSONObject json) {
        try {
            BarrageText dlogs = new BarrageText();
            dlogs.setContent(json.getString("c"));//文本
            dlogs.setReplyCount(json.getIntValue("fcount"));//点赞量
            dlogs.setBarrageId(json.getString("i"));//弹幕id
            dlogs.setShowTime(json.getLong("v"));//弹幕出现时间
            dlogs.setUserId(json.getString("uid"));//用户id
            DbEntityHelper.derive(job, dlogs);
            dList.add(dlogs);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
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
