package com.jinguduo.spider.db.repo;

import com.jinguduo.spider.data.table.BilibiliFansCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @DATE 27/07/2017 13:44
 */
@Repository
public interface BilibiliFansRepo extends JpaRepository<BilibiliFansCount, Integer> {
}
