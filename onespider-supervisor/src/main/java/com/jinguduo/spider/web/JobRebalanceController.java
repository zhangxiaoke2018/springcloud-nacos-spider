package com.jinguduo.spider.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jinguduo.spider.job.JobTracker;

/**
 * Job分配API
 * 
 */
@Controller
@ResponseBody
public class JobRebalanceController {

	@Autowired
	private JobTracker jobTracker;

	/**
	 * 同步任务状态
	 * <p>
	 * <b>通过回传的任务集，可以增加、删除或修改任务
	 * 
	 * @param domain
	 * @param workerId
	 * @return
	 */
	@RequestMapping(path = "/jobs/rebalance")
	public String postJobsReblance(@RequestParam(name = "style") boolean style) {
		jobTracker.rebalance(style);
		return "Running...";
	}
}
