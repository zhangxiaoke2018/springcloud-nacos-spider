package com.jinguduo.spider.db.repo;

import com.jinguduo.spider.data.table.ComicWeibo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;

@Repository
public interface ComicWeiboRepo extends JpaRepository<ComicWeibo, Integer> {
    ComicWeibo findByCodeAndDay(String code, Date day);
}
