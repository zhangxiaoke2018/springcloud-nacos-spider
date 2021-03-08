package com.jinguduo.spider.spider.dangdang;

import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.util.DateUtil;
import com.jinguduo.spider.common.util.Md5Util;
import com.jinguduo.spider.data.table.bookProject.ChildrenBookBillboard;
import com.jinguduo.spider.webmagic.Page;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by lc on 2019/11/27
 * 以下两个url均为入口
 * 畅销榜
 * http://bang.dangdang.com/books/childrensbooks/01.41.00.00.00.00-24hours-0-0-1-1-bestsell
 * 新书榜
 * http://bang.dangdang.com/books/childrensbooks/01.41.00.00.00.00-24hours-0-0-1-1-newhotsell
 */
@Worker
public class DangdangRankSpider extends CrawlSpider {

    private static final Integer DANGDANG_PLATFORM_ID = 59;
    private static final Integer PAGE_SIZE = 20;
    private static final String DD_URL_SEPARATOR = "-";
    private static final String COMMENT_INIT_URL_PRE = "http://product.dangdang.com/index.php?r=comment%2Flist&";
    private static final String COMMENT_INIT_URL_SUF = "productId=%s&mainProductId=%s";

    private static Map<String,String> CATEGORY_CODE_MAP = new HashMap<String, String>(){{
        put("01.41.00.00.00.00","全部");
        put("01.41.26.00.00.00","中国儿童文学");
        put("01.41.27.00.00.00","外国儿童文学");
        put("01.41.70.00.00.00","绘本/图画书");
        put("01.41.05.00.00.00","科普/百科");
        put("01.41.44.00.00.00","婴儿读物");
        put("01.41.45.00.00.00","幼儿启蒙");
        put("01.41.46.00.00.00","益智游戏");
        put("01.41.48.00.00.00","玩具书");
        put("01.41.50.00.00.00","卡通/动漫");
        put("01.41.51.00.00.00","少儿英语");
        put("01.41.55.00.00.00","励志/成长");
        put("01.41.57.00.00.00","进口儿童书");
    }};

    private Site site = SiteBuilder.builder()
            .setDomain("bang.dangdang.com")
            .build();

    private PageRule rules = PageRule.build()
            .add("24hours-0-0-1-1-bestsell", page -> createTask(page))
            .add("24hours-0-0-1-1-newhotsell", page -> createTask(page))
            .add("/books/childrensbooks", page -> processRank(page));

    private void processRank(Page page) {
        Job oldJob = ((DelayRequest) page.getRequest()).getJob();
        String oldUrl = oldJob.getUrl();
        Document document = page.getHtml().getDocument();
        Element data = document.getElementsByClass("bang_list clearfix bang_list_mode").first();
        //被封后直接抛弃
        if (null == data)return;
        Elements li = data.getElementsByTag("li");
        Date day = DateUtil.getDayStartTime(new Date());
        Map<String, String> paramsMap = processRankParam(oldUrl);
        Integer pageNum = Integer.valueOf(paramsMap.get("pageNum"));//1开始
        String rankType = paramsMap.get("rankType");
        for (int i = 0; i < li.size(); i++) {
            Element bookDiv = li.get(i);

            String rankStr = bookDiv.getElementsByClass("list_num").text();
            Integer rankNum;
            if (StringUtils.isEmpty(rankStr)) {
                //此条件依赖于 PAGE_SIZE 的准确性，如目标url参数调整，会引起排序紊乱。
                rankNum = ((pageNum - 1) * PAGE_SIZE) + i + 1;
            } else {
                rankNum = Integer.valueOf(rankStr.trim().replace(".", ""));
            }
            Element picDiv = bookDiv.getElementsByClass("pic").first();

            // String headerImg = picDiv.getElementsByTag("img").attr("src");
            String detailHref = picDiv.getElementsByClass("img").attr("href");
            String bookCode = StringUtils.substring(detailHref,
                    StringUtils.lastIndexOf(detailHref, "/") + 1,
                    StringUtils.lastIndexOf(detailHref, ".html"));

            //保存排名
            ChildrenBookBillboard cbb = new ChildrenBookBillboard();
            cbb.setDay(day);
            cbb.setPlatformId(DANGDANG_PLATFORM_ID);
            cbb.setCode(bookCode);
            cbb.setRank(rankNum);
            cbb.setType(rankType);
            putModel(page, cbb);

            //详情任务  http://product.dangdang.com/25102146.html
            Job detailJob = new Job(detailHref);
            detailJob.setCode(bookCode);
            detailJob.setPlatformId(DANGDANG_PLATFORM_ID);
            putModel(page, detailJob);

            //评论任务
            Job commentJob = new Job(COMMENT_INIT_URL_PRE + String.format(COMMENT_INIT_URL_SUF, bookCode, bookCode));
            commentJob.setCode(bookCode);
            commentJob.setPlatformId(DANGDANG_PLATFORM_ID);
            putModel(page, commentJob);

        }

        //下一页任务
        String nextPageUrl = this.getNextPageUrl(oldUrl);
        if (StringUtils.isEmpty(nextPageUrl)) {
            return;
        }
        Job nextJob = new Job(nextPageUrl);
        nextJob.setCode(Md5Util.getMd5(nextPageUrl));
        nextJob.setPlatformId(DANGDANG_PLATFORM_ID);
        putModel(page, nextJob);
    }

