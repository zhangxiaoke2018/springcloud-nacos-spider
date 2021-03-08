package com.jinguduo.spider.db.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jinguduo.spider.data.table.DoubanCommentsText;

@Repository
public interface DoubanCommentsTextRepo extends JpaRepository<DoubanCommentsText, Integer> {

    DoubanCommentsText findOneByCommentId(Long commentId);

}
