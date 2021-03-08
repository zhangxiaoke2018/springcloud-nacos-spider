package com.jinguduo.spider.db.repo;

import com.jinguduo.spider.data.table.JdGoods;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;

/**
 * Created by lc on 2019/10/31
 */
public interface JdGoodsRepo extends JpaRepository<JdGoods, Integer> {
    JdGoods findByDayAndGoodsId(Date day, String goodsId);
}
