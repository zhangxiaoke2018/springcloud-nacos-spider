package com.jinguduo.spider.db.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jinguduo.spider.data.table.WechatArticleAdKeywords;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 06/06/2017 09:49
 */
@Repository
public interface WechatArticleAdKeywordsRepo extends JpaRepository<WechatArticleAdKeywords, Integer> {

    WechatArticleAdKeywords findByKeyword(String keyword);


}
