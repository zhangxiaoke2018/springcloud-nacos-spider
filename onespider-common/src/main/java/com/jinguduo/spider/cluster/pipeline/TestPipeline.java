package com.jinguduo.spider.cluster.pipeline;

import com.jinguduo.spider.webmagic.ResultItems;
import com.jinguduo.spider.webmagic.Task;
import com.jinguduo.spider.webmagic.pipeline.Pipeline;

public class TestPipeline implements Pipeline {
	
	private ResultItems resultItems;
	private Task task;

	@Override
	public void process(ResultItems resultItems, Task task) {
		this.resultItems = resultItems;
		this.task = task;
	}

	public ResultItems getResultItems() {
		return resultItems;
	}

	public Task getTask() {
		return task;
	}

}
