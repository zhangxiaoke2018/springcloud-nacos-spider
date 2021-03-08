package com.jinguduo.spider.db.repo;

import com.jinguduo.spider.data.table.DangdangCategoryRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by lc on 2019/12/4
 */
@Repository
public interface DangdangCategoryRelationRepo extends JpaRepository<DangdangCategoryRelation, Integer> {

    DangdangCategoryRelation findByCategoryIdAndBookCode(Integer categoryId,String bookCode);
}
