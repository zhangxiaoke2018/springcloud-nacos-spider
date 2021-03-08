package com.jinguduo.spider.web;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jinguduo.spider.cluster.model.JobPackage;
import com.jinguduo.spider.cluster.model.JobRef;
import com.jinguduo.spider.job.JobTracker;

/**
 * 任务API（非Restful）
 * 
 */
@Controller
@ResponseBody
public class JobSyncController {

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
	@RequestMapping(path = "/jobs/sync", method = RequestMethod.POST)
	public JobPackage postJobsStatus(
				@RequestParam(name = "uuid") String uuid,
				@RequestParam(name = "version") int version,
				@RequestBody(required = false) Collection<JobRef> jobs) {
		return jobTracker.sync(uuid, version, jobs);
	}
}
