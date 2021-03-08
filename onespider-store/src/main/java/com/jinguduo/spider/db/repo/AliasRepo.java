package com.jinguduo.spider.db.repo;

import com.jinguduo.spider.data.table.Alias;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by lc on 2017/6/7.
 */
@Repository
public interface AliasRepo extends JpaRepository<Alias, Long> {
    @Query(value = "select * from alias group by relevance_id,`type` ", nativeQuery = true)
    List<Alias> findAllGroupRIdAndType();

    @Query(value = "select * from alias where classify = :classify group by relevance_id,`type` ", nativeQuery = true)
    List<Alias> findAllByClassifyGroupRIdAndType(@Param("classify") String classify);

    @Query(value = "select * from alias where classify = :classify and category = :category and type = :type group by relevance_id,`type` ", nativeQuery = true)
    List<Alias> findAllByClassifyAndCategoryAndTypeGroupRIdAndType(@Param("classify") String classify, @Param("category") String category, @Param("type") Byte type);

    @Query(value = "select * from alias where relevance_id = :relevanceId and type = :type group by relevance_id,`type` ", nativeQuery = true)
    List<Alias> findAllByRIdAndTypeGroupRIdAndType(@Param("relevanceId") Integer relevanceId, @Param("type") Byte type);

    List<Alias> findAllByClassify(String classify);


    List<Alias> findAllByClassifyAndCategoryAndType(String classify, String category, Byte type);

    List<Alias> findAllByRelevanceIdAndType(Integer relevanceId, Byte type);

    Alias findByRelevanceIdAndTypeAndClassify(Integer relevanceId, Byte type, String classify);

    @Query(value = "SELECT t2.`code`,t2.alias FROM core_actors t1 INNER JOIN alias t2 on t1.actor_id = t2.relevance_id AND t2.type = 1 AND t2.classify = 'WEIBO_SEARCH';", nativeQuery = true)
    List<Object[]> findWeiboActorKeyWords();
    @Query(value = "select * from alias al where al.`classify` = 'WEIBO_INDEX' and al.`category` != 'ACTOR' UNION ALL select al.* from alias al  join `core_actors` ca on al.`alias` = ca.`actor_name` and al.`category` = 'ACTOR' and al.`classify` = 'WEIBO_INDEX';", nativeQuery = true)
    List<Alias> findWeiboIndexKeywords();
}
