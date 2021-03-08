package com.jinguduo.spider.service;

import com.jinguduo.spider.data.table.ShowPopularLogs;
import com.jinguduo.spider.db.repo.ShowPopularLogsRepo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by lc on 2018/9/3
 */
@Service
@Slf4j
public class ShowPopularLogsService {

    @Autowired
    ShowPopularLogsRepo repo;

    public ShowPopularLogs save(ShowPopularLogs logs) {
        if (null == logs || StringUtils.isEmpty(logs.getCode())||null ==logs.getHotCount()) return null;
        return repo.save(logs);
    }
}
