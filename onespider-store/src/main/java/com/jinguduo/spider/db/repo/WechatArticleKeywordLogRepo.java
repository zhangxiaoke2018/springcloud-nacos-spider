package com.jinguduo.spider.db.repo;

import java.sql.Date;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

import com.jinguduo.spider.data.table.WechatArticleKeywordLog;

@Component
public interface WechatArticleKeywordLogRepo extends CrudRepository<WechatArticleKeywordLog, Long> {

    WechatArticleKeywordLog findFirstByKeywordAndDayOrderById(String keyword, Date day);

}
