package com.jinguduo.spider.web;


import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.service.CreateTaskByAliasService;

/**
 * Created by lc on 2017/6/15.
 */

@RestController
@RequestMapping("createTask")
public class CreateTaskByAliasController {
    @Autowired
    private CreateTaskByAliasService createService;

    @RequestMapping("/{taskName}")
    public List<Job> getJobByTaskName(@PathVariable("taskName") String taskName) {
        List<Job> taskJobs = new ArrayList<>();
        switch (taskName) {
            case "index360":
                taskJobs = createService.createIndex360Task();
                break;
            case "media360":
                taskJobs = createService.createMedia360Task();
                break;
            case "customer360":
                taskJobs = createService.createCustomer360Task();
                break;
            case "toutiao":
                taskJobs = createService.createToutiaoTask();
                break;
            case "weiboIndex":
                taskJobs = createService.createWeiboIndexTask();
                break;
            case "baiduVideo":
                taskJobs = createService.createBaiduVideoTask();
                break;
            case "news360":
                taskJobs = createService.createNews360Task();
                break;
            case "baiduTieba":
                taskJobs = createService.createBaiduTiebaTask();
                break;
            case "baiduNews":
                taskJobs = createService.createBaiduNewsTask();
                break;
            case "sougouWechatSearch":
                taskJobs = createService.createSougouWechatSearchTask();
                break;
            case "weiboSearch":
                taskJobs = createService.createWeiboSearchTask();
                break;
            case "bilibiliSearch":
                taskJobs = createService.createBilibiliSearchTask();
                break;
            case "coreKeyword":
                taskJobs = createService.createCoreTask();
                break;
            case "wechatArticle":
                taskJobs = createService.createWechatArticleTask();
                break;
            case "newBaiduNews":
                taskJobs = createService.createNewBaiduNewsTask();
                break;
            default:
                break;
        }
        return taskJobs;
    }


}
