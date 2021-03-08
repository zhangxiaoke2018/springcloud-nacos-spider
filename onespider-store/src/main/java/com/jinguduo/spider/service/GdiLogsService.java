package com.jinguduo.spider.service;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jinguduo.spider.data.table.GdiActorLogs;
import com.jinguduo.spider.data.table.GdiShowLogs;
import com.jinguduo.spider.db.repo.GdiActorLogsRepo;
import com.jinguduo.spider.db.repo.GdiShowLogsRepo;

/**
 * 
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @author liuxinglong
 * @DATE 2017年7月11日 下午5:32:34
 *
 */
@Service
public class GdiLogsService {

    @Autowired
    private GdiShowLogsRepo gdiShowLogsRepo;
    
    @Autowired
    private GdiActorLogsRepo gdiActorLogsRepo;
    
    public GdiShowLogs getGdiShowLogsByUk(Integer linkedId,Date day){
        return gdiShowLogsRepo.findByLinkedIdAndDay(linkedId, day);
    }
    
    public GdiActorLogs getGdiActorLogsByUk(Integer linkedId,Date day){
        return gdiActorLogsRepo.findByLinkedIdAndDay(linkedId, day);
    }

}
