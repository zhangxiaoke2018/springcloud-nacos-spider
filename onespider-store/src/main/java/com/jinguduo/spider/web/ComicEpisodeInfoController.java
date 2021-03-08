package com.jinguduo.spider.web;

import com.jinguduo.spider.data.table.ComicEpisodeInfo;
import com.jinguduo.spider.service.ComicEpisodeInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @DATE 04/08/2017 14:05
 */
@RestController
@RequestMapping("/comic_episode_info")
public class ComicEpisodeInfoController {

    @Autowired
    private ComicEpisodeInfoService service;

    @PostMapping
    public ComicEpisodeInfo saveOrUpdate(@RequestBody ComicEpisodeInfo info) {
        return service.insertOrUpdate(info);

    }

}
