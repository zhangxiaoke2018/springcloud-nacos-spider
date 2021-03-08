package com.jinguduo.spider.service;

import com.jinguduo.spider.data.table.Ads;
import com.jinguduo.spider.db.repo.AdsRepo;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @DATE 06/12/2017 18:30
 */
@Deprecated
@Service
@CommonsLog
public class AdsService {

    @Autowired
    private AdsRepo adsRepo;


    public List<Ads> findAll(){

        return adsRepo.findAll();

    }

    public Map page(Integer page, Integer size){

        Map map = new HashMap();
        try {

            Date date = DateUtils.parseDate("2018-01-08", "yyyy-MM-dd");
            Pageable pageable = new PageRequest(page, size, Sort.Direction.ASC, "name");
            Page<Ads> adsPage = adsRepo.findByCreatedAtLessThan(date, pageable);
            map.put("total_page", adsPage.getTotalPages());
            map.put("list", adsPage.getContent());

        } catch (ParseException e) {
            log.error(e.getMessage(), e);
        }



        return map;

    }


    public List<Ads> findAllNoName(){

        return adsRepo.findAll().stream().filter(a -> StringUtils.isNotBlank(a.getName())).collect(Collectors.toList());

    }

    public Ads edit(String name, Integer id){

        Ads ads = adsRepo.findOne(id);

        if (ads == null) {
             return null;
        }
        ads.setName(name);
        return adsRepo.save(ads);
    }






}
