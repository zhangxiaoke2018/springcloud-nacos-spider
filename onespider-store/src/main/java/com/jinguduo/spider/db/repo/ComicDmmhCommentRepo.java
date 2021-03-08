package com.jinguduo.spider.db.repo;

import com.jinguduo.spider.data.table.ComicDmmhComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ComicDmmhCommentRepo extends JpaRepository<ComicDmmhComment, Long> {




}
