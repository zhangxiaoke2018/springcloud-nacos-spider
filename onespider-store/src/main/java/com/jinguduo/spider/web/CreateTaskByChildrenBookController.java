package com.jinguduo.spider.web;


import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.service.CreateTaskByAliasService;
import com.jinguduo.spider.service.CreateTaskByChildrenBookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lc on 2017/6/15.
 */

@RestController
@RequestMapping("/childrenBookTask")
public class CreateTaskByChildrenBookController {
    @Autowired
    private CreateTaskByChildrenBookService createService;

    @RequestMapping("/{taskName}")
    public List<Job> getChildrenBookJobByTaskName(@PathVariable("taskName") String taskName) {
        List<Job> taskJobs = new ArrayList<>();
        switch (taskName) {
            case "children2douban":
                taskJobs = createService.children2doubanJob();
                break;
            case "children2jianshu":
                taskJobs = createService.children2jianshuJob();
                break;
            case "douban2douban":
                taskJobs = createService.douban2doubanJob();
                break;
            case "children2weibo":
                taskJobs = createService.children2weiboJob();
                break;
            case "children2wechat":
                taskJobs = createService.children2wechatJob();
                break;
            case "children2jingdong":
                taskJobs = createService.children2jingdongJob();
                break;
            default:
                break;
        }
        return taskJobs;
    }


}
