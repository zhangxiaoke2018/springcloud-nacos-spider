package com.jinguduo.spider.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jinguduo.spider.common.constant.SpiderStatus;
import com.jinguduo.spider.common.constant.WorkerCommand;
import com.jinguduo.spider.worker.WorkerTracker;

@Controller
@ResponseBody
public class WorkerHeartbeatController {

    @Autowired
    private WorkerTracker workerTracker;

    @RequestMapping(path = "/worker/heartbeat", method = RequestMethod.GET)
    public WorkerCommand heartbeat(
            @RequestParam(name = "hostname") String hostname,
            @RequestParam(name = "uuid") String uuid,
            @RequestParam(name = "domain") String domain,
            @RequestParam(name = "status") SpiderStatus status,
            @RequestParam(name = "command") WorkerCommand command) {

        WorkerCommand newCommand = workerTracker.hearbeat(hostname, uuid, domain, status, command);

        return newCommand;

    }

}
