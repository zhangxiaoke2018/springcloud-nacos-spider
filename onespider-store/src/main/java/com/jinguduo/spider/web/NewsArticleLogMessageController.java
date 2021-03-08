package com.jinguduo.spider.web;

import com.jinguduo.spider.data.table.NewsArticleMessageLogs;
import com.jinguduo.spider.service.NewsArticleMessageLogService;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by gaozl on 2020/10/14.
 */
@RestController
@CommonsLog
public class NewsArticleLogMessageController {

    @Autowired
    NewsArticleMessageLogService newsArticleMessageLogService;
    @RequestMapping("/insert_news_article_message")
    public String addNewsArticles(@RequestBody NewsArticleMessageLogs newsArticleMessageLog){
        try {

            newsArticleMessageLogService.save(newsArticleMessageLog);
            return "ok";
        }
        catch (Exception e){
            //log.error("insert news articles error :"+newsArticleLog.toString()+"\n"+e.getMessage());
            return "error:"+e.getMessage();
        }

    }
}
