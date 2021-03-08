package com.jinguduo.spider.web;

import com.jinguduo.spider.data.table.CartoonBulletin;
import com.jinguduo.spider.service.CartoonBulletinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by lc on 2020/4/9
 */
@RestController
@RequestMapping("/cartoon_bulletin")
public class CartoonBulletinController {
    @Autowired
    private CartoonBulletinService service;


    @PostMapping
    public CartoonBulletin save(@RequestBody CartoonBulletin cb) {
        return service.saveOrUpdate(cb);

    }

}
