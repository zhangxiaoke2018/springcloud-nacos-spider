package com.jinguduo.spider.service;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import com.google.common.collect.Lists;
import com.jinguduo.spider.data.table.Show;
import com.jinguduo.spider.db.repo.ShowRepo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.jinguduo.spider.common.code.FetchCodeEnum;
import com.jinguduo.spider.common.constant.StatusEnum;
import com.jinguduo.spider.data.table.Seed;
import com.jinguduo.spider.db.repo.SeedRepo;

import lombok.extern.apachecommons.CommonsLog;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @DATE 16/6/17 下午3:57
 */
@CommonsLog
@Service
public class SeedService {

    @Resource
    private SeedRepo seedRepo;

    @Autowired
    private ShowRepo showRepo;

    public Seed insertOrUpdate(Seed seed) {
        assert seed != null;

        if (seed.getId() != null) {
            Seed old = seedRepo.findOne(seed.getId());
            BeanUtils.copyProperties(seed, old);
            seed = old;
        }
        return seedRepo.save(seed);
    }
    
    public Seed updateSeed(Seed seed){
        Assert.notNull(seed);
        Assert.notNull(seed.getId());
        seed.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        return seedRepo.save(seed);
    }
    
    public Seed getOne(Integer id){
        return seedRepo.findOne(id);
    }

    /***
     * 插入seed
     * 如果Url存在，更新
     * @param seed
     * @return
     */
    public Seed addSeed(Seed seed) {
        if ( null == seed ) {
            return null;
        }
        //移除url中的空格
        seed.setUrl(seed.getUrl().replace(" ",""));

        //中文encoding
        String regex="([\u4e00-\u9fa5]+)";
        Matcher matcher = Pattern.compile(regex).matcher(seed.getUrl());
        if(matcher.find()){
            String chinese = matcher.group(0);
            try {
                String english = URLEncoder.encode(chinese, "UTF8");
                seed.setUrl(seed.getUrl().replace(chinese, english));
            } catch (UnsupportedEncodingException e) {
                log.error(e.getMessage(), e);
            }
        }

        Seed oldSeed = seedRepo.findByUrl(seed.getUrl());
        if ( null == oldSeed || null == oldSeed.getId()) {//不存在此url
            return seedRepo.save(seed);
        } else {//存在，更新操作
            oldSeed.setUrl(seed.getUrl());
            oldSeed.setPlatformId(seed.getPlatformId());
            //状态归位
            oldSeed.setStatus(StatusEnum.STATUS_OK.getValue());
            return seedRepo.save(oldSeed);
        }
    }

//    public Seed find(String url){
//        String decodeUrl = null;
//        try {
//            decodeUrl = URLDecoder.decode(url,"UTF-8");
//        } catch (UnsupportedEncodingException e) {
//            log.error(e.getMessage(), e);
//        }
//        return seedRepo.findByUrl(decodeUrl);
//    }
    
    public Seed find(String url,Integer status){
        String decodeUrl = null;
        try {
            decodeUrl = URLDecoder.decode(url,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.error(e.getMessage(), e);
        }
        return seedRepo.findByUrlAndStatus(decodeUrl,status);
    }

    public String findCode(String url) {

        try {
            String decodeUrl = URLDecoder.decode(url,"UTF-8");
            return FetchCodeEnum.getCode(decodeUrl);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return "";
    }

    public Seed findByCode(String code){
        return seedRepo.findByCode(code);

    }
    
    public Seed findByCode(String code,Integer status){
        return seedRepo.findByCodeAndStatus(code,status);

    }
    
    public Page<Seed> findSeedPage(int page,int size){
        Pageable pageable = new PageRequest(page, size);
        return seedRepo.findAll(pageable);
    }
    
    //supervisor 调用
    public Collection<Seed> findAllSeedByTime(Long time){
        Assert.notNull(time);
        // 过滤pp视频的动漫
        List<Show> showList = showRepo.findByCategoryAndDepthAndPlatformId("ANIME", 1, 11);
        List<String> codes = showList.stream().map(p -> p.getCode()).collect(Collectors.toList());
        Collection<Seed> seeds = seedRepo.findAllByStatusAndUpdatedAtGreaterThanEqualOrderByFrequencyAscPriorityDesc(StatusEnum.STATUS_OK.getValue(), new Timestamp(time));
        List<Seed> list = seeds.stream().filter(item -> !codes.contains(item.getCode())).collect(Collectors.toList());
        return list;
    }
    
    public void deleteSeedByCode(String code){
        Seed seed = this.findByCode(code);
        if(seed!=null){
            seed.setStatus(StatusEnum.STATUS_DEL.getValue());
            seedRepo.save(seed);
        }
    }
}
