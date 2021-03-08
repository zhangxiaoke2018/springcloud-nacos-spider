package com.jinguduo.spider.web;

import com.jinguduo.spider.data.table.StockCompany;
import com.jinguduo.spider.service.StockCompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Created by lc on 2019/4/9
 */
@RestController
@RequestMapping("stockCompany")
public class StockCompanyController {

    @Autowired
    StockCompanyService service;

    @GetMapping("/all")
    public List<StockCompany> all() {
        return service.findAll();
    }
}
