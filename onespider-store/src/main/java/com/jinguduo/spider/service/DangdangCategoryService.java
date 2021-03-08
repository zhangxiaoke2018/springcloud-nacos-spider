package com.jinguduo.spider.service;

import com.jinguduo.spider.data.table.DangdangCategory;
import com.jinguduo.spider.db.repo.DangdangCategoryRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by lc on 2019/12/4
 */
@Service
public class DangdangCategoryService {

    @Autowired
    private DangdangCategoryRepo repo;

    private static Set<Integer> OLD_CATEGORY_ID_SET = new HashSet<>();

    public DangdangCategory saveOrUpdate(DangdangCategory dc) {
        //几乎没有变更，直接扔内存里，至2019-12-05 store 8G内存空闲量为3.3G，预计占用100个Ingteger 位置
        if (OLD_CATEGORY_ID_SET.contains(dc.getId())) {
            return dc;
        }
        OLD_CATEGORY_ID_SET.add(dc.getId());

        DangdangCategory old = repo.findOne(dc.getId());
        if (null == old) {
            return repo.save(dc);
        }
        return dc;
    }
}
