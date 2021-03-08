package com.jinguduo.spider.service;

import com.jinguduo.spider.data.table.bookProject.ChildrenBookCommentText;
import com.jinguduo.spider.db.repo.ChildrenBookCommentTextRepo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by lc on 2020/5/12
 */
@Service
public class ChildrenBookCommentTextService {

    @Autowired
    private ChildrenBookCommentTextRepo repo;

    public ChildrenBookCommentText saveOrUpdate(ChildrenBookCommentText text) {
        if (StringUtils.isEmpty(text.getCode()) || text.getPlatformId() == null) {
            return text;
        }
        ChildrenBookCommentText old = repo.findByPlatformIdAndCode(text.getPlatformId(), text.getCode());

        if (old == null) {
            return repo.save(text);
        }
        return old;
    }
}
