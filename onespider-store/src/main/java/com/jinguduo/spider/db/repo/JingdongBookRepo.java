package com.jinguduo.spider.db.repo;

import com.jinguduo.spider.data.table.bookProject.JingdongBook;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by lc on 2020/4/2
 */
public interface JingdongBookRepo extends JpaRepository<JingdongBook, Integer> {
}
