package com.jinguduo.spider.spider.fiction;

import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.constant.CommonEnum;
import com.jinguduo.spider.common.util.DateUtil;
import com.jinguduo.spider.data.table.FictionOriginalBillboard;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.utils.UrlUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by lc on 2019/6/27
 */
@Worker
public class ZHPcSpider extends CrawlSpider {
    private Site site = SiteBuilder.builder()
            .setDomain("www.zongheng.com")
            .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36")
            .build();

    private PageRule rules = PageRule.build()
            .add("/rank/details", this::processRank);

    private void processRank(Page page) throws ParseException {
        String url = page.getUrl().get();
        Document document = page.getHtml().getDocument();

        Elements boxs = document.getElementsByClass("rankpage_box");
        if (null == boxs || boxs.isEmpty()) return;
        Elements books = boxs.get(0).getElementsByClass("rank_d_list borderB_c_dsh clearfix");
        if (null == books) return;

        List<FictionOriginalBillboard> billboards = new ArrayList<>();
        Date day = DateUtil.getDayStartTime(new Date());

        Map<String, String> params = UrlUtils.getAllParam(url);
        String rankType = params.get("rt");
        rankType = this.getRankType(rankType);
        if (StringUtils.isEmpty(rankType)) return;
        String pageNum = params.get("p");
        Integer before = (Integer.parseInt(pageNum) - 1) * 20;

        //更新时间，不一定有
        Elements dropbox = document.getElementsByClass("dropbox");
        Date billboardUpdateTime = null;
        if (dropbox != null && !dropbox.isEmpty()) {
            Element drop = dropbox.get(0);
            Elements mayHasYearE = drop.getElementsByClass("select_module_con data-year");
            Elements mayHasMonthE = drop.getElementsByClass("select_module_con data-month");
            Elements mayHasDataE = drop.getElementsByClass("select_module_con data-date");

            if (!mayHasYearE.isEmpty() && !mayHasMonthE.isEmpty()) {
                String yearStr = mayHasYearE.attr("data-val");
                String monthStr = mayHasMonthE.attr("data-val");
                String timeStr = yearStr + "-" + monthStr + "-01";
                billboardUpdateTime = DateUtils.parseDate(timeStr, "yyyy-MM-dd", "yyyy-M-dd");
            } else if (!mayHasDataE.isEmpty()) {
                String timeStr = mayHasDataE.attr("data-val");//20190624
                billboardUpdateTime = DateUtils.parseDate(timeStr, "yyyyMMdd", "yyyy-MM-dd");
            }
        }

        for (int i = 0; i < books.size(); i++) {
            Element book = books.get(i);
            String bookid = book.attr("bookid");
            int rank = before + i + 1;
            FictionOriginalBillboard billboard = new FictionOriginalBillboard();
            billboard.setPlatformId(CommonEnum.Platform.ZONG_HENG.getCode());
            billboard.setType(rankType);
            billboard.setDay(day);
            billboard.setRank(rank);
            billboard.setCode(bookid);
            billboard.setBillboardUpdateTime(billboardUpdateTime);
            billboards.add(billboard);
        }

        putModel(page, billboards);

    }

    // 1：月票、3：畅销、4：新书
    private String getRankType(String initType) {
        String result = "";
        switch (initType) {
            case "1":
                result = "月票榜";
                break;
            case "3":
                result = "畅销榜";
                break;
            case "4":
                result = "新书榜";
                break;
            default:
                break;
        }
        return result;

    }

    public Site getSite() {
        return site;
    }

    public PageRule getPageRule() {
        return rules;
    }
}
