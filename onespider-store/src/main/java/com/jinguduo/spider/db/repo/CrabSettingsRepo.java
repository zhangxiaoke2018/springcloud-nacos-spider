package com.jinguduo.spider.db.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jinguduo.spider.data.table.CrabSettings;

import java.sql.Timestamp;
import java.util.List;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 23/05/2017 15:52
 */
@Repository
public interface CrabSettingsRepo extends JpaRepository<CrabSettings, Integer> {

    List<CrabSettings> findByUpdatedAtGreaterThan(Timestamp updatedAt);


}
