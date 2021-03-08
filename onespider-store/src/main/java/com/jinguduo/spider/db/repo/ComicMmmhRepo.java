package com.jinguduo.spider.db.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jinguduo.spider.data.table.ComicMmmh;

import java.util.Date;

/**
 * I'm a cute code dog
 * User: LingChen
 * Date:2017/11/4
 * Time:18:23
 */
public interface ComicMmmhRepo extends JpaRepository<ComicMmmh, Integer> {

    ComicMmmh findByCodeAndDay(String code, Date day);
}
