package com.jinguduo.spider.web;


import com.jinguduo.spider.common.util.PinYinUtil;


import com.jinguduo.spider.data.table.NewsKeyword;
import com.jinguduo.spider.service.NewsKeywordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@ResponseBody
public class NewsKeywordController {
    @Autowired
    private NewsKeywordService newsKeywordService;
    @RequestMapping(value = "news/add",method = RequestMethod.GET)
    public Object add(@RequestParam Integer id,@RequestParam String classify,@RequestParam String keywords , @RequestParam Byte type ){

        String code = PinYinUtil.toPinyin(keywords);
        return newsKeywordService.fetch(id,classify,keywords,type,code);
    }


    @RequestMapping(value = "news/get",method = RequestMethod.GET)
    public List<NewsKeyword> getNews(@RequestParam String keywords){
        return newsKeywordService.get(keywords);
    }


    @RequestMapping(value = "news/del",method = RequestMethod.GET)
    public Object del(@RequestParam Integer id){

         newsKeywordService.delete(id);
         return "";

    }


}
