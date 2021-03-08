package com.jinguduo.spider.db.repo;

import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import com.jinguduo.spider.data.table.GdiShowLogs;

@Component
public interface GdiShowLogsRepo extends JpaRepository<GdiShowLogs, Integer> {

    GdiShowLogs findByLinkedIdAndDay(Integer linkedId,Date day);
}
