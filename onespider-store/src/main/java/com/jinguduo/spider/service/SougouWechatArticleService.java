package com.jinguduo.spider.service;

import com.jinguduo.spider.data.table.SougouWechatArticleText;
import com.jinguduo.spider.store.WechatArticleCsvFileWriter;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * I'm a cute code dog
 * User: LingChen
 * Date:2017/6/28
 * Time:14:51
 */
@Service
@CommonsLog
public class SougouWechatArticleService {
    @Autowired
    private WechatArticleCsvFileWriter csvFileWriter;

    public SougouWechatArticleText save(SougouWechatArticleText text) throws IOException {
        if (text == null) {
            return null;
        }
        csvFileWriter.write(text);
        return text;
    }
}
