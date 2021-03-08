package com.jinguduo.spider.service;

import com.jinguduo.spider.data.table.Comic163;
import com.jinguduo.spider.db.repo.Comic163Repo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.jinguduo.spider.service.ComicService.skipByCode;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @DATE 04/08/2017 14:04
 */
@Service
public class Comic163Service {

    @Autowired
    private Comic163Repo comic163Repo;

    public Comic163 save(Comic163 comic163){

        boolean isSkip = skipByCode(comic163.getCode());
        if (isSkip) {
            return comic163;
        }
        return comic163Repo.save(comic163);

    }

}
