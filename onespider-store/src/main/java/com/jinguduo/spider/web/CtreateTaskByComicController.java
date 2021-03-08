package com.jinguduo.spider.web;

import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.service.CreateTaskByComicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lc on 2019/1/14
 */
@RestController
@RequestMapping("comicTask")
public class CtreateTaskByComicController {
    @Autowired
    CreateTaskByComicService createService;

    @RequestMapping("/{taskName}")
    public List<Job> getJobByTaskName(@PathVariable("taskName") String taskName) {
        List<Job> taskJobs = new ArrayList<>();
        switch (taskName) {
            case "kuaikan":
                taskJobs = createService.createKuaikanTask();
                break;
            case "xiaomingtaiji":
                taskJobs = createService.createXiaoMingTaiJiTask();
                break;
            case "tengxun":
                taskJobs = createService.createTengXunTask();
                break;
//            case "u17":
//                taskJobs = createService.createU17Task();
//                break;
            case "weibo":
                taskJobs = createService.createWeiboTask();
                break;
            case "bodong":
                taskJobs = createService.createBodongTask();
                break;
            case "mmmh":
                taskJobs = createService.createMmmhTask();
                break;
           /* case "wangyi":
                taskJobs = createService.createWangYiTask();
                break;*/
            default:
                break;
        }
        return taskJobs;


    }

}
