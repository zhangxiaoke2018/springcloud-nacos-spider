package com.jinguduo.spider.spider.tengxun;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.http.client.config.CookieSpecs;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
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
import com.jinguduo.spider.common.util.RegexUtil;
import com.jinguduo.spider.data.table.BarrageLog;
import com.jinguduo.spider.data.text.BarrageText;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.selector.Html;

@Worker
public class TengxunDanmuSpider extends CrawlSpider {

    private final static int INCREASE_SCOPE = 30;//请求增长区间

    private Site site = SiteBuilder.builder()
            .setDomain("mfm.video.qq.com")
            .setCookieSpecs(CookieSpecs.IGNORE_COOKIES)
            .addSpiderListener(new UserAgentSpiderListener())
            .addHeader("cache-control", "no-cache")
            .setCharset("UTF-8")
            .build();

    private PageRule rule = PageRule.build()
            .add("^(?!.*(timestamp=\\d*)).*$", page -> getCount(page))
            .add("timestamp\\=(\\d*)", page -> contents(page));

    /***
     * 弹幕文本解析 <br/>生成下一个任务
     * content : https://mfm.video.qq.com/danmu?otype=json&timestamp=15&target_id=1591317974&session_key=0%2C0%2C0
     * @param page
     */
    private void contents(Page page) {
        Job oldJob = ((DelayRequest) page.getRequest()).getJob();

        JSONObject json = JSONObject.parseObject(page.getRawText());
        List<JSONObject> list = json.getObject("comments", new TypeReference<List<JSONObject>>(){});

        if (CollectionUtils.isNotEmpty(list)) {
            List<BarrageText> bs = Lists.newArrayListWithCapacity(list.size());
            list.stream().forEach(j -> text(page, bs, oldJob, j));
            putModel(page, bs);
        }
        next(page, oldJob, list);
    }

    private void text(Page page, List<BarrageText> bs, Job oldJob, JSONObject j) {
        BarrageText barrageText = new BarrageText(
                j.getString("commentid"),
                "",
                j.getString("opername"),
                j.getLong("timepoint"),//秒数  timepoint/60 = current show time
                null,
                j.getIntValue("upcount"),
                j.getString("content")
        );
        DbEntityHelper.derive(oldJob,barrageText);
        bs.add(barrageText);
    }

    private final static int MAX_SCOPE = 40 * 60;  // 40分钟
    
    private void next(Page page, Job old, List<JSONObject> list) {
        //当前进度
        int current = Integer.valueOf(page.getUrl().regex("\\&timestamp\\=(\\d*)\\&",1).get());
        
        // 跳过Job生成: 时间超出一小时，且弹幕文本为空
        if (current >= MAX_SCOPE && (list == null || list.isEmpty())) {
            return;
        }
        List<Job> jobs = Lists.newArrayList();
        //计算下一个任务的进度
        final int next_scope = INCREASE_SCOPE + current;
        //创建递归任务
        final String nextUrl = page.getUrl().replace("timestamp\\=(\\d*)", String.format("timestamp=%s",next_scope)).get();
        //Db
        Job newJob = DbEntityHelper.deriveNewJob(old, nextUrl);
        newJob.setFrequency(FrequencyConstant.BARRAGE_TEXT);
        jobs.add(newJob);

        putModel(page,jobs);
    }

    private void getCount(Page page) {
        Html html = page.getHtml();
        Job job = ((DelayRequest) page.getRequest()).getJob();
        String tol_up = RegexUtil.getDataByRegex(html.toString(),"\"tol_up\"\\s*:\\s*(\\d*)");
        int danmuCount = Integer.parseInt(tol_up);
        BarrageLog barrageLog = new BarrageLog(danmuCount);
        DbEntityHelper.derive(job, barrageLog);
        putModel(page,barrageLog);
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
