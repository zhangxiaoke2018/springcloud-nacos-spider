package com.jinguduo.spider.web;

import com.jinguduo.spider.data.table.Comic163;
import com.jinguduo.spider.service.Comic163Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @DATE 04/08/2017 14:05
 */
@RestController
@RequestMapping("/comic_163")
public class Comic163Controller {

    @Autowired
    private Comic163Service comic163Service;

    @PostMapping
    public Comic163 save(@RequestBody Comic163 comic163){

        return comic163Service.save(comic163);

    }

}
