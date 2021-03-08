package com.jinguduo.spider.service;

import com.jinguduo.spider.data.table.StockCompany;
import com.jinguduo.spider.db.repo.StockCompanyRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by lc on 2019/4/9
 */
@Service
public class StockCompanyService {

    @Autowired
    private StockCompanyRepo repo;


    public List<StockCompany> findAll() {
        return repo.findAll();

    }
}
