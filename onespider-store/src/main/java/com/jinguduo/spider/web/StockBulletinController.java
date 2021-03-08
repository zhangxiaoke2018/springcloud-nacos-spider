package com.jinguduo.spider.web;

import com.jinguduo.spider.data.table.StockBulletin;
import com.jinguduo.spider.service.StockBulletinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by lc on 2019/4/9
 */

@RestController
@RequestMapping("/stock_bulletin")
public class StockBulletinController {

    @Autowired
    StockBulletinService service;

    @PostMapping
    public StockBulletin save(@RequestBody StockBulletin sb){

        return service.save(sb);

    }


}
