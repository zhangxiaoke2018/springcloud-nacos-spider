package com.jinguduo.spider.service;

import com.jinguduo.spider.data.table.SearchSite;
import com.jinguduo.spider.db.repo.SearchSiteRepo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SearchSiteService {

    @Autowired
    private SearchSiteRepo searchSiteRepo;

    public List<SearchSite> querySearchSite(){

        return searchSiteRepo.findAll();
    }

    public List<SearchSite> findMediaSearchSiteByEnable(Integer enable){

        return searchSiteRepo.findMediaSearchSiteByEnable(enable);
    }
}
