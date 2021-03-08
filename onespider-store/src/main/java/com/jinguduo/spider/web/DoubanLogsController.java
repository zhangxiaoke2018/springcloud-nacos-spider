package com.jinguduo.spider.web;

import com.jinguduo.spider.data.table.DoubanCommentsText;
import com.jinguduo.spider.data.table.DoubanLog;
import com.jinguduo.spider.service.DoubanLogsService;
import com.jinguduo.spider.service.DoubanTextService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.io.IOException;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 31/03/2017 15:27
 */
@RestController
@RequestMapping("/douban_log")
public class DoubanLogsController {

    @Autowired
    private DoubanLogsService doubanLogsService;

    @Autowired
    private DoubanTextService textService;

    @PostMapping
    public DoubanLog insertOrUpdate(@RequestBody DoubanLog doubanLog){

        return doubanLogsService.insertOrUpdate(doubanLog);

    }

    @PostMapping(value = "/comment")
    public Integer addComment(@RequestBody DoubanCommentsText doubanCommentsText) throws IOException {

        return textService.fileWriter(doubanCommentsText);
    }

}
