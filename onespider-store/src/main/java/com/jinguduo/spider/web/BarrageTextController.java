package com.jinguduo.spider.web;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jinguduo.spider.data.text.BarrageText;
import com.jinguduo.spider.service.BarrageTextService;

import lombok.extern.apachecommons.CommonsLog;

@Controller
@ResponseBody
@CommonsLog
public class BarrageTextController {
    
    @Autowired
    private BarrageTextService barrageTextService;

    @RequestMapping(path = "/barrage_texts", method = RequestMethod.POST)
    public boolean post(@RequestBody List<BarrageText> barrageTexts) {
        boolean r = false;
        
        try {
            barrageTextService.save(barrageTexts);
            r = true;
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
        return r;
    }
}
