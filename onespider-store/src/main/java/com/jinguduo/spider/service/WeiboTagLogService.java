package com.jinguduo.spider.service;

import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jinguduo.spider.data.table.WeiboTagLog;
import com.jinguduo.spider.db.repo.WeiboTagLogRepo;

import java.util.List;

/**
 * Created by gsw on 2017/1/5.
 */
@Service
public class WeiboTagLogService {

    @Autowired
    WeiboTagLogRepo weiboTagLogRepo;

    public WeiboTagLog insert(WeiboTagLog weiboTagLog) {

        //以下关键词 不存
        List list = Lists.newArrayList("明星势力榜","中国电视剧品质盛典","秒拍","微博","亚洲新歌榜");
        if(list.contains(weiboTagLog.getKeyword())){
            return null;
        }
        return this.weiboTagLogRepo.save(weiboTagLog);
    }
}
