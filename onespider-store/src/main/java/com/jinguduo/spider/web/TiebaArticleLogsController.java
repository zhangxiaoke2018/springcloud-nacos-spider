package com.jinguduo.spider.web;

import com.jinguduo.spider.data.table.TiebaArticleLogs;
import com.jinguduo.spider.service.TiebaArticleLogsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 21/04/2017 13:25
 */
@RestController
@RequestMapping("/tieba_article_logs")
public class TiebaArticleLogsController {

    @Autowired
    private TiebaArticleLogsService tiebaArticleLogsService;

    @PostMapping
    public TiebaArticleLogs insertOrUpdate(@RequestBody TiebaArticleLogs tiebaArticleLogs){

        return tiebaArticleLogsService.insertOrUpdate(tiebaArticleLogs);
    }




}
