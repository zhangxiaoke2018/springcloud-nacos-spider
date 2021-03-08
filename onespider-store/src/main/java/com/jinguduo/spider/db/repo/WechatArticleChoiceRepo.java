package com.jinguduo.spider.db.repo;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;

import com.jinguduo.spider.data.table.WechatArticleChoice;

import java.sql.Date;
import java.util.List;

@Component
public interface WechatArticleChoiceRepo extends CrudRepository<WechatArticleChoice, Integer> {

    WechatArticleChoice findFirstByKeywordAndUrl(String keyword, String url);
    WechatArticleChoice findFirstByKeywordAndDayAndOrdinal(String keyword,Date day,Integer ordinal);

    WechatArticleChoice findFirstByTypeAndRelevanceIdAndTitleAndDay(Integer type, Integer relevanceId, String title, Date day);

    @Query(value = "select * from wechat_article_choices where day=:day and read_count=:readCount limit 200", nativeQuery = true)
    List<WechatArticleChoice> findByDayAndReadCount(@Param("day") String day, @Param("readCount") Integer readCount);
}
