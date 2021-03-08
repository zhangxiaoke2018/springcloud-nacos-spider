package com.jinguduo.spider.db.repo;

import com.jinguduo.spider.data.table.StockBulletin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by lc on 2019/4/9
 */
@Repository
public interface StockBulletinRepo extends JpaRepository<StockBulletin, Integer> {

    StockBulletin findByCodeAndCompanyCode(String code, String companyCode);
}
