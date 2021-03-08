package com.jinguduo.spider.service;


import com.jinguduo.spider.data.table.ComicCommentText;
import com.jinguduo.spider.db.repo.ComicCommentTextRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ComicCommentTextService {
    @Autowired
    private ComicCommentTextRepo comicCommentTextRepo;

    public ComicCommentText insertOrUpdate(ComicCommentText c){
        ComicCommentText old = comicCommentTextRepo.findByCommentId(c.getCommentId());
        if(null != old){
            old.setContent(c.getContent());
            old.setCommentCreateTime(c.getCommentCreateTime());
            old.setPlatformId(c.getPlatformId());
            old.setRevertCount(c.getRevertCount());
            old.setSupportCount(c.getSupportCount());
            old.setUserId(c.getUserId());
            old.setUserName(c.getUserName());
            comicCommentTextRepo.save(old);
        }else{
            return comicCommentTextRepo.save(c);
        }
        return null;
    }

}
