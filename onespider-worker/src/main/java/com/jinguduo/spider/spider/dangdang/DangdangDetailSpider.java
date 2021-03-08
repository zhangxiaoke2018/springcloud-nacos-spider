package com.jinguduo.spider.spider.dangdang;

import com.alibaba.fastjson.JSONObject;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.cluster.spider.CrawlSpider;
import com.jinguduo.spider.cluster.spider.PageRule;
import com.jinguduo.spider.cluster.spider.Site;
import com.jinguduo.spider.cluster.spider.SiteBuilder;
import com.jinguduo.spider.cluster.worker.Worker;
import com.jinguduo.spider.common.util.DateUtil;
import com.jinguduo.spider.data.table.DangdangCategory;
import com.jinguduo.spider.data.table.DangdangCategoryRelation;
import com.jinguduo.spider.data.table.bookProject.ChildrenBook;
import com.jinguduo.spider.data.table.bookProject.ChildrenBookComment;
import com.jinguduo.spider.data.table.bookProject.ChildrenBookCommentText;
import com.jinguduo.spider.webmagic.Page;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.ParseException;
import java.util.*;

@Worker
public class DangdangDetailSpider extends CrawlSpider {

    private static final Integer DANGDANG_PLATFORM_ID = 59;


    private Site site = SiteBuilder.builder()
            .setDomain("product.dangdang.com")
            .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36")
            .build();

    private PageRule rules = PageRule.build()
            .add(".com/[0-9]\\d*.html", page -> processDetailInfo(page))
            .add("r=comment%2Flist", page -> processComment(page));


