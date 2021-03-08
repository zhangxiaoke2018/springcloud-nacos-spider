package com.jinguduo.spider.db.repo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jinguduo.spider.data.table.SegmentDictionary;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 28/04/2017 14:57
 */
@Repository
public interface SegmentDictionaryRepo extends JpaRepository<SegmentDictionary, Integer> {


    Page<SegmentDictionary> findAll(Specification<SegmentDictionary> spec, Pageable pageable);
}
