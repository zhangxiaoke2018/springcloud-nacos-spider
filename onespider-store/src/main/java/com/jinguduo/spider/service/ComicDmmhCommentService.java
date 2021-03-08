package com.jinguduo.spider.service;


import com.jinguduo.spider.data.table.ComicDmmhComment;
import com.jinguduo.spider.db.repo.ComicDmmhCommentRepo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ComicDmmhCommentService {


    @Autowired
    private ComicDmmhCommentRepo comicDmmhCommentRepo;

    public ComicDmmhComment insertOrUpdate(ComicDmmhComment comicDmmhComment) {
        if (null == comicDmmhComment
                || StringUtils.isBlank(comicDmmhComment.getCode())) {
            return null;
        }

        ComicDmmhComment s = comicDmmhCommentRepo.save(comicDmmhComment);

        return s;
    }
}
