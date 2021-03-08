package com.jinguduo.spider.db.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jinguduo.spider.data.table.MaoyanBox;

import java.util.Date;

/**
 * Created by lc on 2017/5/10.
 */
public interface MaoyanBoxRepo extends JpaRepository<MaoyanBox, Integer> {
    MaoyanBox findByDayAndMovieId(Date day, Integer movieId);
}
