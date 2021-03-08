package com.jinguduo.spider.db.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jinguduo.spider.data.table.Customer360Logs;

import java.util.Date;

/**
 * I'm a cute code dog
 * User: LingChen
 * Date:2017/7/11
 * Time:11:06
 */
public interface Customer360LogRepo extends JpaRepository<Customer360Logs, Integer> {
    Customer360Logs findByCodeAndProvinceAndDay(String code, String province, Date dataDate);
}
