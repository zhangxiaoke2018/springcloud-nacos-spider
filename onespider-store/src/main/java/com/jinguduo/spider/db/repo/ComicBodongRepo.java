package com.jinguduo.spider.db.repo;

import com.jinguduo.spider.data.table.ComicBodong;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;

/**
 * Created by lc on 2018/9/10
 */
@Repository
public interface ComicBodongRepo extends JpaRepository<ComicBodong, Integer> {

    ComicBodong findByCodeAndDay(String code, Date day);
}
