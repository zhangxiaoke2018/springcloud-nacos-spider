package com.jinguduo.spider.db.repo;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jinguduo.spider.data.table.BoxOfficeLogs;

/**
 * I'm a cute code dog
 * User: LingChen
 * Date:2017/8/7
 * Time:14:13
 */
@Repository
public interface ChinaBoxOfficeRepo extends JpaRepository<BoxOfficeLogs, Integer> {

}
