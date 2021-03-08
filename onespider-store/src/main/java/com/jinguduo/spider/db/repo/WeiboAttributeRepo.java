package com.jinguduo.spider.db.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jinguduo.spider.data.table.WeiboAttribute;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 19/04/2017 18:29
 */
@Repository
public interface WeiboAttributeRepo extends JpaRepository<WeiboAttribute, Integer> {

    WeiboAttribute findByKeyword(String keyword);

    WeiboAttribute findByCode(String code);
}
