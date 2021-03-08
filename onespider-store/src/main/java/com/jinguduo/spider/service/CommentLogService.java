package com.jinguduo.spider.service;

import com.jinguduo.spider.data.table.CommentLog;
import com.jinguduo.spider.db.repo.CommentLogRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.*;

@Component
public class CommentLogService {

    @Autowired
    private CommentLogRepo commentLogRepo;


    public CommentLog insert(CommentLog commentLog){

        return commentLogRepo.save(commentLog);
    }

    public List<CommentLog> insert(List<CommentLog> commentLogs){

        return commentLogRepo.save(commentLogs);
    }

    public int insertListReturnNum(List<CommentLog> commentLogs){
        List<CommentLog> commentLogs_back = commentLogRepo.save(commentLogs);
        return !commentLogs_back.isEmpty()?commentLogs_back.size():0;
    }

}
