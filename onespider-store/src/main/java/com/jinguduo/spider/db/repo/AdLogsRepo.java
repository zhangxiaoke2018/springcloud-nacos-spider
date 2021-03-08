package com.jinguduo.spider.db.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jinguduo.spider.data.table.AdLogs;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @DATE 06/12/2017 10:12
 */
@Deprecated
public interface AdLogsRepo extends JpaRepository<AdLogs, Long> {
}
