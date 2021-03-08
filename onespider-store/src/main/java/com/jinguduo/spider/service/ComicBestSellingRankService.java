package com.jinguduo.spider.service;

import com.jinguduo.spider.data.table.ComicBestSellingRank;
import com.jinguduo.spider.db.repo.ComicBestSellingRankRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by lc on 2019/4/17
 */
@Service
public class ComicBestSellingRankService {

    @Autowired
    ComicBestSellingRankRepo repo;

    public ComicBestSellingRank save(ComicBestSellingRank cbsr) {
        if (null == cbsr.getPlatformId() || null == cbsr.getDay() || null == cbsr.getRank() || null == cbsr.getCode()) {
            return cbsr;
        }

        ComicBestSellingRank old = repo.findByPlatformIdAndDayAndRank(cbsr.getPlatformId(), cbsr.getDay(), cbsr.getRank());

        //update
        if (null != old) {
            old.setCode(cbsr.getCode());
            old.setName(cbsr.getName());
            old.setRise(cbsr.getRise());
            old.setRiseStatus(cbsr.getRiseStatus());
            repo.save(old);
        } else {
            //insert
            repo.save(cbsr);
        }
        return cbsr;
    }


}
