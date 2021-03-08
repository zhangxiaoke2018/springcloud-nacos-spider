package com.jinguduo.spider.service;

import com.jinguduo.spider.data.table.bookProject.WechatBookLogs;
import com.jinguduo.spider.db.repo.WechatBookLogsRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by lc on 2020/1/17
 */
@Service
public class WechatBookLogsService {

    @Autowired
    private WechatBookLogsRepo repo;

    public WechatBookLogs saveOrUpdate(WechatBookLogs logs) {

        WechatBookLogs old = repo.findByBookCodeAndPlatformIdAndArticleCode(logs.getBookCode(), logs.getPlatformId(),logs.getArticleCode());

        if (null == old) {
            return repo.save(logs);
        }
        return old;

    }

}
