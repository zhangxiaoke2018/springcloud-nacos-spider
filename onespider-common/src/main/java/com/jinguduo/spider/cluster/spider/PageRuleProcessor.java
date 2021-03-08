package com.jinguduo.spider.cluster.spider;

import com.jinguduo.spider.webmagic.Page;

@FunctionalInterface
public interface PageRuleProcessor {

	void apply(Page page) throws Exception;
}
