package com.jinguduo.spider.service;

import com.jinguduo.spider.common.util.TextUtils;
import com.jinguduo.spider.data.table.WeiboText;
import com.jinguduo.spider.db.repo.WeiboTextRepo;
import com.jinguduo.spider.store.WeiboTextCsvFileWriter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
public class WeiboTextService {

    @Autowired
    private WeiboTextCsvFileWriter weiboTextCsvFileWriter;

    @Autowired
    private WeiboTextRepo weiboTextRepo;


    public void save(WeiboText weiboText) throws IOException {

        if (weiboText == null || StringUtils.isBlank(weiboText.getMid())) {
            return;
        }
        //去除过长的文本
        if (!StringUtils.isBlank(weiboText.getContent()) && weiboText.getContent().length() > 500) {
            return;
        }

        if(!StringUtils.isBlank(weiboText.getOriginalContent()) && weiboText.getOriginalContent().length() > 500){
            return;
        }

        //去除表情
        weiboText.setContent(TextUtils.removeEmoji(weiboText.getContent()));
        weiboText.setPostPlatform(TextUtils.removeEmoji(weiboText.getPostPlatform()));
        weiboText.setOriginalContent(TextUtils.removeEmoji(weiboText.getOriginalContent()));
        weiboText.setOriginalPostPlatform(TextUtils.removeEmoji(weiboText.getOriginalPostPlatform()));
        try {
            Integer one = weiboTextRepo.findIdByMidAndCode(weiboText.getMid(), weiboText.getCode());
            if (one != null) {
                weiboText.setId(one);
                weiboTextRepo.save(weiboText);
            } else {
                weiboTextRepo.save(weiboText);
            }
        } catch (Exception e) {
            log.error("weibo save error ,this error is ->{},this pojo is ->{}", e.getMessage(), weiboText);
            e.printStackTrace();
        }

        //  weiboTextCsvFileWriter.write(weiboText);
    }

}
