package com.jinguduo.spider.service;

import com.google.common.collect.Lists;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.common.constant.FrequencyConstant;
import com.jinguduo.spider.common.constant.JobKind;
import com.jinguduo.spider.common.util.DateUtil;
import com.jinguduo.spider.data.table.Alias;
import com.jinguduo.spider.data.table.Classify;
import com.jinguduo.spider.data.table.NewsKeyword;
import com.jinguduo.spider.data.table.WechatNewText;
import com.jinguduo.spider.db.repo.CoreKeywordRepo;
import com.jinguduo.spider.db.repo.WechatNewTextRepo;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by gaozl on 2020/10/15.
 */
@SuppressWarnings("all")
@Service
@Slf4j
public class CreateTaskByANewsService {

    @Autowired
    private NewsKeywordService newsKeywordService;


    //百度新闻
    private static final String BAIDU_NEWS_URL = "https://www.baidu.com/s?rtt=1&bsst=1&cl=2&tn=news&word=%s";
    /**
     * 创建任务的基础方法
     *
     * @param aliases 需要的生成任务的别名list
     * @param initUrl 初始url，别名应放在第一位
     * @param params  除了别名之外，需要添加的参数(以此排列在alias之后)
     */
    public List<Job> createTaskBase(List<NewsKeyword> newsKeywords, String initUrl, JobKind jobKind, String... params) {
        //创建需要发送的任务list
        List<Job> jobs = Lists.newArrayList();
        //遍历别名信息
        for (NewsKeyword newsKeyword : newsKeywords) {
            String url = null;
            try {
                List<String> strings = new ArrayList<>();
                strings.add(URLEncoder.encode(newsKeyword.getKeywords().trim()));
                if (null != params && params.length != 0) {
                    for (String s : params) {
                        strings.add(s);
                    }
                }
                url = String.format(initUrl, strings.toArray());
            } catch (Exception e) {
                log.error("createTesk error,this task is -->{}", initUrl, e);
                continue;
            }
            if (StringUtils.isBlank(url)) {
                continue;
            }
            Job jobModel = new Job();
            jobModel.setCode(newsKeyword.getCode());
            jobModel.setUrl(url);
            jobModel.setKind(null == jobKind ? JobKind.Forever : jobKind);
            jobModel.setFrequency(FrequencyConstant.BLANK);
            jobs.add(jobModel);
        }
        return jobs;
    }


    //改版后的百度新闻
    public List<Job> createBaiduTask() {
        List<NewsKeyword> allByClassify = newsKeywordService.getAliasByClassify(Classify.BAIDU_NEWS.name());
        List<Job> jobs = this.createTaskBase(allByClassify, BAIDU_NEWS_URL, JobKind.Forever, null);
        return jobs;
    }


}
