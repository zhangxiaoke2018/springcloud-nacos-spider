package com.jinguduo.spider.service;

import com.jinguduo.spider.data.table.bookProject.ChildrenBookComment;
import com.jinguduo.spider.db.repo.ChildrenBookCommentRepo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by lc on 2019/12/4
 */
@Service
public class ChildrenBookCommentService {
    @Autowired
    private ChildrenBookCommentRepo repo;
    public ChildrenBookComment saveOrUpdate(ChildrenBookComment cbc) {
        if (StringUtils.isEmpty(cbc.getCode()) || cbc.getPlatformId()==null){
            return cbc;
        }

        ChildrenBookComment old = repo.findByPlatformIdAndCodeAndDay(cbc.getPlatformId(), cbc.getCode(), cbc.getDay());
        if (null == old){
            return repo.save(cbc);
        }

        if (null != cbc.getCommentCount()){
            old.setCommentCount(cbc.getCommentCount());
        }
        if (null!= cbc.getGreatCount()){
            old.setGreatCount(cbc.getGreatCount());
        }
        if (null!=cbc.getIndifferentCount()){
            old.setIndifferentCount(cbc.getIndifferentCount());
        }
        if (null!= cbc.getDetestCount()){
            old.setDetestCount(cbc.getDetestCount());
        }
        if (null!= cbc.getGoodRate()){
            old.setGoodRate(cbc.getGoodRate());
        }

        return repo.save(old);
    }
}
