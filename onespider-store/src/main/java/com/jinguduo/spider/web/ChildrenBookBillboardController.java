package com.jinguduo.spider.web;

import com.jinguduo.spider.data.table.bookProject.ChildrenBookBillboard;
import com.jinguduo.spider.service.ChildrenBookBillboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by lc on 2019/12/4
 */
@RestController
@RequestMapping("/children_book_billboard")
public class ChildrenBookBillboardController {
    @Autowired
    private ChildrenBookBillboardService service;

    @PostMapping
    public ChildrenBookBillboard save(@RequestBody ChildrenBookBillboard cbb){
        return service.saveOrUpdate(cbb);

    }

}
