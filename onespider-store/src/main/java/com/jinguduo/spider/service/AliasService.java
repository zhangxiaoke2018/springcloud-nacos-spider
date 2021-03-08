package com.jinguduo.spider.service;

import com.jinguduo.spider.data.table.Alias;
import com.jinguduo.spider.data.table.AliasPolymerization;
import com.jinguduo.spider.data.table.Classify;
import com.jinguduo.spider.data.table.RelationKeywords;
import com.jinguduo.spider.db.repo.AliasRepo;
import com.jinguduo.spider.db.repo.RelationKeywordsRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lc on 2017/6/7.
 */
@Service
public class AliasService {
    @Autowired
    private AliasRepo aliasRepo;

    @Autowired
    private RelationKeywordsRepo keywordsRepo;

    /**
     * 返回所有的别名和微信相关词信息
     */
    public List<AliasPolymerization> getAll() {
        //all data
        List<Alias> relevanceIds = aliasRepo.findAllGroupRIdAndType();
        List<Alias> aliases = aliasRepo.findAll();
        List<RelationKeywords> keywordss = keywordsRepo.findAll();
        List<AliasPolymerization> polymerizations = this.disposeData(relevanceIds, aliases, keywordss);
        return polymerizations;
    }


    public List<AliasPolymerization> getAllByClassify(String classify) {
        //all data
        List<Alias> relevanceIds = aliasRepo.findAllByClassifyGroupRIdAndType(classify);
        List<Alias> aliases = aliasRepo.findAllByClassify(classify);
        List<RelationKeywords> keywordss = keywordsRepo.findAllByClassify(classify);
        List<AliasPolymerization> polymerizations = this.disposeData(relevanceIds, aliases, keywordss);
        return polymerizations;
    }

    public List<AliasPolymerization> getAllByClassifyAndCategoryAndType(String classify, String category, Byte type) {
        //all data
        List<Alias> relevanceIds = aliasRepo.findAllByClassifyAndCategoryAndTypeGroupRIdAndType(classify, category, type);
        List<Alias> aliases = aliasRepo.findAllByClassifyAndCategoryAndType(classify, category, type);
        List<RelationKeywords> keywordss = keywordsRepo.findAllByClassifyAndType(classify, type);
        List<AliasPolymerization> polymerizations = this.disposeData(relevanceIds, aliases, keywordss);
        return polymerizations;
    }


    public List<AliasPolymerization> getByRelevanceIdAndType(Integer relevanceId, Byte type) {
        //all data
        List<Alias> relevanceIds = aliasRepo.findAllByRIdAndTypeGroupRIdAndType(relevanceId, type);
        List<Alias> aliases = aliasRepo.findAllByRelevanceIdAndType(relevanceId, type);
        List<RelationKeywords> keywordss = keywordsRepo.findAllByRelevanceIdAndType(relevanceId, type);
        List<AliasPolymerization> polymerizations = this.disposeData(relevanceIds, aliases, keywordss);
        return polymerizations;
    }


    /**
     * 给三个list，聚合数据
     *
     * @param relevanceIds 总体约束
     * @param aliases      别名
     * @param keywordss    关联词
     */
    private List<AliasPolymerization> disposeData(List<Alias> relevanceIds, List<Alias> aliases, List<RelationKeywords> keywordss) {
        //create return list
        List<AliasPolymerization> aps = new ArrayList<>();

        //将List扔到map
        Map<String, List<Alias>> aliasDataMap = new HashMap<>();
        for (Alias aa : aliases) {
            List<Alias> mapAlias = aliasDataMap.get(aa.getCode());
            if (null == mapAlias) {
                List<Alias> aaList = new ArrayList<>();
                aaList.add(aa);
                aliasDataMap.put(aa.getCode(), aaList);
            } else {
                mapAlias.add(aa);
            }
        }

        Map<String, List<RelationKeywords>> keyWordsDataMap = new HashMap<>();
        for (RelationKeywords keywords : keywordss) {
            List<RelationKeywords> mapKeyWords = keyWordsDataMap.get(keywords.getRelevanceId() + "" + keywords.getType());
            if (null == mapKeyWords) {
                List<RelationKeywords> keywordsList = new ArrayList<>();
                keywordsList.add(keywords);
                keyWordsDataMap.put(keywords.getRelevanceId() + "" + keywords.getType(), keywordsList);
            } else {
                mapKeyWords.add(keywords);
            }
        }

        //polymerization
        for (Alias initAl : relevanceIds) {
            //create one
            AliasPolymerization ap = new AliasPolymerization();

            //baseAttribute
            ap.setRelevanceId(initAl.getRelevanceId());
            ap.setType(initAl.getType());

            //aliasList
            List<Alias> theAList = aliasDataMap.get(initAl.getCode());
            ap.setAliases(theAList);
            //keywordsList
            List<RelationKeywords> theKList = keyWordsDataMap.get(initAl.getCode());
            ap.setRelationKeywords(theKList);

            aps.add(ap);
        }
        return aps;
    }


    public Alias updateAlias(Alias alias) {
        Alias one = aliasRepo.findOne(alias.getId());
        if (null == one) {
            return null;
        }
        one.setAlias(alias.getAlias());
        return aliasRepo.saveAndFlush(one);
    }

    /**
     * 未聚合的别名信息
     */
    public List<Alias> getAliasByClassify(String classify) {
        List<Alias> aliases = aliasRepo.findAllByClassify(classify);
        return aliases;
    }

    /**
     * 唯一获取
     */
    public Alias getAliasByUk(Integer linkedId, Byte type, String classify) {
        return aliasRepo.findByRelevanceIdAndTypeAndClassify(linkedId, type, classify);
    }

    public void generateAlias(Integer linkedId, String showName, String category) {
        //遍历生成别名
        for (Classify c : Classify.values()) {
            Alias alias = aliasRepo.findByRelevanceIdAndTypeAndClassify(linkedId, Byte.valueOf("0"), c.name());
            if (alias != null) {
                alias.setAlias(showName);
                alias.setCategory(category);
                aliasRepo.save(alias);
            } else {
                Alias a = new Alias();
                a.setRelevanceId(linkedId);
                a.setAlias(showName);
                a.setCategory(category);
                a.setType(Byte.valueOf("0"));
                a.setCode(linkedId + "0");
                a.setClassify(c.name());
                aliasRepo.save(a);
            }
        }
    }

    public List<Object[]> findWeiboActorKeyWords() {
        List<Object[]> weiboActorKeyWords = aliasRepo.findWeiboActorKeyWords();
        return weiboActorKeyWords;
    }

    public List<Alias> findWeiboIndexKeywords() {

        List<Alias> weiboIndexKeywords = aliasRepo.findWeiboIndexKeywords();

        return weiboIndexKeywords;
    }


}
