package com.jinguduo.spider.db.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jinguduo.spider.data.table.ScreenshotLogs;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 19/05/2017 17:19
 */
@Repository
public interface ScreenshotLogsRepo extends JpaRepository<ScreenshotLogs, Integer> {

    ScreenshotLogs findByUuid(String uuid);

}
