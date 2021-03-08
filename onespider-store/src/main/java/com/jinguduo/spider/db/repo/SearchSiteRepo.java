package com.jinguduo.spider.db.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import com.jinguduo.spider.data.table.SearchSite;

import java.util.List;

@Component
public interface SearchSiteRepo extends JpaRepository<SearchSite, Integer> {
    List<SearchSite> findMediaSearchSiteByEnable(Integer enable);
}
