package com.jinguduo.spider.spider.letv;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.util.CollectionUtils;

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
import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.data.text.BarrageText;
import com.jinguduo.spider.webmagic.Page;

@Worker
public class LeDanmuSpider extends CrawlSpider {

    private final int INCREASE_SCOPE = 300;//请求增长区间

    private Site sites = SiteBuilder.builder().setDomain("cdn.api.my.letv.com").build();

    private PageRule rules = PageRule.build()
            .add("\\&start\\=(\\d*)\\&",page -> getText(page));

    /**http://cdn.api.my.letv.com/danmu/list?vid=27247728&cid=2&start=0&getcount=1
     * 获取弹幕文本信息
     * @param page
     * start=300代表这部剧的第300秒
     */
    private void getText(Page page) {
        Job old = ((DelayRequest) page.getRequest()).getJob();

            JSONObject jsonStr = JSON.parseObject(page.getRawText());
            JSONObject json = jsonStr.getJSONObject("data");
            List<JSONObject> list = (List) json.getJSONArray("list");
            List<BarrageText> lists = Lists.newArrayListWithCapacity(list.size());
            if (!CollectionUtils.isEmpty(list)) {
                list.stream().forEach(j -> analysis(page, lists, old, j));
            }
            if(lists.size()>0){
                putModel(page, lists);
            }
            next(page, old, list);
    }

    private final static int MAX_SCOPE = 1 * 60 * 60;
    
    private void next(Page page, Job old,  List<JSONObject> list) {
            //当前进度
            int current = Integer.valueOf(page.getUrl().regex("\\&start\\=(\\d*)\\&",1).get());
            
            // 跳过Job生成: 时间超出一小时，且弹幕文本为空
            // http://cdn.api.my.letv.com/danmu/list?vid=26602272&cid=2&start=15900&getcount=1
            if (current >= MAX_SCOPE && (list == null || list.isEmpty())) {
                return;
            }

            List<Job> jobs = Lists.newArrayList();
            //计算下一个任务的进度
            final int next_scope = INCREASE_SCOPE + current;
            //创建递归任务
            final String nextUrl = page.getUrl().replace("start\\=(\\d*)", String.format("start=%s",next_scope)).get();
            //Db
            Job newJob = DbEntityHelper.deriveNewJob(old, nextUrl);
            newJob.setFrequency(FrequencyConstant.BARRAGE_TEXT);
            jobs.add(newJob);

            Job newJob2 = DbEntityHelper.deriveNewJob(
                    old,
                    page.getUrl().replace("start\\=(\\d*)", String.format("start=%s", INCREASE_SCOPE + next_scope)).get()
            );
            newJob2.setFrequency(FrequencyConstant.BARRAGE_TEXT);
            jobs.add(newJob2);

            putModel(page,jobs);
    }

    private void analysis(Page page, List<BarrageText> lists, Job job, JSONObject j) {
            BarrageText barrageText = new BarrageText(
                    j.getString("_id"),
                    j.getString("uid"),
                    "",
                    j.getLong("start"),
                    new Timestamp(j.getLong("addtime") * 1000),
                    0,
                    j.getString("txt")
            );
            DbEntityHelper.derive(job,barrageText);
            lists.add(barrageText);
    }

    @Override
    public PageRule getPageRule() {
        return rules;
    }

    @Override
    public Site getSite() {
        return sites;
    }
}
