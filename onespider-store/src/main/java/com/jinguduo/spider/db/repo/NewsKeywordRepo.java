package com.jinguduo.spider.db.repo;

import com.jinguduo.spider.data.table.NewsKeyword;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.List;

@Repository
public interface NewsKeywordRepo extends PagingAndSortingRepository<NewsKeyword,Integer> {
    NewsKeyword findByClassifyAndKeywordsAndType(String classify,String keywords,Byte type);


    @Query(value = "select * from news_keyword where keywords=:keywords group by id", nativeQuery = true)
    List<NewsKeyword> findAllByKeywordsGroupId(@Param("keywords") String keywords);

    @Query(value = "select * from news_keyword where keywords=:keywords and classify=:classify  ", nativeQuery = true)
    NewsKeyword findByKeywordsAndClassify(@Param("keywords") String keywords,@Param("classify") String classify);

    List<NewsKeyword> findAllByClassify(String classify);

}
