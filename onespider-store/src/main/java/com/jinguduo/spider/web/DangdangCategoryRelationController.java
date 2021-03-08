package com.jinguduo.spider.web;

import com.jinguduo.spider.data.table.DangdangCategoryRelation;
import com.jinguduo.spider.service.DangdangCategoryRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by lc on 2019/12/4
 */
@RestController
@RequestMapping("/dangdang_category_relation")
public class DangdangCategoryRelationController {

    @Autowired
    private DangdangCategoryRelationService service;

    @PostMapping
    public DangdangCategoryRelation save(@RequestBody DangdangCategoryRelation dcr) {
        return service.saveOrUpdate(dcr);

    }
}
