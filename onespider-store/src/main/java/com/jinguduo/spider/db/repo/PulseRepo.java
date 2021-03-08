package com.jinguduo.spider.db.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jinguduo.spider.data.table.Pulse;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @DATE 01/08/2017 10:21
 */
@Repository
public interface PulseRepo extends JpaRepository<Pulse, Integer> {



}
