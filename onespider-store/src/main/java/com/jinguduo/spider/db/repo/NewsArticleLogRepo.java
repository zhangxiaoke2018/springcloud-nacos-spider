package com.jinguduo.spider.db.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jinguduo.spider.data.table.NewsArticleLog;

import java.util.Optional;

/**
 * Created by csonezp on 2017/3/8.
 */
public interface NewsArticleLogRepo extends JpaRepository<NewsArticleLog,Long> {
    Optional<NewsArticleLog> findByCodeAndUrl(String code,String url);
    Optional<NewsArticleLog> findByCodeAndTitle(String code ,String title);

}
