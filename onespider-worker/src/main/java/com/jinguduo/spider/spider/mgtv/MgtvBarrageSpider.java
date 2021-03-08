package com.jinguduo.spider.spider.mgtv;

import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.constant.FrequencyConstant;
import com.jinguduo.spider.common.exception.AntiSpiderException;
import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.data.text.BarrageText;
import com.jinguduo.spider.webmagic.Page;

@Worker
public class MgtvBarrageSpider extends CrawlSpider {

    private Site site = SiteBuilder.builder()
            .setDomain("galaxy.person.mgtv.com")
            .setCharset("UTF-8")
            .build();

    private PageRule rule = PageRule.build()
            .add("/rdbarrage\\?\\&time=\\d*\\&vid=.*?\\&cid=.*", page -> textProcess(page));

    private final static int NEXT = 60000;// unit : ms
    private final static int MAX_SCOPE = 1 * 60 * 60 * 1000;// unit : ms


    private void textProcess(Page page) {
        Job oldJob = ((DelayRequest) page.getRequest()).getJob();

        JSONObject jsonObject = JSON.parseObject(page.getRawText());

//        if (!"0".equals(jsonObject.getString("status")) || jsonObject.getJSONObject("data") == null) {
//            throw new AntiSpiderException("fetch MgBarrage error by: " + page.getRawText());
//        }
        if ("0".equals(jsonObject.getString("status")) && jsonObject.getJSONObject("data") != null) {
            List<JSONObject> jsonArray = (List) (jsonObject.getJSONObject("data").getJSONArray("items"));
            next(page, oldJob, jsonArray);
            if (jsonArray == null || jsonArray.isEmpty()) {
                return;
            }

            final String[] id = new String[1];
            final String[] content = new String[1];
            final long[] time = new long[1];
            final String[] uid = new String[1];

            jsonArray.stream()
                    .forEach(json -> {
                        //String barrageId, String userId, String nickName, Long showTime, Timestamp createdTime, Integer up, String content
                        id[0] = json.getString("ids");
                        content[0] = json.getString("content");
                        Long showTimes = json.getLongValue("time");//所有平台统一存的单位为 s
                        if (showTimes != null) {
                            time[0] = (showTimes / 1000);
                        }
                        //type = json.getString("type");
                        uid[0] = json.getString("uid");
                        if (StringUtils.isNotBlank(id[0]) && StringUtils.isNotBlank(content[0])) {
                            putModel(page,
                                    DbEntityHelper.derive(
                                            oldJob,
                                            new BarrageText(
                                                    id[0],
                                                    uid[0],
                                                    null,
                                                    time[0],
                                                    null,//创建时间没有
                                                    0,
                                                    content[0]
                                            )
                                    )
                            );
                        }
                    });
        }
    }

    private void next(Page page, Job oldJob, Collection list) {
        //当前进度
        int current = Integer.valueOf(page.getUrl().regex("\\&time=(\\d*)\\&", 1).get());

        // 跳过Job生成: 时间超出一小时，且弹幕文本为空
        if (current >= MAX_SCOPE && (list == null || list.isEmpty())) {
            return;
        }
        List<Job> jobs = Lists.newArrayList();
        //计算下一个任务的进度
        final int next_scope = NEXT + current;
        //创建递归任务
        final String nextUrl = page.getUrl().replace("time\\=(\\d*)", String.format("time=%s", next_scope)).get();
        //Db
        Job newJob = DbEntityHelper.deriveNewJob(oldJob, nextUrl);
        newJob.setFrequency(FrequencyConstant.BARRAGE_TEXT);
        jobs.add(newJob);

        putModel(page, jobs);
    }

    @Override
    public PageRule getPageRule() {
        return rule;
    }

    @Override
    public Site getSite() {
        return site;
    }
}
