package com.jinguduo.spider.spider.baidu;

import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.common.util.TextUtils;
import com.jinguduo.spider.data.table.TiebaArticleLogs;
import com.jinguduo.spider.data.table.TiebaLog;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.selector.Html;

import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.Lists;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.List;

@Worker
@CommonsLog
public class TiebaBaiduSpider extends CrawlSpider {

    private Site site = SiteBuilder.builder().setDomain("tieba.baidu.com").build();

    private PageRule rules = PageRule.build()
            .add("", page -> analyzeTiebaMovieDataProcess(page))//贴吧
            .add("pn", page -> processContent(page));

    /***
     * 解析贴吧媒体指数数据
     * @param page
     */
    private void analyzeTiebaMovieDataProcess(Page page) {

        if (page.getUrl().get().contains("pn")) {
            return;
        }

        Job job = ((DelayRequest) page.getRequest()).getJob();
        String url = page.getUrl().get();
        Html html = page.getHtml();

        Document document = html.getDocument();

        if (this.checkPage(document)) {
            return;
        }

        /** 关注数和帖子数 */
        try {
            // get FollowCount and PostCount
            String followCountStr = document.getElementsByClass("card_menNum").text().replaceAll(",", "");
            String postCountStr = document.getElementsByClass("card_infoNum").text().replaceAll(",", "");

            Integer followCount = Integer.valueOf(followCountStr);
            Integer postCount = Integer.valueOf(postCountStr);
            //save tieba data
            TiebaLog tiebaLog = new TiebaLog();
            tiebaLog.setCode(job.getCode());
            tiebaLog.setFollowCount(followCount);
            tiebaLog.setPostCount(postCount);
            putModel(page, tiebaLog);

        } finally {
            //生成第一页内容抓取任务
            Job newJob = new Job(url + "&pn=0");
            DbEntityHelper.derive(job, newJob);
            page.putField(Job.class.getSimpleName(), newJob);
        }
    }

    /**
     * 贴吧标题处理
     * 处理5页
     *
     * @param page
     */
    private void processContent(Page page) {

        Job job = ((DelayRequest) page.getRequest()).getJob();
        Document document = page.getHtml().getDocument();

        if (this.checkPage(document)) {
            return;
        }

        //打印空指针时的页面内容
        if (document.getElementById("content_wrap") == null || document.getElementById("content_wrap").getElementsByClass("cleafix") == null) {
            log.error(page.getHtml());
            return;
        }
        Element contentWrap = document.getElementById("content_wrap");
        Elements cleafix = contentWrap.getElementsByClass("cleafix");

        List<TiebaArticleLogs> list = Lists.newArrayList();
        for (Element element : cleafix) {

            //跳过置顶
            Elements iconTop = element.getElementsByClass("icon-top");
            if (iconTop != null && iconTop.size() > 0) {
                continue;
            }
            Elements threadlistRepNum = element.getElementsByClass("threadlist_rep_num");
            //回复数不存在的直接跳过
            if (threadlistRepNum == null || threadlistRepNum.size() == 0) {
                continue;
            }
            String repNum = threadlistRepNum.get(0).text();
            Element aElement = element.getElementsByClass("threadlist_title").get(0).getElementsByTag("a").get(0);
            String href = aElement.attr("href");
            String title = aElement.text();

            TiebaArticleLogs logs = new TiebaArticleLogs();
            logs.setCode(job.getCode());
            logs.setRepNum(null == repNum || repNum.equals("") ? 0 : Integer.valueOf(repNum));
            logs.setTitle(TextUtils.removeEmoji(title));
            logs.setUrl(href);

            if (logs.getTitle() == null) {
                continue;
            }

            list.add(logs);
        }
        page.putField(TiebaArticleLogs.class.getSimpleName(), list);

        //生成下一个任务
        Integer pn = Integer.valueOf(page.getUrl().regex("pn=(\\d+)", 1).get());
        //当前不是第五页就继续生成
        if (pn != 200) {
            String newUrl = page.getUrl().replace("pn=(\\d+)", "pn=" + String.valueOf(pn + 50)).get();
            Job nextJob = new Job(newUrl);
            DbEntityHelper.derive(job, nextJob);
            page.putField(Job.class.getSimpleName(), nextJob);
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


    /**
     * 过滤无效页面
     *
     * @param document 页面document
     *                 true:验证不通过。
     *                 false：验证通过
     */
    private boolean checkPage(Document document) {
        Elements titles = document.select("title");
        Element title = titles.get(0);
        String tit = title.text().trim();
        if (StringUtils.equals("百度贴吧", tit)) {
            return true;
        }
        Elements metas = document.getElementsByTag("meta");
        Elements attrs = metas.attr("name", "keywords");
        if (attrs.size() <= 3) {
            return true;
        }
        return false;
    }

}
