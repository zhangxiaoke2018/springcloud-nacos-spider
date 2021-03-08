package com.jinguduo.spider.web;


import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.service.CreateTaskByANewsService;
import com.jinguduo.spider.service.CreateTaskByAliasService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gaozl on 2020/10/14.
 */

@RestController
@RequestMapping("createNewsTask")
public class CreateTaskByNewsController {
    @Autowired
    private CreateTaskByANewsService createService;

    @RequestMapping("/{taskName}")
    public List<Job> getJobByTaskName(@PathVariable("taskName") String taskName) {
        List<Job> taskJobs = new ArrayList<>();
        switch (taskName) {
            case "baidu":
                taskJobs = createService.createBaiduTask();
                break;
            default:
                break;
        }
        return taskJobs;
    }


}
