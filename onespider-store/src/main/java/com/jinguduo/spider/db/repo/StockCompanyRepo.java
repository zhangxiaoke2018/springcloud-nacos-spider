package com.jinguduo.spider.db.repo;

import com.jinguduo.spider.data.table.StockCompany;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by lc on 2019/4/9
 */
@Repository
public interface StockCompanyRepo extends JpaRepository<StockCompany, Integer> {
}
