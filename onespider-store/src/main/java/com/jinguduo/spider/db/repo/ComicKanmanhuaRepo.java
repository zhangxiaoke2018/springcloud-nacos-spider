package com.jinguduo.spider.db.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jinguduo.spider.data.table.ComicKanmanhua;

import java.util.Date;

/**
 * I'm a cute code dog
 * User: LingChen
 * Date:2017/11/4
 * Time:18:23
 */
public interface ComicKanmanhuaRepo extends JpaRepository<ComicKanmanhua, Integer> {

    ComicKanmanhua findByCodeAndDay(String code, Date day);
}
