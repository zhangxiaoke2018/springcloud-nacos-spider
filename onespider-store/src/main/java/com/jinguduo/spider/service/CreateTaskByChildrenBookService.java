package com.jinguduo.spider.service;

import com.google.common.collect.Lists;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.common.constant.FrequencyConstant;
import com.jinguduo.spider.common.constant.JobKind;
import com.jinguduo.spider.common.util.DateUtil;
import com.jinguduo.spider.data.table.bookProject.ChildrenBook;
import com.jinguduo.spider.data.table.bookProject.DoubanBook;
import com.jinguduo.spider.data.table.bookProject.JingdongBook;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;

/**
 * Created by lc on 2017/6/15.
 */
@SuppressWarnings("all")
@Service
@Slf4j
public class CreateTaskByChildrenBookService {

    @Autowired
    ChildrenBookService childrenBookService;
    @Autowired
    DoubanBookService doubanBookService;
    @Autowired
    JingdongBookService jingdongBookService;

    private static final String DOUBAN_BOOK_SEARCH_INIT_URL = "https://search.douban.com/book/subject_search?search_text=%s&cat=1001";
    private static final String JIANSHU_BOOK_SEARCH_INIT_URL = "https://www.jianshu.com/search/do?q=%s&type=note&page=1&order_by=default";

    //微博搜索手机端前缀
    private static final String WEIBO_SEARCH_MOBILE_URL_PREFIX = "https://m.weibo.cn/api/container/getIndex?containerid=100103";
    //微博搜索手机端后缀
    private static final String WEIBO_SEARCH_MOBILE_URL_SUFFIX = "type=%s&q=%s";

    private static final String SOUGOUWECHAT_SEARCH_CHILDREN_BOOK_URL = "https://weixin.sogou.com/weixin?type=2&query=%s&ie=utf8&s_from=input&_sug_=n&_sug_type_=1#children_book";

    private static final String JINGDONG_COMMENT_URL = "https://club.jd.com/comment/skuProductPageComments.action?callback=&productId=%s&score=0&sortType=5&page=0";


    public List<Job> children2doubanJob() {
        //所有的没有豆瓣链接的童书 0.code -> 1.platformId -> 2.isbn
        List<Object[]> allBook = childrenBookService.findAllNewIsbn();

        //创建需要发送的任务list
        List<Job> jobs = Lists.newArrayList();
        //遍历别名信息
        for (Object[] book : allBook) {
            String code = (String) book[0];
            Integer platformId = (Integer) book[1];
            String isbn = (String) book[2];

            //isbn标准长度为13
            if (StringUtils.isEmpty(isbn) || isbn.length() < 13) continue;

            String url = String.format(DOUBAN_BOOK_SEARCH_INIT_URL, isbn);
            Job jobModel = new Job();
            jobModel.setCode(code);
            jobModel.setParentCode(isbn);
            jobModel.setPlatformId(platformId);
            jobModel.setUrl(url);
            jobModel.setKind(JobKind.Forever);
            jobModel.setFrequency(FrequencyConstant.BLANK);
            jobs.add(jobModel);
        }
        return jobs;
    }


