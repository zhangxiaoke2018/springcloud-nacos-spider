package com.jinguduo.spider.service;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.fluent.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.jinguduo.spider.common.constant.ProxyState;
import com.jinguduo.spider.common.proxy.ProxyType;
import com.jinguduo.spider.data.table.Proxy;
import com.jinguduo.spider.db.repo.ProxyRepo;

import lombok.extern.apachecommons.CommonsLog;

@CommonsLog
@Service
public class ProxyService {
    
    @Autowired
    private ProxyRepo proxyRepo;
    
    public Page<Proxy> findAllByStateAndPtypeIn(ProxyState state, Collection<ProxyType> ptypes, int page, int size) {
        Sort sort = new Sort(Direction.DESC, "updatedAt");
        Pageable pageReq = new PageRequest(page, size, sort);
        if (ptypes == null || ptypes.isEmpty()) {
            return proxyRepo.findAllByState(state, pageReq);
        } else {
            return proxyRepo.findAllByStateAndPtypeIn(state, ptypes, pageReq);
        }
    }

    public Iterable<Proxy> save(List<Proxy> proxies) {
        List<Proxy> result = Lists.newArrayList();
        for (Proxy proxy : proxies) {
        	String host = proxy.getHost().trim();  // 万恶的空格
        	if (StringUtils.isBlank(host) || !host.contains(":")) {
        		log.warn("The host maybe bad." + host);
				continue;
			}
            proxy.setHost(host);
            if (proxy.getState().ordinal() < ProxyState.Availabled.ordinal()) {
                // Availabled状态的出口只有Broken
                Proxy p = proxyRepo.findOne(proxy.getHost());
                if (p != null && p.getState() == ProxyState.Availabled) {
                    continue;
                }
            }
            Proxy r = proxyRepo.save(proxy);
            result.add(r);
        }
        return result;
    }

    @Transactional
    public Proxy update(Proxy proxy) {

        if (StringUtils.isBlank(proxy.getHostName())) {
            return null;
        }

        List<Proxy> proxies = proxyRepo.findByServerName(proxy.getServerName());

        if (proxies != null && proxies.size() > 0) {
        	proxyRepo.delete(proxies);
        }
        return proxyRepo.save(proxy);
    }

    public List<Proxy> findAllByState(ProxyState state) {
        return proxyRepo.findAllByState(state);
    }

	public List<Proxy> randomSearchByState(ProxyState state, Integer size) {
		Pageable pr = new PageRequest(0, size);
		Page<Proxy> r = proxyRepo.randomSearchByState(state, pr);
		if (r != null) {
        	return r.getContent();
		}
        return Lists.newArrayList();
	}

	public List<Proxy> findKuaidaili() throws IOException {

        String url = "http://svip.kdlapi.com/api/getproxy/?orderid=936273988936860&num=500&protocol=1&method=2&an_an=1&an_ha=1&quality=2&sep=1";
        String[] ps = Request.Get(url).execute().returnContent().toString().split("\r\n");

        List<Proxy> proxies = Lists.newArrayList();
        for (String p : ps) {
            Proxy proxy = Proxy.newHttpProxy();
            proxy.setHost(p);
            proxies.add(proxy);

        }
        return proxies;
    }


}
