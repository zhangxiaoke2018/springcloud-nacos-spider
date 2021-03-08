package com.jinguduo.spider.db.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jinguduo.spider.data.table.ShowActors;

import java.util.List;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @DATE 03/07/2017 17:40
 */
@Repository
public interface ShowActorsRepo extends JpaRepository<ShowActors, Integer> {

    ShowActors findByCodeAndCelebrityCode(String code, String celebrityCode);

    List<ShowActors> findByCode(String code);

}
