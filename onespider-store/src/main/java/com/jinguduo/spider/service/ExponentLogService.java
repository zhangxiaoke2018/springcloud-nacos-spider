package com.jinguduo.spider.service;

import org.springframework.stereotype.Component;

import com.jinguduo.spider.data.table.ExponentLog;

@Component
public interface ExponentLogService {
    ExponentLog insertOrUpdate(ExponentLog exponentLog);
}
