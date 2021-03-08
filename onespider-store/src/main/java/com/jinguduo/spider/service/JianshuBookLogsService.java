package com.jinguduo.spider.service;

import com.jinguduo.spider.data.table.bookProject.JianshuBookLogs;
import com.jinguduo.spider.db.repo.JianshuBookLogsRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by lc on 2020/1/17
 */
@Service
public class JianshuBookLogsService {

    @Autowired
    private JianshuBookLogsRepo repo;

    public JianshuBookLogs saveOrUpdate(JianshuBookLogs logs) {

        JianshuBookLogs old = repo.findByKeywordAndCode(logs.getKeyword(), logs.getCode());

        if (null == old) {
            return repo.save(logs);
        }

        if (null != logs.getLikes()) {
            old.setLikes(logs.getLikes());
        }
        if (null != logs.getViews()) {
            old.setViews(logs.getViews());
        }
        if (null != logs.getComments()) {
            old.setComments(logs.getComments());
        }
        if (null!= logs.getShareTime()){
            old.setShareTime(logs.getShareTime());
        }

        return repo.save(old);

    }

}
