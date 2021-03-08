package com.jinguduo.spider.service;

import java.io.IOException;
import java.util.List;

import com.jinguduo.spider.data.text.CommentText;
import com.jinguduo.spider.store.CommentCsvFileWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jinguduo.spider.store.CsvFileWriter;

@Component
public class CommentTextService {

    @Autowired
    private CommentCsvFileWriter csvFileWriter;

    public void save(List<CommentText> commentTexts) throws IOException {
        if(commentTexts == null || commentTexts.isEmpty()){
            return;
        }
        csvFileWriter.write(commentTexts);
    }
}
