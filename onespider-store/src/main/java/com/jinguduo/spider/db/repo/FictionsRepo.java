package com.jinguduo.spider.db.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jinguduo.spider.data.table.Fiction;

/**
 * 
 * @author huhu
 *
 */
public interface FictionsRepo extends JpaRepository<Fiction, Integer> {

	Fiction findByNameAndAuthor(String name,String author);
}
