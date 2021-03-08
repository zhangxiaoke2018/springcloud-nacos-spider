package com.jinguduo.spider.spider.fiction;

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
import com.jinguduo.spider.common.util.DateUtil;
import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.data.table.Fiction;
import com.jinguduo.spider.data.table.FictionCommentLogs;
import com.jinguduo.spider.data.table.FictionOriginalBillboard;
import com.jinguduo.spider.data.table.FictionPlatformRate;
import com.jinguduo.spider.webmagic.Page;
import com.jinguduo.spider.webmagic.utils.UrlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by lc on 2019/7/1
 */
@Worker
@Slf4j
@SuppressWarnings("all")
public class SQMobileSpider extends CrawlSpider {

    //1.作者id 2. 网文id 3. 随机数（用户名）4.作者名称 5.网文名称
    private static final String COMMENT_SCORE_URL = "http://read.xiaoshuo1-sm.com/novel/i.php?do=sp_get&authorId=%s&bookId=%s&fetch=merge&sqUid=%s&score=yes&authorName=%s&bookName=%s";

    //bookId、user_id、timestamp、sign
    private static final String CLICK_URL = "http://content.shuqireader.com/andapi/book/info/?bookId=%s&user_id=%s&timestamp=%s&sign=%s";
    private Site site = new SiteBuilder()
            .setDomain("read.xiaoshuo1-sm.com")
            .build();
    private static final String SIGN_KEY = "37e81a9d8f02596e1b895d07c171d5c9";

    private PageRule rule = PageRule.build()
            .add("do=is_rank_list", this::processRank)
            .add("do=sp_get", this::processCommentAndScore);

    private void processCommentAndScore(Page page) {
        JSONObject jsonObject = JSONObject.parseObject(page.getRawText());
        String status = jsonObject.getString("status");
        if (!StringUtils.equals("200", status)) return;

        Date day = DateUtil.getDayStartTime(new Date());
        String bookId = UrlUtils.getParam(page.getUrl().get(), "bookId");

        JSONObject info = jsonObject.getJSONObject("info");
        Integer comment = info.getInteger("total");


        JSONObject scoreInfo = info.getJSONObject("scoreInfo");
        String scoreStr = scoreInfo.getString("score");
        String peopleStr = scoreInfo.getString("people");
        Float score = 0F;
        Integer people = 0;
        //可能为其他字符
        try {
            people = Integer.parseInt(peopleStr);
            score = Float.parseFloat(scoreStr);
        } catch (NumberFormatException e) {
        }
        FictionPlatformRate rate = new FictionPlatformRate();
        rate.setCode(bookId);
        rate.setPlatformId(CommonEnum.Platform.SQ.getCode());
        rate.setRate(score);
        rate.setUserCount(people);

        FictionCommentLogs commentLogs = new FictionCommentLogs();
        commentLogs.setCode(bookId);
        commentLogs.setCommentCount(comment);
        commentLogs.setPlatformId(CommonEnum.Platform.SQ.getCode());

        putModel(page, rate);
        putModel(page, commentLogs);
    }

    private void processRank(Page page) {
        JSONObject jsonObject = JSONObject.parseObject(page.getRawText());
        Integer status = jsonObject.getInteger("status");
        if (1 != status) return;

        JSONObject info = jsonObject.getJSONObject("info");
        JSONArray data = jsonObject.getJSONArray("data");


        Integer pageNum = info.getInteger("page");
        Integer size = info.getInteger("size");
        Integer beforeSize = (pageNum - 1) * size;
        String type_name = info.getString("type_name");

        Date day = DateUtil.getDayStartTime(new Date());

        Date billboardUpdateTime = null;

        String month = UrlUtils.getParam(page.getUrl().get(), "month");
        if (StringUtils.isNotEmpty(month)) {
            try {
                billboardUpdateTime = DateUtils.parseDate(month + "-01", "yyyy-MM-dd");
            } catch (ParseException e) {
            }
        }


        String url = page.getUrl().get();
        String sex = UrlUtils.getParam(url, "interest");
        try {
            sex = URLDecoder.decode(sex, "utf-8");
        } catch (UnsupportedEncodingException e) {
        }
        type_name = type_name + "_" + sex;
        List<Fiction> fictions = new ArrayList<>();
        List<FictionOriginalBillboard> billboardList = new ArrayList<>();


        String userId = "138" + RandomUtils.nextInt(5019265, 9999999);
        String signTime = String.valueOf(new Date().getTime());


        for (int i = 0; i < data.size(); i++) {
            JSONObject book = data.getJSONObject(i);

            String name = book.getString("title");
            String author = book.getString("author");
            //1完结、0连载
            Integer isFinished = book.getInteger("status");

            String bid = book.getString("bid");
            Integer words = book.getInteger("words");
            String cover = book.getString("cover");
            String desc = book.getString("desc");
            String tags = book.getString("tags");
            tags = StringUtils.replaceAll(tags, ",", "/");

            String authorid = book.getString("authorid");

            Fiction fiction = new Fiction();
            fiction.setAuthor(author);
            fiction.setChannel(StringUtils.equals("男生", sex) ? CommonEnum.FictionChannel.BOY.getCode() : CommonEnum.FictionChannel.GIRL.getCode());
            fiction.setIsFinish(isFinished);
            fiction.setName(name);
            fiction.setPlatformId(CommonEnum.Platform.SQ.getCode());
            fiction.setCode(bid);
            fiction.setTotalLength(words);
            fiction.setCover(cover);
            fiction.setIntro(desc);
            fiction.setTags(tags);
            fictions.add(fiction);

            FictionOriginalBillboard billboard = new FictionOriginalBillboard();
            billboard.setPlatformId(CommonEnum.Platform.SQ.getCode());
            billboard.setType(type_name);
            billboard.setDay(day);
            billboard.setRank(beforeSize + i + 1);
            billboard.setCode(bid);
            billboard.setBillboardUpdateTime(billboardUpdateTime);
            billboardList.add(billboard);

            //详情页任务
            //1.作者id authorid 2. 网文id bid 3. 随机数（用户名）4.作者名称 author 5.网文名称 name
            //user_id=1385019265

            try {
                String commentUrl = String.format(COMMENT_SCORE_URL, authorid, bid, userId, URLEncoder.encode(author, "utf-8"), URLEncoder.encode(name, "utf-8"));
                createJob(page, commentUrl, bid);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            //详情页任务2
            //bookId、user_id、timestamp、sign
            String sign = bid + signTime + userId + SIGN_KEY;
            sign = DigestUtils.md5Hex(sign);
            String detailUrl = String.format(CLICK_URL, bid, userId, signTime, sign);
            createJob(page, detailUrl, bid);

        }
        putModel(page, billboardList);
        putModel(page, fictions);
    }

    private void createJob(Page page, String url, String bid) {
        Job oldJob = ((DelayRequest) page.getRequest()).getJob();
        Job job = DbEntityHelper.derive(oldJob, new Job(url));
        job.setCode(bid);
        job.setPlatformId(CommonEnum.Platform.SQ.getCode());
        putModel(page, job);
    }


    @Override
    public Site getSite() {
        // TODO Auto-generated method stub
        return site;
    }

    @Override
    public PageRule getPageRule() {
        // TODO Auto-generated method stub
        return rule;
    }

}
