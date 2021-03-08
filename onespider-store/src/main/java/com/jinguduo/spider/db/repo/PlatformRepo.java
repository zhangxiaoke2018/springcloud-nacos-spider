package com.jinguduo.spider.db.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.jinguduo.spider.data.table.Platform;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 16/6/20 下午3:00
 */
@Repository
public interface PlatformRepo extends JpaRepository<Platform,Integer> {
}
