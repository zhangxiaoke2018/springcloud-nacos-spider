package com.jinguduo.spider.web;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jinguduo.spider.common.constant.ProxyState;
import com.jinguduo.spider.common.proxy.ProxyType;
import com.jinguduo.spider.data.table.Proxy;
import com.jinguduo.spider.service.ProxyService;

@Controller
@ResponseBody
@Slf4j
public class ProxyController {
    
    @Autowired
    private ProxyService proxyService;
    
    @GetMapping(value = "/proxies")
    public Iterable<Proxy> list(
            @RequestParam(name = "state") ProxyState state,
            @RequestParam(name = "ptype", required = false) Collection<ProxyType> ptypes,
            @RequestParam(name = "page", defaultValue = "0", required = false) Integer page,
            @RequestParam(name = "size", defaultValue = "100", required = false) Integer size) throws IOException {

        if (ProxyState.Kuaidaili.equals(state)) {
            return proxyService.findKuaidaili();
        }

        Page<Proxy> resp = proxyService.findAllByStateAndPtypeIn(state, ptypes, page, size);
        return resp.getContent();
    }
    
    @GetMapping(value = "/proxies", consumes = "text/plain", produces = "text/plain")
    public String listPlain(
    		@RequestParam(name = "size", defaultValue = "0", required = false) Integer size,
            @RequestParam(name = "state") ProxyState state) throws IOException {
        
    	List<Proxy> proxies = null;

        if (ProxyState.Kuaidaili.equals(state)) {
            proxies = proxyService.findKuaidaili();

        } else {
            if (size == 0) {
                proxies = proxyService.findAllByState(state);
            } else {
                proxies = proxyService.randomSearchByState(state, size);
            }
        }

        if (proxies == null || proxies.isEmpty()) {
            return "";
        }
        return proxies.stream().map(e -> e.getHost()).collect( Collectors.joining("\n"));
    }


    @PostMapping(value = "/proxies")
    public Iterable<Proxy> post(
            @RequestBody List<Proxy> proxies) {
        return proxyService.save(proxies);
    }

    @PostMapping(value = "/proxy")
    public Proxy postOne(
            @RequestBody Proxy proxy) {
        return proxyService.update(proxy);
    }

}
