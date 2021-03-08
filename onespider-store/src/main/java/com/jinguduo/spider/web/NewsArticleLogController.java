package com.jinguduo.spider.web;

import com.jinguduo.spider.data.table.NewsArticleLog;
import com.jinguduo.spider.service.NewsArticleLogService;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by csonezp on 2017/3/10.
 */
@RestController
@CommonsLog
public class NewsArticleLogController {

    @Autowired
    NewsArticleLogService newsArticleLogService;
    @RequestMapping("/insert_news_article")
    public String addNewsArticles(@RequestBody NewsArticleLog newsArticleLog){
        try {

            newsArticleLogService.save(newsArticleLog);
            return "ok";
        }
        catch (Exception e){
            //log.error("insert news articles error :"+newsArticleLog.toString()+"\n"+e.getMessage());
            return "error:"+e.getMessage();
        }

    }
}
