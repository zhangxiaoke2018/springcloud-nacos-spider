package com.jinguduo.spider.db.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import com.jinguduo.spider.data.table.KWords;

@Component
public interface KWordsRepo extends JpaRepository<KWords, Integer> {

    KWords findByKeyword(String keyword);
}
