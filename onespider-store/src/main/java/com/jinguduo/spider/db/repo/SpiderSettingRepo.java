package com.jinguduo.spider.db.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jinguduo.spider.data.table.SpiderSetting;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 16/6/28 下午7:01
 */
@Repository
public interface SpiderSettingRepo  extends JpaRepository<SpiderSetting, Integer> {

    SpiderSetting findByDomain(String domain);
}
