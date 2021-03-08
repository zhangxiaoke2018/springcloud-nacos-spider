package com.jinguduo.spider.service;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.jinguduo.spider.data.table.OpinionWords;
import com.jinguduo.spider.data.table.OpinionWordsFeature;
import com.jinguduo.spider.db.repo.OpinionWordsFeatureRepo;
import com.jinguduo.spider.db.repo.OpinionWordsRepo;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 24/03/2017 15:31
 */
@Service
@CommonsLog
public class OpinionWordsService {

    @Autowired
    private OpinionWordsRepo opinionWordsRepo;

    @Autowired
    private OpinionWordsFeatureRepo opinionWordsFeatureRepo;


    public List<OpinionWords> findAll(){
        return opinionWordsRepo.findAll();
    }

    public List<OpinionWords> findFeature(){

        List<OpinionWordsFeature> features = opinionWordsFeatureRepo.findAll();

        List<OpinionWords> words = opinionWordsRepo.findAll();

        List<OpinionWords> respWords = Lists.newArrayList();

        for (OpinionWords word : words) {
            for (OpinionWordsFeature feature : features) {
                if(feature.getCategory().equals(word.getCategory())){
                    // 分类相同 -> 追加
                    OpinionWords ow = new OpinionWords();
                    BeanUtils.copyProperties(word, ow);
                    ow.setSubjectFeature(feature.getSubject());
                    ow.setRelatedKeywordFeature(feature.getRelatedKeywords());
                    respWords.add(ow);
                }
            }
        }

        return respWords;
    }

    public List<OpinionWords> findNew(Timestamp createdAt){
        return opinionWordsRepo.findByCreatedAtGreaterThan(createdAt);
    }

    public List<OpinionWords> findByLinkedId(Integer linkedId){

        List<OpinionWords> opinionWordses = opinionWordsRepo.findByLinkedId(linkedId).stream().map(o -> this.split(o)).collect(Collectors.toList());

        return opinionWordses;
    }

    public OpinionWords insertOrUpdate(OpinionWords opinionWords){

        OpinionWords ow = null;

        //处理下分集
        if(StringUtils.isNotBlank(opinionWords.getEpisode())){

            Set<String> set = this.split(opinionWords.getEpisode(), "/");
            set.add(opinionWords.getEpi().toString());
            if(set.size() == 2){
                String tmpEpi = "";
                for (String str : set) {
                    if(str.length() > 3){
                        tmpEpi = str.substring(2, str.length());
                    }
                }
                if(StringUtils.isNotBlank(tmpEpi)){
                    set.add(tmpEpi);
                }
            }
            opinionWords.setEpisode(StringUtils.join(set, "/"));
        }

        if(opinionWords.getId() == null){//保存
            //先查询有无相同类型相同主题的
            OpinionWords word = opinionWordsRepo.findByLinkedIdAndCategoryAndSubjectAndEpi(opinionWords.getLinkedId(), opinionWords.getCategory(), opinionWords.getSubject(), opinionWords.getEpi());
            if(word != null){//合并相关词汇

                word = this.split(word);

                List RKList = Lists.newArrayList(
                        this.split(word.getRelatedKeyword(),"/"),
                        this.split(opinionWords.getRelatedKeyword(),"/")
                );
                String join = StringUtils.join(RKList, "/");
                word.setRelatedKeyword(join);

                List RNList = Lists.newArrayList(
                        this.split(word.getRoleName(),"/"),
                        this.split(opinionWords.getRoleName(), "/")
                );
                word.setRoleName(StringUtils.join(RNList, "/"));

                word = this.merge(word);

                ow = opinionWordsRepo.save(word);
            }else {
                ow = opinionWordsRepo.save(this.merge(opinionWords));
            }

        }else {

            OpinionWords one = opinionWordsRepo.findOne(opinionWords.getId());
            BeanUtils.copyProperties(opinionWords, one, "id");

            one = this.merge(one);

            ow = opinionWordsRepo.save(one);
        }
        return ow;
    }

    public void del(Integer id){
        opinionWordsRepo.delete(id);
    }


    public OpinionWords findById(Integer id) {

        return this.split(opinionWordsRepo.findOne(id));
    }

    public Set split(String word, String split){
        return Sets.newHashSet(
                Splitter.on(split)
                        .trimResults()
                        .omitEmptyStrings()
                        .split(word));

    }

    public OpinionWords split(OpinionWords words){

        if(words.getRelatedKeyword().indexOf("|") != -1){

            int centerNum = words.getRelatedKeyword().lastIndexOf("|");
            String role = words.getRelatedKeyword().substring(0, centerNum);
            String rk = words.getRelatedKeyword().substring(centerNum + 1, words.getRelatedKeyword().length());

            role = StringUtils.join(this.split(role,"|"),"/");

            words.setRoleName(role);
            words.setRelatedKeyword(rk);
        }
        return words;
    }

    /**
     * 合并角色名称与相关词
     * @return
     */
    public OpinionWords merge(OpinionWords words){

        Set nowRK = this.split(words.getRelatedKeyword(),"/");
        if(StringUtils.isNotBlank(words.getRoleName())){
            Set nowRN = this.split(words.getRoleName(),"/");

            words.setRelatedKeyword(StringUtils.join(nowRN, "|") + "|" + StringUtils.join(nowRK,"/"));
        }else {
            words.setRelatedKeyword(StringUtils.join(nowRK,"/"));
        }

        return words;
    }

}
