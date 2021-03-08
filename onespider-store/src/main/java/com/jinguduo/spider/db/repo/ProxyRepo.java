package com.jinguduo.spider.db.repo;

import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

import com.jinguduo.spider.common.constant.ProxyState;
import com.jinguduo.spider.common.proxy.ProxyType;
import com.jinguduo.spider.data.table.Proxy;

@Component
public interface ProxyRepo extends CrudRepository<Proxy, String> {

    Page<Proxy> findAllByState(ProxyState state, Pageable pageable);
    
    Page<Proxy> findAllByStateAndPtypeIn(ProxyState state, Collection<ProxyType> ptypes, Pageable pageable);

    List<Proxy> findAllByState(ProxyState state);

    List<Proxy> findByServerName(String hostName);

    @Query("SELECT p FROM Proxy p Where p.state = ?1 ORDER BY RAND()")
	Page<Proxy> randomSearchByState(ProxyState state, Pageable pageable);
}
