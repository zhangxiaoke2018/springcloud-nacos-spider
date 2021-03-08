package com.jinguduo.spider.service;

import com.jinguduo.spider.data.table.KWords;
import com.jinguduo.spider.db.repo.KWordsRepo;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Deprecated
@Component
public class KWordsService {
    @Autowired
    private KWordsRepo kWordsRepo;

    public KWords insertKWord(KWords kWords){
        //精确查找
        KWords kWords_query = kWordsRepo.findByKeyword(kWords.getKeyword());
        if( null != kWords_query && StringUtils.isNotBlank(kWords_query.getKeyword())){
            return kWords_query;
        }
        return kWordsRepo.save(kWords);
    }

}
