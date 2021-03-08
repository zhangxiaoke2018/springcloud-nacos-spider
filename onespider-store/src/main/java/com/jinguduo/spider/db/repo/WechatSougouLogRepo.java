package com.jinguduo.spider.db.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jinguduo.spider.data.table.WechatSougouLog;

/**
 * @DATE 01/03/2018 15:04
 */
@Repository
public interface WechatSougouLogRepo extends JpaRepository<WechatSougouLog, Integer> {






}
