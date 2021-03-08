package com.jinguduo.spider.db.repo;

import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import com.jinguduo.spider.data.table.GdiActorLogs;

@Component
public interface GdiActorLogsRepo extends JpaRepository<GdiActorLogs, Integer> {

    GdiActorLogs findByLinkedIdAndDay(Integer linkedId,Date day);
}
