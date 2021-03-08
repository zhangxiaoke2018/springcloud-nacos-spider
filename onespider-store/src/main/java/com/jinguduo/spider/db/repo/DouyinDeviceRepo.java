package com.jinguduo.spider.db.repo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

import com.jinguduo.spider.data.table.DouyinDevice;

@Component
public interface DouyinDeviceRepo extends CrudRepository<DouyinDevice, Integer> {

	@Query("SELECT dd FROM DouyinDevice dd ORDER BY RAND()")
	Page<DouyinDevice> findByRandomSorted(Pageable pageable);

}
