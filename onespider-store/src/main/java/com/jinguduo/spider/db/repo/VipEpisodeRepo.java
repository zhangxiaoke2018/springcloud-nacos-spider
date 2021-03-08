package com.jinguduo.spider.db.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jinguduo.spider.data.table.VipEpisode;

/**
 * I'm a cute code dog
 * User: LingChen
 * Date:2017/7/3
 * Time:16:28
 */
public interface VipEpisodeRepo extends JpaRepository<VipEpisode, Integer> {
    VipEpisode findByCodeAndPlatformId(String code, Integer platformId);
}
