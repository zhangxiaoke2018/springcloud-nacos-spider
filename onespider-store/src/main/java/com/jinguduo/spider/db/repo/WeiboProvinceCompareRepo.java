package com.jinguduo.spider.db.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jinguduo.spider.data.table.WeiboProvinceCompare;

import java.util.List;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 19/04/2017 18:30
 */
@Repository
public interface WeiboProvinceCompareRepo extends JpaRepository<WeiboProvinceCompare, Integer> {

    List<WeiboProvinceCompare> findByKeyword(String keyword);

    List<WeiboProvinceCompare> findByCode(String code);
}
