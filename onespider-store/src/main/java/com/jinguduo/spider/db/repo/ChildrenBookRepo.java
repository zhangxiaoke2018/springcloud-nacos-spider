package com.jinguduo.spider.db.repo;

import com.jinguduo.spider.data.table.bookProject.ChildrenBook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by lc on 2019/12/4
 */
@Repository
public interface ChildrenBookRepo extends JpaRepository<ChildrenBook, Integer> {

    ChildrenBook findByPlatformIdAndCode(Integer platformId, String code);

    @Query(value = "SELECT `code`,platform_id,isbn FROM children_book WHERE douban_query_status = 0 AND isbn LIKE '9%' ORDER BY isbn DESC;", nativeQuery = true)
    List<Object[]> findIsbnNotExistDouban();

    List<ChildrenBook> findByIsbn(String isbn);

    //只抓榜单前150
//    @Query(value = "SELECT t1.`code`,t1.platform_id,t1.simple_name FROM children_book t1 INNER JOIN\t (\n" +
//            "SELECT DISTINCT `code`,platform_id FROM children_book_billboard WHERE rank <= 150 AND `day`  = date_add(curdate(), interval -1 day)\n" +
//            ") t2 ON t1.`code` = t2.code AND t1.platform_id = t2.platform_id;", nativeQuery = true)

    //只抓简称已经修改完毕的
    @Query(value = "SELECT `code`,platform_id,simple_name FROM children_book WHERE name_status = 1;", nativeQuery = true)
    List<Object[]> findRankLess150BookInfo();


}
