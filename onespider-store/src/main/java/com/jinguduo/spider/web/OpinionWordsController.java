package com.jinguduo.spider.web;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jinguduo.spider.data.table.OpinionWords;
import com.jinguduo.spider.service.OpinionWordsService;

import lombok.extern.apachecommons.CommonsLog;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 24/03/2017 15:30
 */
@CommonsLog
@RestController
@RequestMapping("/opinion_words")
public class OpinionWordsController {

    @Autowired
    private OpinionWordsService opinionWordsService;

    @GetMapping("/all")
    public Object findAll(@RequestParam(required = false) String ids, @RequestParam(required = false) String categories){
        List<OpinionWords> resp = null;

        if (StringUtils.isBlank(ids) && StringUtils.isBlank(categories)){
            resp =  opinionWordsService.findAll();
        }else if (StringUtils.isNotBlank(ids) && StringUtils.isBlank(categories)) { // 过滤 id
            List id = Lists.newArrayList(ids.split(","));
            resp = opinionWordsService.findAll().stream().filter(o -> id.contains(o.getId().toString())).collect(Collectors.toList());
        }else if (StringUtils.isBlank(ids) && StringUtils.isNotBlank(categories)){ // 过滤 category
            List category = Lists.newArrayList(categories.split(","));
            resp = opinionWordsService.findAll().stream().filter(o -> category.contains(o.getCategory())).collect(Collectors.toList());
        }else { // 两个参数都存在
            List id = Lists.newArrayList(ids.split(","));
            List category = Lists.newArrayList(categories.split(","));
            resp = opinionWordsService.findAll().stream().filter(o -> category.contains(o.getCategory())).filter(o -> id.contains(o.getId().toString())).collect(Collectors.toList());
        }
        return resp;
    }

    @GetMapping("/feature")
    public Object findFeature(){
        return opinionWordsService.findFeature();
    }

    @GetMapping("/new")
    public Object newOpinionWords(@RequestParam String time){

        try {
            Date date = DateUtils.parseDate(time, "yyyy-MM-ddHH:mm:ss");

            return opinionWordsService.findNew(new Timestamp(date.getTime()));

        } catch (ParseException e) {
            log.error(e.getMessage(), e);
        }
        return Collections.EMPTY_LIST;
    }

    @GetMapping("/linked_id/{linkedId}")
    public Object findByLinkedId(@PathVariable Integer linkedId ){

        List<OpinionWords> opinionWordses = opinionWordsService.findByLinkedId(linkedId);

        return opinionWordses;
    }

    @GetMapping("/id/{id}")
    public Object findById(@PathVariable Integer id){

        OpinionWords opinionWords = opinionWordsService.findById(id);

        return opinionWords;
    }

    @PostMapping
    public Object insertOrUpdate(@RequestBody @Valid OpinionWords opinionWords){

        OpinionWords words = opinionWordsService.insertOrUpdate(opinionWords);
        return words;
    }

    @DeleteMapping("/id/{id}")
    public String del(@PathVariable Integer id){
        try {
            opinionWordsService.del(id);
        }catch (Exception ex){
            ex.printStackTrace();
            return "FAILURE";
        }
        return "SUCCESS";
    }
}
