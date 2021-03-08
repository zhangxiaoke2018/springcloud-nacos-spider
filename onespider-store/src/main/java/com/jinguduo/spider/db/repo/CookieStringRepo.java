package com.jinguduo.spider.db.repo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

import com.jinguduo.spider.data.table.CookieString;

@Component
public interface CookieStringRepo extends CrudRepository<CookieString, Integer> {

    Page<CookieString> findAllByDomain(String domain, Pageable pageable);

}
