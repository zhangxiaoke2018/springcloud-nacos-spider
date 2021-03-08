package com.jinguduo.spider.service;

import java.util.List;
import java.util.stream.Collectors;

import lombok.extern.apachecommons.CommonsLog;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.jinguduo.spider.data.table.Keywords;
import com.jinguduo.spider.data.table.RKeywordsLinked;
import com.jinguduo.spider.db.repo.KeyWordsRepo;
import com.jinguduo.spider.db.repo.RKeywordsLinkedRepo;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 06/12/2016 2:59 PM
 */
@Service
@CommonsLog
public class KeywordsService {

    @Autowired
    private KeyWordsRepo keyWordsRepo;

    @Autowired
    private RKeywordsLinkedRepo rKeywordsLinkedRepo;

    public List findAll(){

        return keyWordsRepo.findAll();

    }

    public List<Keywords> findByLinkedId(Integer linkedId){

        List<Keywords> keywordses = Lists.newArrayList();

        List<RKeywordsLinked> rls = rKeywordsLinkedRepo.findByLinkedId(linkedId);

        rls.forEach(r -> keywordses.add(keyWordsRepo.findOne(r.getKeywordsId())));

        return keywordses.stream().filter(k -> k.getDeleted().equals(Boolean.FALSE)).collect(Collectors.toList());
    }

    @Transactional
    public void del(Integer id, Integer linkedId){
        Keywords keywords = keyWordsRepo.findOne(id);
        //keywords.setDeleted(Boolean.TRUE);
        //keyWordsRepo.save(keywords); 暂不给关键词本身添加删除位,只把关系删除

        rKeywordsLinkedRepo.deleteByLinkedIdAndKeywordsId(linkedId,keywords.getId());


    }

    public Keywords insertOrUpdate(Keywords keyword) {

        Keywords rkds = null;

        if(keyword == null || StringUtils.isBlank(keyword.getKeyword())){
            return null;
        }
        Keywords existKW = keyWordsRepo.findByKeyword(keyword.getKeyword());


        if(existKW != null){

            if(existKW.getDeleted()){//是曾经删除过的 keyword ,初始化所有参数
                existKW.setGreatest(0);
                existKW.setDeleted(Boolean.FALSE);
                rkds = keyWordsRepo.save(existKW);
            }else{
                if(keyword.getGreatest() != null||StringUtils.isNotBlank(keyword.getRelatedKeyword())){
                    if(keyword.getGreatest() != null&&keyword.getGreatest()>=0){
                        existKW.setGreatest(keyword.getGreatest());
                    }
                    if(StringUtils.isNotBlank(keyword.getRelatedKeyword())){
                        existKW.setRelatedKeyword(keyword.getRelatedKeyword());
                        existKW.setModified(true);
                    }
                    rkds = keyWordsRepo.save(existKW);
                }else {
                    rkds = existKW;
                }
            }
        }else {
            if(keyword.getLinkedId() != null){
                Keywords kw = this.findByLinkedId(keyword.getLinkedId()).get(0);
                if(kw == null){ // 暂未同步最原始的数据,不允许添加
                    return null;
                }
                keyword.setCategory(kw.getCategory());
                keyword.setCode(DigestUtils.md5Hex(keyword.getKeyword()));
                rkds = keyWordsRepo.save(keyword);

            }else {
                return null;
            }
        }
        this.save(new RKeywordsLinked(keyword.getLinkedId(),rkds.getId()));
        return rkds;
    }

    private RKeywordsLinked save(RKeywordsLinked keywordsLinked){

        if(keywordsLinked.getKeywordsId() == null || keywordsLinked.getLinkedId() == null){
            return null;
        }
        RKeywordsLinked exRkl = rKeywordsLinkedRepo.findByLinkedIdAndKeywordsId(keywordsLinked.getLinkedId(), keywordsLinked.getKeywordsId());
        if(exRkl != null){//已存在
            return exRkl;
        }
        return rKeywordsLinkedRepo.save(keywordsLinked);
    }
}
