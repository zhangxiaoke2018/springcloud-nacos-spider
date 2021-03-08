package com.jinguduo.spider.db.repo;

import com.jinguduo.spider.data.table.bookProject.ChildrenBookCommentText;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by lc on 2020/5/12
 */
@Repository
public interface ChildrenBookCommentTextRepo extends JpaRepository<ChildrenBookCommentText, Integer> {

    ChildrenBookCommentText findByPlatformIdAndCode(Integer platformId, String code);
}
