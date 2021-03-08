package com.jinguduo.spider.db.repo;

import com.jinguduo.spider.data.table.ComicBanner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;

/**
 * Created by lc on 2019/8/12
 */
@Repository
public interface ComicBannerRepo extends JpaRepository<ComicBanner, Integer> {

    ComicBanner findByCodeAndPlatformIdAndDay(String code, Integer platformId, Date day);
}
