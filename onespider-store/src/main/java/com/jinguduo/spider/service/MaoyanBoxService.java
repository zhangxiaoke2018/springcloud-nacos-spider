package com.jinguduo.spider.service;

import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.common.util.TextUtils;
import com.jinguduo.spider.data.table.Comic;
import com.jinguduo.spider.data.table.ComicSex;
import com.jinguduo.spider.data.table.MaoyanBox;
import com.jinguduo.spider.data.table.Media360Logs;
import com.jinguduo.spider.db.repo.ComicRepo;
import com.jinguduo.spider.db.repo.MaoyanBoxRepo;
import lombok.extern.apachecommons.CommonsLog;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @DATE 04/08/2017 18:17
 */
@Service
@CommonsLog
public class MaoyanBoxService {

    @Autowired
    private MaoyanBoxRepo maoyanBoxRepo;

    public MaoyanBox insertOrUpdate(MaoyanBox box) {

        MaoyanBox exist = maoyanBoxRepo.findByDayAndMovieId(box.getDay(), box.getMovieId());

        //如果有该数据,跳过
        if (null != exist) {
            return exist;
        } else {
            return maoyanBoxRepo.save(box);
        }

    }
}
