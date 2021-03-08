package com.jinguduo.spider.db.repo;

import java.sql.Date;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

import com.jinguduo.spider.data.table.WeiboFeedKeywordLog;

@Component
public interface WeiboFeedKeywordLogRepo extends CrudRepository<WeiboFeedKeywordLog, Long> {

    WeiboFeedKeywordLog findFirstByKeywordAndDayOrderById(String keyword, Date day);

    WeiboFeedKeywordLog findFirstByTypeAndRelevanceIdAndDayOrderById(Integer type, Integer relevanceId, Date day);

}
