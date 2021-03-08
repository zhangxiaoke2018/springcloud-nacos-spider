package com.jinguduo.spider.service;

import com.jinguduo.spider.data.table.WechatArticleAdKeywords;
import com.jinguduo.spider.db.repo.WechatArticleAdKeywordsRepo;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 06/06/2017 10:36
 */
@Service
@CommonsLog
public class WechatArticleAdKeywordService {

    @Autowired
    private WechatArticleAdKeywordsRepo wechatArticleAdKeywordsRepo;


    public List<WechatArticleAdKeywords> findAll(){

        return wechatArticleAdKeywordsRepo.findAll();
    }


    public void del(Integer id){

        wechatArticleAdKeywordsRepo.delete(id);

    }

    public void add(String keyword){

        if(StringUtils.isBlank(keyword)) return;

        WechatArticleAdKeywords hadKeyword = wechatArticleAdKeywordsRepo.findByKeyword(keyword);

        if(hadKeyword == null){
            WechatArticleAdKeywords keywords = new WechatArticleAdKeywords();
            keywords.setKeyword(keyword);
            wechatArticleAdKeywordsRepo.save(keywords);
        }
    }

}
