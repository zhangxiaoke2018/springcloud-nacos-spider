package com.jinguduo.spider.web;

import com.jinguduo.spider.data.table.ComicU17;
import com.jinguduo.spider.service.ComicU17Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @DATE 01/08/2017 09:32
 */
@RestController
@RequestMapping("/comic_u17")
public class ComicU17Controller {

    @Autowired
    private ComicU17Service comicU17Service;

    @PostMapping
    public ComicU17 save(@RequestBody ComicU17 comicU17){

        ComicU17 u17 = comicU17Service.insertOrUpdate(comicU17);

        return u17;
    }


}
