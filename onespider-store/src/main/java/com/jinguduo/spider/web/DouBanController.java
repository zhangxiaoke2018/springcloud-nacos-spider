package com.jinguduo.spider.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jinguduo.spider.data.table.DouBanActor;
import com.jinguduo.spider.data.table.DouBanShow;
import com.jinguduo.spider.service.DouBanService;

/**
 * Created by csonezp on 2016/8/15.
 */
@RestController
public class DouBanController {

    @Autowired
    DouBanService douBanService;

    @RequestMapping(path = "/doubanshow", method = RequestMethod.POST)
    public String post(@RequestBody DouBanShow douBanShow) {
        douBanService.addShow(douBanShow);
        return "success";
    }

    @RequestMapping(path = "/doubanactor", method = RequestMethod.POST)
    public String postActor(@RequestBody DouBanActor douBanActor) {
        douBanService.addOrUpdateActor(douBanActor);
        return "success";
    }

    @RequestMapping(path = "/douban/getactorshow", method = RequestMethod.GET)
    public Object checkActorShow(@RequestParam("id") Integer actorId) {
        return douBanService.getActorPlaydShow(actorId);
    }

    @RequestMapping(path = "/douban/guessactor", method = RequestMethod.GET)
    public Object guessActorByName(@RequestParam("name") String name) {
        return douBanService.guessActorName(name);
    }

    @RequestMapping(path = "/douban/getShows", method = RequestMethod.POST)
    public Object getShows(int page, int size) {
        return douBanService.getShows(page, size);
    }

    @RequestMapping(path = "/douban/getactors", method = RequestMethod.POST)
    public Object getActors(int page, int size) {
        return douBanService.getActors(page, size);
    }
}
