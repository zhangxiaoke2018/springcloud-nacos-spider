package com.jinguduo.spider.web;

import com.jinguduo.spider.data.table.OpinionWords;
import com.jinguduo.spider.data.table.ShowActors;
import com.jinguduo.spider.service.ShowActorsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @DATE 03/07/2017 17:47
 */
@RestController
@RequestMapping("/show_actors")
public class ShowActorsController {


    @Autowired
    private ShowActorsService showActorsService;

    @PostMapping
    public void insertOrUpdate(@RequestBody ShowActors showActors){

        showActorsService.insertOrUpdate(showActors);

    }

    @GetMapping("/opinion_words")
    public Object toOpinionWords(@RequestParam("linked_id") Integer linkedId){

        List<OpinionWords> opinionWords = showActorsService.findShowActor2OpinionWords(linkedId);
        return opinionWords;

    }



}
