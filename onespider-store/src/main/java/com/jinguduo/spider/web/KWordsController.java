package com.jinguduo.spider.web;

import com.jinguduo.spider.data.table.KWords;
import com.jinguduo.spider.service.KWordsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Deprecated
@RestController
public class KWordsController {

    @Autowired
    private KWordsService kWordsService;

    @RequestMapping(value = "addKWord",method = RequestMethod.POST)
    public KWords addKWords(@RequestBody KWords kWord){
        return kWordsService.insertKWord(kWord);
    }
}
