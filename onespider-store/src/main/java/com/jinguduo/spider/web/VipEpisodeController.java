package com.jinguduo.spider.web;

import com.jinguduo.spider.data.table.VipEpisode;
import com.jinguduo.spider.service.VipEpisodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by lc on 2017/5/5.
 */
@RestController
@RequestMapping("/vip_episode")
public class VipEpisodeController {

    @Autowired
    private VipEpisodeService vipService;

    @PostMapping
    public VipEpisode insertOrUpdate(@RequestBody VipEpisode vip) {
        return vipService.save(vip);
    }
}
