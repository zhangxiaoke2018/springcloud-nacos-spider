package com.jinguduo.spider.web;

import com.jinguduo.spider.data.table.MaoyanBox;
import com.jinguduo.spider.service.MaoyanBoxService;
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
@RequestMapping("/maoyan_box")
public class MaoyanBoxController {

    @Autowired
    private MaoyanBoxService maoyanBoxService;

    @PostMapping
    public MaoyanBox saveOrUpdate(@RequestBody MaoyanBox box) {

        return maoyanBoxService.insertOrUpdate(box);

    }

}
