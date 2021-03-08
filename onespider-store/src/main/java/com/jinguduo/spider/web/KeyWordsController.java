package com.jinguduo.spider.web;

import java.util.List;

import lombok.extern.apachecommons.CommonsLog;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.jinguduo.spider.data.table.Keywords;
import com.jinguduo.spider.service.KeywordsService;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 06/12/2016 3:03 PM
 */
@RestController
@RequestMapping("/keyword")
@CommonsLog
public class KeyWordsController {

    @Autowired
    private KeywordsService keywordsService;

    @RequestMapping(value = "/all",method = RequestMethod.GET)
    public List findAll(){

        return keywordsService.findAll();
    }

    @RequestMapping("/linked_id/{linkedId}")
    public List<Keywords> find(@PathVariable Integer linkedId){

        return keywordsService.findByLinkedId(linkedId);
    }

    @RequestMapping(value = "",method = RequestMethod.POST)
    public Object addKeyword(@RequestBody Keywords keywords){

        return keywordsService.insertOrUpdate(keywords);
    }

    @RequestMapping(value = "/del/id/{id}/linked_id/{linkedId}",method = RequestMethod.DELETE)
    public Object del(@PathVariable Integer id, @PathVariable Integer linkedId){

        try {
            keywordsService.del(id, linkedId);
        }catch (Exception ex){
            log.error(ex.getMessage(),ex);
            return "";
        }
        return "SUCCESS";
    }

}
