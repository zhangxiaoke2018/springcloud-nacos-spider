package com.jinguduo.spider.service;

import com.jinguduo.spider.data.table.Pulse;
import com.jinguduo.spider.db.repo.PulseRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @DATE 01/08/2017 10:26
 */
@Service
public class PulseService {

    @Autowired
    private PulseRepo pulseRepo;


    public List<Pulse> findAll(){
        return pulseRepo.findAll();
    }


}
