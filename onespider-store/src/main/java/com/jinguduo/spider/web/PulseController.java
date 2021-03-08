package com.jinguduo.spider.web;

import com.jinguduo.spider.data.table.Pulse;
import com.jinguduo.spider.service.PulseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @DATE 01/08/2017 10:22
 */
@RestController
@RequestMapping("/pulse")
public class PulseController {

    @Autowired
    private PulseService pulseService;

    @GetMapping("/all")
    public List<Pulse> all(){
        return pulseService.findAll();
    }


}
