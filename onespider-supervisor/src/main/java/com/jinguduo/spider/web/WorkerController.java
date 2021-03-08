package com.jinguduo.spider.web;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jinguduo.spider.common.constant.WorkerCommand;
import com.jinguduo.spider.common.util.Paginator;
import com.jinguduo.spider.worker.Worker;
import com.jinguduo.spider.worker.WorkerManager;

@RestController
public class WorkerController {

    @Autowired
    private WorkerManager workerManager;
    
    @GetMapping(value = "/workers")
    public Paginator<Worker> doList(
            @RequestParam(value = "domain", required = false) String domain,
            @RequestParam(name = "page", defaultValue = "1", required = false) Integer page,
            @RequestParam(name = "size", defaultValue = "100", required = false) Integer size) {
        
        Collection<Worker> workers = null;
        if (StringUtils.hasText(domain)) {
            Worker[] ws = workerManager.getActivedWorkersByDomain(domain);
            if (ws != null) {
                workers = Arrays.asList(ws);
            }
        } else {
            workers = workerManager.getAllActivedWorkers();
        }
        if (workers == null) {
            workers = new ArrayList<>();
        }
        Paginator<Worker> p = new Paginator<>(1, workers.size());
        p.setEntites(workers);
        
        return p;
    }
 
    @PostMapping(value = "/workers")
    public Worker doPost(
            @RequestParam(value = "uuid") String uuid,
            @RequestParam(value = "command") WorkerCommand command) {
        
        Worker worker = workerManager.getActivedWorkerByUuid(uuid);
        if (worker != null) {
            worker.setCommand(command);
        } 
        
        return worker;
    }
}