    public List<Job> children2jianshuJob() {
        //所有豆瓣童书
        List<ChildrenBook> allBook = childrenBookService.findAll();

        //创建需要发送的任务list
        List<Job> jobs = Lists.newArrayList();
        //遍历别名信息
        for (ChildrenBook book : allBook) {
            String simpleName = book.getSimpleName();
            if (StringUtils.isEmpty(simpleName)) continue;

            try {
                simpleName = URLEncoder.encode(simpleName, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                continue;
            }

            String url = String.format(JIANSHU_BOOK_SEARCH_INIT_URL, simpleName);
            Job jobModel = new Job();
            jobModel.setCode(book.getIsbn());
            jobModel.setUrl(url);
            jobModel.setMethod("POST");
            jobModel.setKind(JobKind.Forever);
            jobModel.setFrequency(FrequencyConstant.BLANK);
            jobs.add(jobModel);
        }
        return jobs;

    }

    public List<Job> douban2doubanJob() {
        //查询3天没有更新的豆瓣
        Date queryDate = DateUtils.addDays(DateUtil.getDayStartTime(new Date()), -3);
        List<DoubanBook> list = doubanBookService.findNotUpdateByDay(queryDate);
        //创建需要发送的任务list
        List<Job> jobs = Lists.newArrayList();
        for (DoubanBook book : list) {
            Job jobModel = new Job();
            jobModel.setCode(book.getCode());
            jobModel.setUrl(book.getUrl());
            jobModel.setPlatformId(book.getPlatformId());
            jobModel.setKind(JobKind.Forever);
            jobModel.setFrequency(FrequencyConstant.BLANK);
            jobs.add(jobModel);
        }
        return jobs;
    }


    public List<Job> children2weiboJob() {
        //所有的没有豆瓣链接的童书
        //0.code,1.platform_id,2.simple_name
        List<Object[]> allBook = childrenBookService.findRankLess150BookInfo();
        //创建需要发送的任务list
        List<Job> jobs = Lists.newArrayList();
        for (Object[] book : allBook) {
            String simpleName = (String) book[2];
            simpleName = this.getQueryNameFromString(simpleName, "+");

            String weiboMobileUrl = WEIBO_SEARCH_MOBILE_URL_PREFIX + URLEncoder.encode(String.format(WEIBO_SEARCH_MOBILE_URL_SUFFIX, "60", simpleName)) + "#children_book";
            Job jobModel = new Job();
            jobModel.setCode(String.valueOf(book[0]));
            jobModel.setPlatformId((Integer) book[1]);
            jobModel.setUrl(weiboMobileUrl);
            jobModel.setKind(JobKind.Forever);
            jobModel.setFrequency(FrequencyConstant.BLANK);
            jobs.add(jobModel);

            String weiboMobileUrl2 = WEIBO_SEARCH_MOBILE_URL_PREFIX + URLEncoder.encode(String.format(WEIBO_SEARCH_MOBILE_URL_SUFFIX, "1", simpleName)) + "#children_book";
            Job job2 = new Job();
            job2.setCode(String.valueOf(book[0]));
            job2.setPlatformId((Integer) book[1]);
            job2.setUrl(weiboMobileUrl2);
            job2.setKind(JobKind.Forever);
            job2.setFrequency(FrequencyConstant.BLANK);
            jobs.add(job2);
        }
        return jobs;
    }


    private String getQueryNameFromString(String initString, String delimiter) {
        initString = initString.trim();
        //注意！！！！！有些api | 需转义！使用 | 必须测试！
        initString = StringUtils.replaceAll(initString, "\\|", delimiter);
        if (!initString.contains("书") && !initString.contains("绘本")) {
            initString = initString + delimiter + "书";
        }
        return initString;
    }

    public List<Job> children2wechatJob() {
        List<Object[]> allBook = childrenBookService.findRankLess150BookInfo();
        List<Job> jobs = Lists.newArrayList();


        for (Object[] book : allBook) {
            String simpleName = (String) book[2];
            simpleName = this.getQueryNameFromString(simpleName, "+");
            String encode = URLEncoder.encode(simpleName);
            String jobUrl = String.format(SOUGOUWECHAT_SEARCH_CHILDREN_BOOK_URL, encode);
            Job jobModel = new Job();
            jobModel.setCode(String.valueOf(book[0]));
            jobModel.setPlatformId((Integer) book[1]);
            jobModel.setUrl(jobUrl);
            jobModel.setKind(JobKind.Forever);
            jobModel.setFrequency(FrequencyConstant.BLANK);
            jobs.add(jobModel);
        }
        return jobs;
    }

    public List<Job> children2jingdongJob() {
         Integer JD_PLATFORM_ID = 58;
        List<JingdongBook> books = jingdongBookService.findAll();
        List<Job> jobs = Lists.newArrayList();

        for (JingdongBook book : books) {
            if (StringUtils.isEmpty(book.getCode())|| StringUtils.isEmpty(book.getGoodsId())) continue;

            String commentUrl = String.format(JINGDONG_COMMENT_URL, book.getGoodsId());

            Job jobModel = new Job();
            //code 保存为goodsId,方便关联书籍
            jobModel.setCode(book.getGoodsId());
            jobModel.setPlatformId(JD_PLATFORM_ID);
            jobModel.setUrl(commentUrl);
            jobModel.setKind(JobKind.Forever);
            jobModel.setFrequency(FrequencyConstant.BLANK);
            jobs.add(jobModel);

        }
        return jobs;
    }
}
