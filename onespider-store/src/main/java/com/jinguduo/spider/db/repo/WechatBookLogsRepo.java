package com.jinguduo.spider.db.repo;

import com.jinguduo.spider.data.table.bookProject.WechatBookLogs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by lc on 2020/3/9
 */
@Repository
public interface WechatBookLogsRepo extends JpaRepository<WechatBookLogs, Integer> {

    WechatBookLogs findByBookCodeAndPlatformIdAndArticleCode(String bookCode,Integer platformId,String articleCode);
}
