package com.jinguduo.spider.service;

import com.jinguduo.spider.data.table.WeiboOfficialLog;
import com.jinguduo.spider.db.repo.WeiboOfficialLogRepo;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 31/03/2017 17:17
 */
@Service
@CommonsLog
public class WeiboOfficialLogService {

    @Autowired
    private WeiboOfficialLogRepo weiboOfficialLogRepo;

    public WeiboOfficialLog insertOrUpdate(WeiboOfficialLog log) {

        if (log == null || log.getFansCount() == -1) {
            return null;
        }
        if (StringUtils.isBlank(log.getWeiboName())) {
            WeiboOfficialLog oldWeibo = weiboOfficialLogRepo.findTop1ByCodeAndWeiboNameNotNullOrderByIdDesc(log.getCode());
            if (null != oldWeibo) {
                log.setWeiboName(oldWeibo.getWeiboName());
            }
        }
        return weiboOfficialLogRepo.save(log);
    }

}
