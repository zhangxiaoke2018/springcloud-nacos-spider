package com.jinguduo.spider.service;

import com.google.common.collect.Sets;
import com.jinguduo.spider.data.table.Alias;
import com.jinguduo.spider.data.table.CoreKeyword;
import com.jinguduo.spider.data.table.ShowActors;
import com.jinguduo.spider.db.repo.AliasRepo;
import com.jinguduo.spider.db.repo.CoreKeywordRepo;
import com.jinguduo.spider.db.repo.ShowActorsRepo;
import javafx.util.Pair;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @DATE 10/11/2017 09:54
 */
@Service
public class CoreKeywordService {

    @Autowired
    private CoreKeywordRepo coreKeywordRepo;

    @Autowired
    private AliasRepo aliasRepo;

    @Autowired
    private ShowActorsRepo showActorsRepo;


    public List<String> findAll(){
        return coreKeywordRepo.findAll().stream().map(c -> c.getKeyword()).collect(Collectors.toList());
    }

    public Set<Pair<String,String>> findAliasAll(){

        List<CoreKeyword> coreKeywords = coreKeywordRepo.findAll();
        Set<Pair<String,String>> respKeywords = Sets.newHashSet();
        for (CoreKeyword keyword : coreKeywords) {

            Alias wechatArticle = aliasRepo.findByRelevanceIdAndTypeAndClassify(keyword.getRelevanceId(), keyword.getType().byteValue(), "WECHAT_ARTICLE");

            if (wechatArticle == null){
                respKeywords.add(new Pair<>(keyword.getRelevanceId()+""+keyword.getType(),keyword.getKeyword()));
                continue;
            }

            String[] keywords = wechatArticle.getAlias().split("/");

            for (String k : keywords) {
                respKeywords.add(new Pair<>(keyword.getRelevanceId()+""+keyword.getType(),k));
            }

        }
        return respKeywords;
    }


}
