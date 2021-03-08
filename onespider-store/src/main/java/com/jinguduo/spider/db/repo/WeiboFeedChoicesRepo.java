package com.jinguduo.spider.db.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jinguduo.spider.data.table.WeiboFeedChoices;

import java.sql.Timestamp;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 23/03/2017 17:51
 */
@Repository
public interface WeiboFeedChoicesRepo extends JpaRepository<WeiboFeedChoices, Integer> {

    WeiboFeedChoices findByKeywordAndPostTimeAndNickName(String keyword, Timestamp postTime, String nickName);

}
