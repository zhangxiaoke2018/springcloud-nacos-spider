package com.jinguduo.spider.db.repo;

import com.jinguduo.spider.data.table.WeiboText;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * @DATE 2018/7/30 14:22
 */
@Repository
public interface WeiboTextRepo extends JpaRepository<WeiboText, String> {
    WeiboText findFirstByMidAndCode(String mid, String code);

    @Query(value = "select id from weibo_texts  where mid = :mid and `code` = :code ;", nativeQuery = true)
    Integer findIdByMidAndCode(@Param("mid") String mid, @Param("code") String code);

}
