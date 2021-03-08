package com.jinguduo.spider.db.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import com.jinguduo.spider.data.table.CommentLog;

@Component
public interface CommentLogRepo extends JpaRepository<CommentLog, Integer> {


}