    private void processDetailInfo(Page page) {
        Job oldJob = ((DelayRequest) page.getRequest()).getJob();

        Document document = page.getHtml().getDocument();
        Element nameInfo = document.getElementsByClass("name_info").first();
        if (null == nameInfo) return;
        String bookName = nameInfo.getElementsByTag("h1").attr("title");
        String intro = nameInfo.getElementsByClass("head_title_name").first().attr("title");

        Element messBox = document.getElementsByClass("messbox_info").first();
        String author = messBox.getElementById("author").text().trim();
        author = author.replace("作者:", "");
        String press = messBox.getElementsByAttributeValue("dd_name", "出版社").last().text();
        String publishTimeStr = messBox.getElementsByTag("span").get(2).text().trim();
        Date publishTime = null;
        try {
            publishTimeStr = publishTimeStr.replace("出版时间:", "").replace(" ", "");
            publishTime = DateUtils.parseDate(publishTimeStr, "yyyy年MM月");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Elements imgs = document.getElementById("main-img-slider").getElementsByTag("img");
        Set<String> imgSet = new HashSet();
        for (Element img : imgs) {
            String initImg = img.attr("src");
            //小图转中图 小图：_x_ 中图：_w_ 大图：_u_
            initImg = StringUtils.replace(initImg, "_x_", "_w_");
            imgSet.add(initImg);
        }
        String headerImg = StringUtils.join(imgSet, "\\");


        Elements ISBNLis = document.getElementById("detail_describe").getElementsByTag("li");
        String ISBN = "";
        for (Element isbnLi : ISBNLis) {
            if (isbnLi.text().contains("ISBN")) {
                String isbnText = isbnLi.text();
                ISBN = isbnText.replace("国际标准书号ISBN：", "");
            }
        }


        Element fenleiElement = document.getElementsByClass("clearfix fenlei").first();
        Elements fenleiList = fenleiElement.getElementsByClass("lie");
        List<String> categoryStrList = new ArrayList<>();

        for (Element fenleiSpan : fenleiList) {
            List<String> categoryList = new ArrayList<>();
            Elements fenleiATags = fenleiSpan.getElementsByTag("a");
            int parentId = 0;
            for (int i = 0; i < fenleiATags.size(); i++) {
                Element aTag = fenleiATags.get(i);
                String categoryId = aTag.attr("data-category-id");
                String categoryName = aTag.text();
                //保存当当树状结构类型
                DangdangCategory dc = new DangdangCategory();
                Integer thisCategoryId = Integer.valueOf(categoryId);
                dc.setId(thisCategoryId);
                dc.setName(categoryName);
                dc.setParentId(parentId);
                putModel(page, dc);
                parentId = thisCategoryId.intValue();
                //保存当当书籍分类
                if (i == fenleiATags.size() - 1) {
                    DangdangCategoryRelation dcr = new DangdangCategoryRelation();
                    dcr.setBookCode(oldJob.getCode());
                    dcr.setCategoryId(Integer.valueOf(categoryId));
                    putModel(page, dcr);
                }
                categoryList.add(categoryName);
            }
            String categoryStr = StringUtils.join(categoryList, ">");
            categoryStrList.add(categoryStr);
        }

        //保存书籍信息
        ChildrenBook cb = new ChildrenBook();
        cb.setPlatformId(DANGDANG_PLATFORM_ID);
        cb.setCode(oldJob.getCode());
        cb.setName(bookName);
        cb.setHeaderImg(headerImg);
        cb.setIntro(intro);
        cb.setAuthor(author);
        cb.setPublisher(press);
        cb.setPublishTime(publishTime);
        cb.setCategory(StringUtils.join(categoryStrList, "\\"));
        cb.setIsbn(ISBN.trim());
        putModel(page, cb);

    }

    private void processComment(Page page) {
        Job oldJob = ((DelayRequest) page.getRequest()).getJob();
        JSONObject jsonObject = JSONObject.parseObject(page.getRawText());
        String errMsg = jsonObject.getString("errMsg");
        if (StringUtils.isNotEmpty(errMsg)) {
            return;
        }

        JSONObject summary = jsonObject.getJSONObject("data").getJSONObject("list").getJSONObject("summary");
        Integer commentCount = summary.getInteger("total_comment_num");
        Integer greatCount = summary.getInteger("total_crazy_count");
        Integer indifferentCount = summary.getInteger("total_indifferent_count");
        Integer detestCount = summary.getInteger("total_detest_count");
        Float goodRate = summary.getFloat("goodRate");

        ChildrenBookComment cbc = new ChildrenBookComment();
        cbc.setPlatformId(DANGDANG_PLATFORM_ID);
        cbc.setCode(oldJob.getCode());
        cbc.setDay(DateUtil.getDayStartTime(new Date()));
        cbc.setCommentCount(commentCount);
        cbc.setGreatCount(greatCount);
        cbc.setIndifferentCount(indifferentCount);
        cbc.setDetestCount(detestCount);
        cbc.setGoodRate(goodRate);
        putModel(page, cbc);

        //获取评论文本第一页
        String commentHtml = jsonObject.getJSONObject("data").getJSONObject("list").getString("html");
        if (StringUtils.isEmpty(commentHtml)) return;
        Document commentDocument = Jsoup.parse(commentHtml);
        Elements items = commentDocument.getElementsByClass("comment_items clearfix");
        if (null == items || items.isEmpty()) return;
        Date firstCommentDate = new Date();

        for (Element item : items) {
            try {
                Element items_right = item.getElementsByClass("items_right").first();
                Element items_left_pic = item.getElementsByClass("items_left_pic").first();
                String commentContent = items_right.getElementsByClass("describe_detail").text();
                String commentId = items_right.getElementsByClass("support").attr("data-comment-id");
                String timeStr = items_right.getElementsByClass("starline clearfix").first().getElementsByTag("span").first().text();
                Date date = DateUtils.parseDate(timeStr, "yyyy-MM-dd HH:mm:ss");
                firstCommentDate = firstCommentDate.compareTo(date) == -1 ? firstCommentDate : DateUtil.getDayStartTime(date);
                String author = items_left_pic.getElementsByClass("name").text();
                //保存评论。每个书籍10条左右 commentContent,date,author
                ChildrenBookCommentText text = new ChildrenBookCommentText();
                text.setCode(oldJob.getCode());
                text.setPlatformId(DANGDANG_PLATFORM_ID);
                text.setCommentId(commentId);
                text.setCommentContent(commentContent);
                text.setPublishTime(date);
                text.setPublisher(author);
                putModel(page,text);
            } catch (Exception e) {
                continue;
            }
        }
        //保存评论时间
        ChildrenBook cb = new ChildrenBook();
        cb.setCode(oldJob.getCode());
        cb.setPlatformId(DANGDANG_PLATFORM_ID);
        cb.setPublishTime(firstCommentDate);
        putModel(page,cb);

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
