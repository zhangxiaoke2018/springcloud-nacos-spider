package com.jinguduo.spider.db.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jinguduo.spider.data.table.WeiboFeedKeywordTag;

import java.io.Serializable;
import java.util.Date;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @DATE 08/08/2017 10:56
 */
@Repository
public interface WeiboFeedKeywordTagRepo extends JpaRepository<WeiboFeedKeywordTag, Integer> {

    @Query(value = "select * from `weibo_feed_keyword_tags` wfk where wfk.`keyword` = :keyword and wfk.`tag` = :tag and wfk.`day` = :day ;", nativeQuery = true)
    WeiboFeedKeywordTag findByKeywordAndTagAndDay(@Param("keyword") String keyword, @Param("tag") String tag, @Param("day") String day);

}
