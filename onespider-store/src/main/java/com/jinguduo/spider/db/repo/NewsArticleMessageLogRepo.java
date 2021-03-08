package com.jinguduo.spider.db.repo;

import com.jinguduo.spider.data.table.NewsArticleMessageLogs;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Created by gaozl on 2020/10/14.
 */
public interface NewsArticleMessageLogRepo extends JpaRepository<NewsArticleMessageLogs,Long> {
    Optional<NewsArticleMessageLogs> findByCodeAndUrl(String code, String url);
    Optional<NewsArticleMessageLogs> findByCodeAndTitle(String code , String title);


}
