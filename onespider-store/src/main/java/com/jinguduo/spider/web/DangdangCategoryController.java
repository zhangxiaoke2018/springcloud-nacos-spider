package com.jinguduo.spider.web;

import com.jinguduo.spider.data.table.DangdangCategory;
import com.jinguduo.spider.service.DangdangCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by lc on 2019/12/4
 */
@RestController
@RequestMapping("/dangdang_category")
public class DangdangCategoryController {

    @Autowired
    private DangdangCategoryService service;
    @PostMapping
    public DangdangCategory save(@RequestBody DangdangCategory dc){
        return service.saveOrUpdate(dc);

    }
}
