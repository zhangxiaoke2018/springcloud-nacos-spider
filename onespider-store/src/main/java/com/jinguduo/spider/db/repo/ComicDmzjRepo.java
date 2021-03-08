package com.jinguduo.spider.db.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jinguduo.spider.data.table.ComicDmzj;
import org.springframework.stereotype.Repository;

import java.util.Date;

/**
 * I'm a cute code dog
 * User: LingChen
 * Date:2017/11/4
 * Time:18:23
 */
@Repository
public interface ComicDmzjRepo extends JpaRepository<ComicDmzj, Integer> {

    ComicDmzj findByCodeAndDay(String code, Date day);
}
