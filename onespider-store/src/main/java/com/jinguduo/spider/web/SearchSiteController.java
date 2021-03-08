package com.jinguduo.spider.web;

import com.jinguduo.spider.data.table.SearchSite;
import com.jinguduo.spider.service.SearchSiteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class SearchSiteController {
    @Autowired
    private SearchSiteService searchSiteService;

    @RequestMapping(path = "querySearchSite",method = RequestMethod.GET)
    public List<SearchSite> querySearchSite(){

        List<SearchSite> searchSites = searchSiteService.querySearchSite();

        return searchSites;
    }

}
