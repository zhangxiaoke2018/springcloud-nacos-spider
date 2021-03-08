package com.jinguduo.spider.service;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jinguduo.spider.data.table.RelationKeywords;
import com.jinguduo.spider.db.repo.RelationKeywordsRepo;

/**
 * 
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @author liuxinglong
 * @DATE 2017年7月6日 下午5:16:20
 *
 */
@Service
@Slf4j
public class RelationKeywordsService {
    @Autowired
    private RelationKeywordsRepo keywordsRepo;


    public RelationKeywords updateRKeywords(RelationKeywords rkeyword) {
        RelationKeywords one = keywordsRepo.findOne(rkeyword.getId());
        if (null == one) {
            return null;
        }
        if(!StringUtils.equals(one.getRelationWords(), rkeyword.getRelationWords())){
            one.setRelationWords(rkeyword.getRelationWords());
            one.setModify(1);
        }else{
            return one;
        }
        return keywordsRepo.saveAndFlush(one);
    }
    
}