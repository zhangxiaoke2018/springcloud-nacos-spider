package com.jinguduo.spider.service;


import com.jinguduo.spider.data.table.BoxOfficeLogs;
import com.jinguduo.spider.db.repo.ChinaBoxOfficeRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @DATE 27/07/2017 13:43
 */
@Service
@Slf4j
public class ChinaBoxOfficeService {


    @Autowired
    private ChinaBoxOfficeRepo chinaBoxOfficeRepo;

    public BoxOfficeLogs saveBoxOffice(BoxOfficeLogs boxOffice) {

        return chinaBoxOfficeRepo.save(boxOffice);
    }
}
