package com.jinguduo.spider.service;

import com.jinguduo.spider.data.table.NewsKeyword;
import com.jinguduo.spider.db.repo.NewsKeywordRepo;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.criteria.CriteriaBuilder;
import java.util.List;

@CommonsLog
@Service
public class NewsKeywordService {

    @Autowired
    private NewsKeywordRepo newsKeywordRepo;

    public NewsKeyword fetch(Integer id,String classify,String keywords,Byte type,String code){

       NewsKeyword newsKeyword = newsKeywordRepo.findOne(id);
        if(newsKeyword == null ||newsKeyword.getId() == -1){
            NewsKeyword keyword = new NewsKeyword();
            keyword.setType(type);
            keyword.setClassify(classify);
            keyword.setKeywords(keywords);
            keyword.setCode(code);
            return  newsKeywordRepo.save(keyword);
        }else{
            NewsKeyword keyword = new NewsKeyword();
            keyword.setId(newsKeyword.getId());
            keyword.setType(type);
            keyword.setClassify(classify);
            keyword.setKeywords(keywords);
            keyword.setCode(code);
            return  newsKeywordRepo.save(keyword);
        }

    }

    public List<NewsKeyword> get(String keywords){
        return newsKeywordRepo.findAllByKeywordsGroupId(keywords);
    }

    public void delete(Integer id){
         newsKeywordRepo.delete(id);

    }

    /**
     * 未聚合的别名信息
     */
    public List<NewsKeyword> getAliasByClassify(String classify) {
        List<NewsKeyword> newsKeywordList = newsKeywordRepo.findAllByClassify(classify);
        return newsKeywordList;
    }


}
