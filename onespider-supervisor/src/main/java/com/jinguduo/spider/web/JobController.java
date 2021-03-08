package com.jinguduo.spider.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.job.JobManager;

import lombok.extern.apachecommons.CommonsLog;

/**
 * Job API
 *
 */
@RestController
@CommonsLog
public class JobController {
	
	@Autowired
    private JobManager jobManager;
	
	@RequestMapping(path = "/job2", method = RequestMethod.POST)
	public String post(@RequestBody List<Job> jobs) {
	    
	    for (Job job : jobs) {
	        try {
	            if(job==null){
	                continue;
	            }
	            jobManager.add(job);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
		return "OK";
	}

}
