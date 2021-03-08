package com.jinguduo.spider.service;

import com.jinguduo.spider.data.table.ComicAuthorRelation;
import com.jinguduo.spider.db.repo.ComicAuthorRelationRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by lc on 2019/4/18
 */
@Service
public class ComicAuthorRelationService {
    @Autowired
    ComicAuthorRelationRepo repo;


    public ComicAuthorRelation save(ComicAuthorRelation car) {

        ComicAuthorRelation old = repo.findByComicCodeAndAuthorId(car.getComicCode(), car.getAuthorId());
        if (null != old) {
            //人工设置不更新
            if (1 == old.getStatus()) {
                return car;
            }
            old.setAuthorName(car.getAuthorName());
            repo.save(old);
        } else {
            repo.save(car);
        }
        return car;

    }
}
