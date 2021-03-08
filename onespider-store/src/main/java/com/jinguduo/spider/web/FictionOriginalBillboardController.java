package com.jinguduo.spider.web;

import com.jinguduo.spider.data.table.FictionOriginalBillboard;
import com.jinguduo.spider.service.FictionOriginalBillboardService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by lc on 2019/6/27
 */
@RestController
@RequestMapping("/fiction_original_billboard")
@Slf4j
public class FictionOriginalBillboardController {

    @Autowired
    FictionOriginalBillboardService service;


    @PostMapping()
    public FictionOriginalBillboard save(@RequestBody FictionOriginalBillboard billboard) {
        return service.saveOrUpdate(billboard);
    }

}
