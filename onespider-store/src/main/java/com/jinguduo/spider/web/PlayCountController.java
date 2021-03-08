package com.jinguduo.spider.web;

import java.sql.Timestamp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jinguduo.spider.data.table.PlayCountHour;
import com.jinguduo.spider.service.PlayCountHourService;

/**
 * 
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @author liuxinglong
 * @DATE 2017年7月28日 下午4:32:39
 *
 */
@RestController
@RequestMapping("playcount")
public class PlayCountController {

    @Autowired
    private PlayCountHourService playCountHourService;

    @RequestMapping(value = "hours", method = RequestMethod.GET)
    public Object getPlayCountHour(@RequestParam Integer showId, @RequestParam String startDate, @RequestParam String endDate) {
        return playCountHourService.getPlayCountHour(showId,startDate,endDate);
    }
    
    @RequestMapping(value = "add", method = RequestMethod.GET)
    public void addPlayCountHour(
            @RequestParam(value = "playCount") Long playCount,
            @RequestParam(value = "showId") Integer showId,
            @RequestParam(value = "category") String category,
            @RequestParam(value = "platformId") Integer platformId,
            @RequestParam(value = "crawledAt") Long crawledAt) {
        
        PlayCountHour pc = new PlayCountHour(Long.valueOf(playCount));
        pc.setShowId(showId);
        pc.setCategory(category);
        pc.setPlatformId(platformId);
        pc.setCrawledAt(new Timestamp(Long.valueOf(crawledAt)));
        pc.setFailure(Boolean.TRUE);
        playCountHourService.insertOrUpdate(pc);
    }
}
