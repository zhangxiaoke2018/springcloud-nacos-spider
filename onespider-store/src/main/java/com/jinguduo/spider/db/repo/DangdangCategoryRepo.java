package com.jinguduo.spider.db.repo;

import com.jinguduo.spider.data.table.DangdangCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by lc on 2019/12/4
 */
@Repository
public interface DangdangCategoryRepo extends JpaRepository<DangdangCategory, Integer> {

}
