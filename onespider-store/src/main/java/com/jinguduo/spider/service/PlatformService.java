package com.jinguduo.spider.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jinguduo.spider.data.table.Platform;
import com.jinguduo.spider.db.repo.PlatformRepo;

import lombok.extern.apachecommons.CommonsLog;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 16/6/20 下午2:59
 */
@CommonsLog
@Service
public class PlatformService {

    @Autowired
    private PlatformRepo platformRepo;

    /**
     * 优先匹配原则 : 先匹配多的
     * for ex:
     *  acfun.tv -- 先匹配
     *  fun.tv   -- 后匹配
     * @param url
     * @return
     */
    public Platform find(String url){

        try {
            url = URLDecoder.decode(url,"UTF-8");
            List<Platform> platforms = platformRepo.findAll()
                    .stream()
                    .sorted((x, y) -> x.getSymbol().length() > y.getSymbol().length() ? -1 : 1)
                    .collect(Collectors.toList());

            for (Platform platform : platforms) {
                if(url.contains(platform.getSymbol())){
                    return platform;
                }
            }
        } catch (UnsupportedEncodingException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }
}
