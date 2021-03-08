package com.jinguduo.spider.db.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.jinguduo.spider.data.table.WeiboOfficialLog;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 01/04/2017 11:05
 */
@Repository
public interface WeiboOfficialLogRepo extends JpaRepository<WeiboOfficialLog, Integer> {

    WeiboOfficialLog findTop1ByCodeAndWeiboNameNotNullOrderByIdDesc(String code);
}

