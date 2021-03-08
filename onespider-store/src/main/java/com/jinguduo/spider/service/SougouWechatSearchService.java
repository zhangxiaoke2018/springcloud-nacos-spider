package com.jinguduo.spider.service;

import java.io.IOException;

import com.jinguduo.spider.common.util.TextUtils;
import com.jinguduo.spider.data.table.WechatSearchText;
import com.jinguduo.spider.db.repo.WechatSearchTextRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jinguduo.spider.data.table.SougouWechatSearchText;
import com.jinguduo.spider.store.WechatSearchCsvFileWriter;

/**
 * I'm a cute code dog
 * User: LingChen
 * Date:2017/6/28
 * Time:14:51
 */
@Slf4j
@Service
public class SougouWechatSearchService {

    @Autowired
    private WechatSearchCsvFileWriter wechatSearchCsvFileWriter;

    @Autowired
    private WechatSearchTextRepo wechatSearchTextRepo;

    public SougouWechatSearchText save(SougouWechatSearchText text) throws IOException {
        if (text == null) {
            return null;
        }
        wechatSearchCsvFileWriter.write(text);

        try {
            // 落库处理
            WechatSearchText wst = new WechatSearchText();
            wst.setArticleTime(text.getArticleTime());
            wst.setAuthor(text.getAuthor());
            wst.setTitle(TextUtils.removeEmoji(text.getTitle()));
            wst.setSummary(TextUtils.removeEmoji(text.getSummary()));
            wst.setUrl(text.getUrl());
            wst.setSort(text.getCompositor());

            WechatSearchText had = wechatSearchTextRepo.findByAuthorAndArticleTime(wst.getAuthor(), wst.getArticleTime());
            if(had == null) {
                wechatSearchTextRepo.save(wst);
            }

        }catch (Exception ex){
            log.error("wechat_search_text save error:" + ex.getMessage());
        }


        return text;
    }
}
