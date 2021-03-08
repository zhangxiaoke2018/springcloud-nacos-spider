package com.jinguduo.spider.db.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jinguduo.spider.data.table.ComicTengxun;
import com.jinguduo.spider.data.table.ComicTengxunComment;

import java.util.Date;

/**
 * I'm a cute code dog
 * User: LingChen
 * Date:2017/8/2
 * Time:19:26
 */
public interface ComicTengxunCommentRepo extends JpaRepository<ComicTengxunComment, Integer> {
}
