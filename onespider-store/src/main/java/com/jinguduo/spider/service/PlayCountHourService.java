package com.jinguduo.spider.service;

import java.sql.Timestamp;

import org.apache.commons.lang3.time.DateUtils;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jinguduo.spider.data.table.PlayCountHour;
import com.jinguduo.spider.db.repo.PlayCountHourRepo;

/**
 * 
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @author liuxinglong
 * @DATE 2017年7月28日 下午4:36:19
 *
 */
@Service
public class PlayCountHourService {
    @Autowired
    private PlayCountHourRepo playCountHourRepo;

    public Object getPlayCountHour(Integer showId, String startDate, String endDate) {
        try{
            Timestamp s = new Timestamp(DateUtils.parseDate(startDate + " 00:00:00", "yyyy-MM-dd HH:mm:ss").getTime()  - 1000L);
            Timestamp e = new Timestamp(DateUtils.parseDate(endDate + " 23:59:59", "yyyy-MM-dd HH:mm:ss").getTime() + 1000L);

            return playCountHourRepo.findByShowIdAndCrawledAtGreaterThanAndCrawledAtLessThan(showId, s, e).orElse(Lists.newArrayList());
        }catch(Exception e){
            return null;
        }

    }
    
    public void insertOrUpdate(PlayCountHour playCount) {
        PlayCountHour pc = playCountHourRepo.findByShowIdAndPlatformIdAndCrawledAt(playCount.getShowId(), playCount.getPlatformId(), playCount.getCrawledAt());

        if(pc != null){
            pc.setPlayCount(playCount.getPlayCount());
            pc.setFailure(Boolean.TRUE);
            playCountHourRepo.save(pc);
        }else {
            playCountHourRepo.save(playCount);
        }
    }
}
