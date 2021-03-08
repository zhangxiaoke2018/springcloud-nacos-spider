package com.jinguduo.spider.db.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jinguduo.spider.data.table.Comic;

import java.util.List;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @DATE 04/08/2017 18:16
 */
@Repository
public interface ComicRepo extends JpaRepository<Comic, Integer> {

    Comic findByCode(String code);

    List<Comic> findByPlatformId(Integer platformId);

    @Query(value = "SELECT * FROM comic WHERE header_img is NOT null AND inner_img_url is NULL AND platform_id = :platformId",nativeQuery = true)
    List<Comic> findByHeaderImgAndInnerImgUrlAndPlatformId(@Param("platformId")Integer platformId);

}
