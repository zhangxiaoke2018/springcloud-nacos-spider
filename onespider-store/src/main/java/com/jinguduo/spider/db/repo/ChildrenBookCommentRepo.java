package com.jinguduo.spider.db.repo;

import com.jinguduo.spider.data.table.bookProject.ChildrenBookComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;

/**
 * Created by lc on 2019/12/4
 */
@Repository
public interface ChildrenBookCommentRepo extends JpaRepository<ChildrenBookComment, Integer> {

    ChildrenBookComment findByPlatformIdAndCodeAndDay(Integer platformId, String code, Date day);
}
