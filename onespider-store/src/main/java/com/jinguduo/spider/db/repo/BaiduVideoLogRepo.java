package com.jinguduo.spider.db.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jinguduo.spider.data.table.BaiduVideoLog;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 01/04/2017 10:46
 */
@Repository
public interface BaiduVideoLogRepo extends JpaRepository<BaiduVideoLog, Integer> {
}
