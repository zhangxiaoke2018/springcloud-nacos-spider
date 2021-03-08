package com.jinguduo.spider.db.repo;

import com.jinguduo.spider.data.table.ComicDmmh;
import com.jinguduo.spider.data.table.ComicU17;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @DATE 31/07/2017 16:21
 */
@Repository
public interface ComicDmmhRepo extends JpaRepository<ComicDmmh, Integer> {

}
