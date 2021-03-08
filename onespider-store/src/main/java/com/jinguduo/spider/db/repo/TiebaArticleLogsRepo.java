package com.jinguduo.spider.db.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jinguduo.spider.data.table.TiebaArticleLogs;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 21/04/2017 10:20
 */
public interface TiebaArticleLogsRepo extends JpaRepository<TiebaArticleLogs, Integer> {

    TiebaArticleLogs findByCodeAndUrl(String code, String url);

}
