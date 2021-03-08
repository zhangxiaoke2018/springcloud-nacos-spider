package com.jinguduo.spider.web;


import com.jinguduo.spider.data.table.*;
import com.jinguduo.spider.service.BilibiliSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @DATE 27/07/2017 13:42
 */
@RestController
@RequestMapping("/bilibili/search")
public class BilibiliSearchController {

    @Autowired
    private BilibiliSearchService bilibiliSearchService;

    @PostMapping("/click")
    public Object saveClick(@RequestBody BilibiliVideoClick click){

        return bilibiliSearchService.saveClick(click);
    }

    @PostMapping("/dm")
    public Object saveDm(@RequestBody BilibiliVideoDm dm){

        return bilibiliSearchService.saveDm(dm);
    }

    @PostMapping("/stow")
    public Object saveStow(@RequestBody BilibiliVideoStow stow){

        return bilibiliSearchService.saveStow(stow);
    }

    @PostMapping("/count")
    public Object saveCount(@RequestBody BilibiliVideoCount count){

        return bilibiliSearchService.saveCount(count);
    }

    @PostMapping("/score")
    public Object saveScore(@RequestBody BilibiliVideoScore score){
        return bilibiliSearchService.insertOrUpdated(score);
    }

    @PostMapping("/fans")
    public Object saveFans(@RequestBody BilibiliFansCount fans){
        return bilibiliSearchService.saveFans(fans);
    }

}
