package com.jinguduo.spider.service;

import com.jinguduo.spider.data.table.ScreenshotLogs;
import com.jinguduo.spider.db.repo.ScreenshotLogsRepo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 19/05/2017 17:21
 */
@Service
public class ScreenshotLogsService {

    @Autowired
    private ScreenshotLogsRepo screenshotLogsRepo;


    public void insertOrUpdate(ScreenshotLogs screenshotLogs) {

        ScreenshotLogs sl = screenshotLogsRepo.findByUuid(screenshotLogs.getUuid());
        if(sl == null && StringUtils.isNotBlank(screenshotLogs.getCode())){// worker第一次创建
            screenshotLogsRepo.save(screenshotLogs);
            return;
        }else if(sl == null) {
            return;
        }else {
            sl.setContent(screenshotLogs.getContent());
            screenshotLogsRepo.save(sl);
        }
    }
}
