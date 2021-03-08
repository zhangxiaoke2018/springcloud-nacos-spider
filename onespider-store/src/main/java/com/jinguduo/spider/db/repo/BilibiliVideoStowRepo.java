package com.jinguduo.spider.db.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jinguduo.spider.data.table.BilibiliVideoStow;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @DATE 27/07/2017 13:47
 */
@Repository
public interface BilibiliVideoStowRepo extends JpaRepository<BilibiliVideoStow, Integer> {
}
