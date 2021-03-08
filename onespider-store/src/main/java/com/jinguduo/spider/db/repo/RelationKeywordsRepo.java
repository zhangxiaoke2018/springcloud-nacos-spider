package com.jinguduo.spider.db.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jinguduo.spider.data.table.RelationKeywords;

import java.util.List;

/**
 * Created by lc on 2017/6/7.
 */
@Repository
public interface RelationKeywordsRepo extends JpaRepository<RelationKeywords, Integer> {
    List<RelationKeywords> findAllByClassify(String classify);

    List<RelationKeywords> findAllByClassifyAndType(String classify, Byte type);

    List<RelationKeywords> findAllByRelevanceIdAndType(Integer relevanceId, Byte type);
}