    /**
     * 切记生成任务时排除掉入口任务本身！！！！！！
     */
    private void createTask(Page page) {
        Document document = page.getHtml().getDocument();

        Element bangList = document.getElementsByClass("bang_list_date").first();
        Elements dateList = bangList.getElementsByClass("date_list");

        for (Element dateElement : dateList) {
            Elements dateATags = dateElement.getElementsByTag("a");
            for (Element aTag : dateATags) {
                String rankName = aTag.text();
                //排除月榜和年榜，只保存日榜，周榜，月榜
                if (StringUtils.isEmpty(rankName) || StringUtils.contains(rankName,"月") ||StringUtils.contains(rankName,"年")){
                    continue;
                }
                String href = aTag.attr("href");
                //排除入口任务，绝对禁止重复生成！！
                if (StringUtils.contains(href, "24hours")) continue;

                Job job = new Job(href);
                job.setPlatformId(DANGDANG_PLATFORM_ID);
                job.setCode(Md5Util.getMd5(href));
                putModel(page, job);
            }

        }


    }

    /**
     * rankType 榜单类型
     * pageNum 页码
     */
    //01.41.00.00.00.00 全部
    //01.41.26.00.00.00 中国儿童文学
    //01.41.27.00.00.00 外国儿童文学
    //01.41.70.00.00.00 绘本/图画书
    //01.41.05.00.00.00 科普/百科
    //01.41.44.00.00.00 婴儿读物
    //01.41.45.00.00.00 幼儿启蒙
    //01.41.46.00.00.00 益智游戏
    //01.41.48.00.00.00 玩具书
    //01.41.50.00.00.00 卡通/动漫
    //01.41.51.00.00.00 少儿英语
    //01.41.55.00.00.00 励志/成长
    //01.41.57.00.00.00 进口儿童书
    //01.41.69.00.00.00 少儿期刊
    //01.41.59.00.00.00 阅读工具书
    private Map<String, String> processRankParam(String url) {
        //  http://bang.dangdang.com/books/childrensbooks/01.41.00.00.00.00-24hours-0-0-1-1-bestsell
        Map result = new HashMap();
        url = StringUtils.substring(url,StringUtils.indexOf(url,"/01.41")+1);
        String[] ps = StringUtils.split(url, DD_URL_SEPARATOR);
        String typeCategory = ps[0];
        String typePrefix = ps[1];
        String typeYear = ps[2];
        String typeMonth = ps[3];
        String pageNum = ps[5];
        String typeSuffix = ps.length > 6 ? ps[6] : "bestsell";
        typeCategory = CATEGORY_CODE_MAP.get(typeCategory);
        if (StringUtils.isEmpty(typeCategory)){
            typeCategory = "未知";
        }
        if (StringUtils.contains(typePrefix, "hours")) {
            result.put("rankType", typeCategory+"\\"+typePrefix + "\\" + typeSuffix);
        } else if (StringUtils.contains(typePrefix, "recent")) {
            result.put("rankType", typeCategory+"\\"+typePrefix + "\\" + typeSuffix);
        } else if (StringUtils.contains(typePrefix, "month")) {
            result.put("rankType", typeCategory+"\\"+typePrefix + "\\" + typeYear + "-" + typeMonth + "\\" + typeSuffix);
        } else if (StringUtils.contains(typePrefix, "year")) {
            result.put("rankType", typeCategory+"\\"+typePrefix + "\\" + typeYear + "\\" + typeSuffix);
        }
        result.put("pageNum", pageNum);
        return result;

    }

    private String getNextPageUrl(String oldUrl) {
        String[] urlResolve = StringUtils.split(oldUrl, DD_URL_SEPARATOR);
        urlResolve[urlResolve.length - 2] = String.valueOf(Integer.valueOf(urlResolve[urlResolve.length - 2]) + 1);
        if (Integer.valueOf(urlResolve[urlResolve.length - 2]) > 50) {
            return "";
        }
        return StringUtils.join(urlResolve, DD_URL_SEPARATOR);
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
