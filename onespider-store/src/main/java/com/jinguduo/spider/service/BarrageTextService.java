package com.jinguduo.spider.service;

import java.io.IOException;
import java.util.List;

import com.jinguduo.spider.store.DanmakuCsvFileWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jinguduo.spider.data.text.BarrageText;
import com.jinguduo.spider.store.CsvFileWriter;

@Service
public class BarrageTextService {
    
    @Autowired
    private DanmakuCsvFileWriter csvFileWriter;

    public void save(List<BarrageText> barrageTexts) throws IOException {
        if(barrageTexts == null || barrageTexts.isEmpty()){
            return;
        }

        csvFileWriter.write(barrageTexts);
    }
}
