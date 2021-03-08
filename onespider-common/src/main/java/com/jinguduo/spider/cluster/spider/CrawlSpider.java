package com.jinguduo.spider.cluster.spider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;

import org.apache.commons.collections.CollectionUtils;

import com.jinguduo.spider.cluster.spider.PageRule.Rule;
import com.jinguduo.spider.common.exception.QuickException;
import com.jinguduo.spider.common.metric.MetricFactory;
import com.jinguduo.spider.common.metric.Metrizable;
import com.jinguduo.spider.common.util.HostUtils;
import com.jinguduo.spider.webmagic.Page;

import lombok.extern.apachecommons.CommonsLog;

/**
 * 多url模式
 * 
 *
 */
@CommonsLog
public abstract class CrawlSpider implements Spider {

	public abstract PageRule getPageRule();
	
	private  Metrizable exceptionCounter;
    private  Metrizable successCounter;

	private void init(){
        if (exceptionCounter == null && successCounter == null){
            successCounter = MetricFactory.builder()
            		.namespace("onespider_crawl")
                    .metricName("crawl_success")
                    .addDimension("Host", HostUtils.getHostName())
                    .addDimension("Domain", this.getSite().getDomain())
                    .build();
            
            exceptionCounter = MetricFactory.builder()
            		.namespace("onespider_crawl")
                    .metricName("crawl_exception")
                    .addDimension("Host", HostUtils.getHostName())
                    .addDimension("Domain", this.getSite().getDomain())
                    .build();
        }
    }


	@Override
	public void process(Page page) throws Exception {
	    Exception lastException = null;
	    
        init();

		Collection<Rule> rules = getPageRule().getAll();
		if (rules == null || rules.isEmpty()) {
			throw new RuntimeException("The PageRule is empty.");
		}
		String url = page.getRequest().getUrl();
		for (Rule rule : rules) {
			Matcher matcher = rule.pattern.matcher(url);
			if (matcher != null && matcher.find()) {
			    try {
			        rule.getProcessor().apply(page);
                    successCounter.addAndGet(1);
                } catch (Exception e) {
                    exceptionCounter.addAndGet(1);
                    if (lastException != null) {  // 避免重复输出异常，只log被忽略的异常
                        log.error(page.getRequest().getUrl(), e);
                    }
                    lastException = new QuickException(e.getMessage(), e);
                }
			}
		}
		if (lastException != null) {
		    throw lastException;
        }
	}


	
	protected <T> void putModel(Page page, T model) {
        if (model == null) {
            return;
        }
        String key = "";
        if(model.getClass().isAssignableFrom(ArrayList.class)){
            @SuppressWarnings("unchecked")
            List<T> l =(List<T>)model;
            if(CollectionUtils.isEmpty(l)){
                return;
            }else{
                key = l.get(0).getClass().getSimpleName();
                Collection<T> models = page.getResultItems().get(key);
                if (models == null) {
                    models = new ArrayList<T>();
                    page.putField(key, models);
                }
                models.addAll(l);
            }
        }else{
            key = model.getClass().getSimpleName();
            Collection<T> models = page.getResultItems().get(key);
            if (models == null) {
                models = new ArrayList<T>();
                page.putField(key, models);
            }
            models.add(model);
        }
    }

}
