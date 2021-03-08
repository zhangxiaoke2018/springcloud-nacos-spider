package com.jinguduo.spider.db.repo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.jinguduo.spider.data.table.Ads;

import java.util.Date;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @DATE 06/12/2017 10:12
 */
@Deprecated
public interface AdsRepo extends JpaRepository<Ads, Integer> {

    Ads findByFileName(String fileName);

    Page<Ads> findAllOrderByName(Pageable pageable);

    Page<Ads> findByCreatedAtLessThan(Date createdAt, Pageable pageable);

}
