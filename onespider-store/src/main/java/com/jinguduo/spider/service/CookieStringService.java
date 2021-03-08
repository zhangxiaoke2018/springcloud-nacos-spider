package com.jinguduo.spider.service;

import javax.annotation.Resource;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.jinguduo.spider.data.table.CookieString;
import com.jinguduo.spider.db.repo.CookieStringRepo;

@Service
public class CookieStringService {
    
    @Resource
    private CookieStringRepo cookieStringRepo;

    public Page<CookieString> findAllByDomain(String domain, Integer page, Integer size) {
        
        Pageable pageable = new PageRequest(page, size);
        return cookieStringRepo.findAllByDomain(domain, pageable);
    }

    public CookieString save(CookieString cookieString) {
        return cookieStringRepo.save(cookieString);
    }

}
