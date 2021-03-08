package com.jinguduo.spider.db.repo;

import com.jinguduo.spider.data.table.bookProject.DoubanBook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * Created by lc on 2020/1/15
 */
@Repository
public interface DoubanBookRepo extends JpaRepository<DoubanBook, Integer> {
    DoubanBook findByCodeAndPlatformIdAndUrl(String code, Integer platformId,String url);

    @Query(value = "SELECT * FROM douban_book WHERE updated_at < :day OR book_name is NULL", nativeQuery = true)
    List<DoubanBook> findNotUpdateByDay(@Param("day") Date day);
}
