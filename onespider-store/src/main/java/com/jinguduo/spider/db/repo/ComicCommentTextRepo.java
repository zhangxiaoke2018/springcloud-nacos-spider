package com.jinguduo.spider.db.repo;

import com.jinguduo.spider.data.table.ComicCommentText;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;

@Repository
public interface ComicCommentTextRepo  extends JpaRepository<ComicCommentText, Integer> {
    ComicCommentText findByCommentId(Long commentId);
}
