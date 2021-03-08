package com.jinguduo.spider.service;

import com.jinguduo.spider.data.table.ComicDmmh;
import com.jinguduo.spider.db.repo.ComicDmmhRepo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static com.jinguduo.spider.service.ComicService.skipByCode;


@Service
@Slf4j
public class ComicDmmhService {
    @Autowired
    private ComicDmmhRepo comicDmmhRepo;

    public ComicDmmh insertOrUpdate(ComicDmmh comicDmmh) {
        if (null == comicDmmh
                || StringUtils.isBlank(comicDmmh.getCode())) {
            return null;
        }

        boolean isSkip = skipByCode(comicDmmh.getCode());
        if (isSkip) {
            return comicDmmh;
        }

        ComicDmmh s = comicDmmhRepo.save(comicDmmh);

        return s;
    }


}
