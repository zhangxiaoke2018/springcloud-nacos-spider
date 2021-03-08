package com.jinguduo.spider.db.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jinguduo.spider.data.table.TiebaLog;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 31/03/2017 17:16
 */
@Repository
public interface TiebaLogRepo extends JpaRepository<TiebaLog, Integer> {

}
