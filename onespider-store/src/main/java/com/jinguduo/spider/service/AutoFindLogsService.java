package com.jinguduo.spider.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jinguduo.spider.data.table.AutoFindLogs;
import com.jinguduo.spider.data.table.Show;
import com.jinguduo.spider.db.repo.AutoFindLogsRepo;
import com.jinguduo.spider.db.repo.ShowRepo;

@Service
public class AutoFindLogsService {

    @Autowired
    private AutoFindLogsRepo autoFindLogsRepo;
    
    @Autowired
    private ShowRepo showRepo;
    
    public void save(AutoFindLogs o){
        if (o == null ||StringUtils.isBlank(o.getCode()) || StringUtils.isBlank(o.getUrl())) {
            return;
        }
        Show old = showRepo.findFirstByUrlAndDepthOrderById(o.getUrl(), 1);
        if (old == null) {
            old = showRepo.findFirstByCodeAndDepthOrderById(o.getCode(), 1);
        }
        if (old != null && !old.getDeleted()) {
            o.setExistJobStatus(1);
            o.setExistJobId(old.getId());
            o.setExistJobChecked(old.getCheckedStatus());
            o.setExistJobTime(old.getCreatedAt());
        }else{
            o.setExistJobStatus(0);
        }
        autoFindLogsRepo.save(o);
    }
}
