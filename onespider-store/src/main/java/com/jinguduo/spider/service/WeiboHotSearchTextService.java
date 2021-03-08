package com.jinguduo.spider.service;

import java.io.IOException;

import com.jinguduo.spider.store.WeiboHotSearchTextCsvFileWriter;

import lombok.extern.apachecommons.CommonsLog;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jinguduo.spider.data.text.WeiboHotSearchText;

@Service
@CommonsLog
public class WeiboHotSearchTextService {
    
    @Autowired
    private WeiboHotSearchTextCsvFileWriter weiboHotSearchTextWriter;

    public void save(WeiboHotSearchText hotSearchText){
        if(hotSearchText == null){
        	 log.info("write weibo hotsearch empty");
            return;
        }

        try {
			weiboHotSearchTextWriter.write(hotSearchText);
		} catch (IOException e) {
		   log.info("write weibo hotsearch error, Exception:"+e.getClass().getSimpleName()+" Message:"+e.getMessage());
		}
    }
}
