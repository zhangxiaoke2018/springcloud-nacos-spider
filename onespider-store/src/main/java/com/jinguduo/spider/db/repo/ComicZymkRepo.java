package com.jinguduo.spider.db.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jinguduo.spider.data.table.ComicZymk;

import java.util.Date;

/**
 * I'm a cute code dog
 * User: LingChen
 * Date:2017/10/20
 * Time:14:15
 */
public interface ComicZymkRepo extends JpaRepository<ComicZymk, Integer> {
    ComicZymk findByCodeAndDay(String code, Date day);
}
