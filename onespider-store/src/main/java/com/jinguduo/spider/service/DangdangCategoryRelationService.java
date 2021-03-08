package com.jinguduo.spider.service;

import com.jinguduo.spider.data.table.DangdangCategoryRelation;
import com.jinguduo.spider.db.repo.DangdangCategoryRelationRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by lc on 2019/12/4
 */
@Service
public class DangdangCategoryRelationService {
    @Autowired
    private DangdangCategoryRelationRepo repo;

    public DangdangCategoryRelation saveOrUpdate(DangdangCategoryRelation dcr) {

        DangdangCategoryRelation old = repo.findByCategoryIdAndBookCode(dcr.getCategoryId(), dcr.getBookCode());

        if (null == old){
            return repo.save(dcr);
        }
        return dcr;
    }
}
