package com.jinguduo.spider.service;

import com.jinguduo.spider.data.table.ComicKuaiKan;
import com.jinguduo.spider.db.repo.ComicKuaiKanRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.jinguduo.spider.service.ComicService.skipByCode;

/**
 * I'm a cute code dog
 * User: LingChen
 * Date:2017/7/31
 * Time:18:45
 */
@Service
@Slf4j
public class ComicKuaiKanService {
    @Autowired
    private ComicKuaiKanRepo comicKuaiKanRepo;

    public ComicKuaiKan save(ComicKuaiKan logs) {
        if (null == logs) {
            log.error("***save ComicKuaiKan error ,because ComicKuaiKan is null ***");
            return null;
        }
        boolean isSkip = skipByCode(logs.getCode());
        if (isSkip) {
            return logs;
        }
        return comicKuaiKanRepo.save(logs);
    }
}
