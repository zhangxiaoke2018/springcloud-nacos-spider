package com.jinguduo.spider.db.repo;

import com.jinguduo.spider.data.table.ComicAuthorRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by lc on 2019/4/18
 */
@Repository
public interface ComicAuthorRelationRepo extends JpaRepository<ComicAuthorRelation, Integer> {

    ComicAuthorRelation findByComicCodeAndAuthorId(String comicCode,String authorId);
}
