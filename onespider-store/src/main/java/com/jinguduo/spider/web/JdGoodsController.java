package com.jinguduo.spider.web;

import com.jinguduo.spider.data.table.JdGoods;
import com.jinguduo.spider.service.JdGoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by lc on 2019/10/31
 */
@RestController
@RequestMapping("/jdGoods")
public class JdGoodsController {
    @Autowired
    JdGoodsService jdGoodsService;


    @PostMapping
    public JdGoods insertOrUpdate(@RequestBody JdGoods goods) {
        return jdGoodsService.save(goods);

    }
}
