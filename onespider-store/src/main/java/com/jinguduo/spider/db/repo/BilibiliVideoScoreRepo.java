package com.jinguduo.spider.db.repo;

import com.jinguduo.spider.data.table.BilibiliVideoScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;


@Repository
public interface BilibiliVideoScoreRepo extends JpaRepository<BilibiliVideoScore, Integer> {
    BilibiliVideoScore findByCodeAndDayAndScoreAndScoreNumber(String code,Date date,Double score,Integer scoreNumber);
}
