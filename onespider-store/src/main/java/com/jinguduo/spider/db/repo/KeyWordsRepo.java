package com.jinguduo.spider.db.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jinguduo.spider.data.table.Keywords;

import java.util.List;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 06/12/2016 2:58 PM
 */
@Repository
public interface KeyWordsRepo extends JpaRepository<Keywords,Integer> {

    Keywords findByKeyword(String keyword);

}
