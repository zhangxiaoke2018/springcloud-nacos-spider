package com.jinguduo.spider.service;

import com.jinguduo.spider.data.table.DoubanCommentsText;
import com.jinguduo.spider.store.CsvFileWriter;
import com.jinguduo.spider.store.DoubanCsvFileWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
public class DoubanTextService {

    @Autowired
    private DoubanCsvFileWriter csvFileWriter;

    public int fileWriter(List<DoubanCommentsText> commentTexts) throws IOException {
        int c = 0;
        if(commentTexts == null || commentTexts.isEmpty()){
            return 0;
        }
        try {
            c = commentTexts.size();
            csvFileWriter.write(commentTexts);
        } catch (Exception e) {
            c = 0;
            log.error(e.getMessage(), e);
        }
        return c;
    }

    public int fileWriter(DoubanCommentsText commentTexts) throws IOException {
        int c = 1;
        if(commentTexts == null){
            return 0;
        }
        try {
            csvFileWriter.write(commentTexts);
        } catch (Exception e) {
            c = 0;
            log.error(e.getMessage(), e);
        }
        return c;
    }
}
