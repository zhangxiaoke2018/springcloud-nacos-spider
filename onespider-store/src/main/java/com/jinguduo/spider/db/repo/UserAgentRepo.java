package com.jinguduo.spider.db.repo;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

import com.jinguduo.spider.common.constant.UserAgentKind;
import com.jinguduo.spider.data.table.UserAgent;

@Component
public interface UserAgentRepo extends CrudRepository<UserAgent, Integer> {

    Iterable<UserAgent> findAllByKind(UserAgentKind kind);

}
