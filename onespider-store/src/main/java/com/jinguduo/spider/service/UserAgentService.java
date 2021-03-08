package com.jinguduo.spider.service;

import javax.annotation.Resource;

import com.jinguduo.spider.common.constant.UserAgentKind;
import com.jinguduo.spider.data.table.UserAgent;

import org.springframework.stereotype.Service;

import com.jinguduo.spider.db.repo.UserAgentRepo;

@Service
public class UserAgentService {
    
    @Resource
    private UserAgentRepo userAgentRepo;

    public Iterable<UserAgent> findByKind(UserAgentKind kind) {
        return userAgentRepo.findAllByKind(kind);
    }

}
