package com.jinguduo.spider.db.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jinguduo.spider.data.table.BaiduIndexLogs;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 05/12/2016 7:33 PM
 */
@Repository
public interface BaiduIndexLogsRepo extends JpaRepository<BaiduIndexLogs,Long> {

}
