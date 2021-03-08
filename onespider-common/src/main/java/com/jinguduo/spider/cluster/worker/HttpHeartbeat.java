package com.jinguduo.spider.cluster.worker;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.jinguduo.spider.common.constant.WorkerCommand;
import com.jinguduo.spider.common.thread.Timeout;
import com.jinguduo.spider.common.util.HostUtils;

import lombok.extern.apachecommons.CommonsLog;

@CommonsLog
public class HttpHeartbeat implements Heartbeat {
	
	protected String heartbeatUrl;

	protected RestTemplate restTemplate;
	
	// TimeUnit.SECONDS
	private final static TimeUnit TIME_UNIT = TimeUnit.SECONDS;
	protected long delay = 20;   // first delay
	protected long period = 20;  // interval for every time
	protected long taskTimeout = 5; // 
	
	protected int executorPoolSize = 2;
	protected final ScheduledExecutorService executor;
	
	private final String hostname;
	
	private Map<String, ScheduledFuture<?>> scheds = new HashMap<>();
	
	public HttpHeartbeat(RestTemplate restTemplate, String heartbeatUrl) {
		this.restTemplate = restTemplate;
		this.heartbeatUrl = heartbeatUrl;
		
		ThreadFactory threadFactory = new ThreadFactoryBuilder()
		            .setNameFormat(this.getClass().getSimpleName() + "-%d")
		            .build();
		this.executor = Executors.newScheduledThreadPool(executorPoolSize, threadFactory);
		
		hostname = HostUtils.getHostName();
	}
	
	@Override
	public synchronized void start(SpiderWorker worker) {
	    String domain = worker.getSpider().getSite().getDomain();
        ScheduledFuture<?> sched = scheds.get(domain);
        // 如果爬虫重启，删除旧的HeartbeatTask
        if (sched != null) {
            sched.cancel(true);
            scheds.remove(domain);
        }
        if (sched == null || sched.isCancelled() || sched.isDone()) {
            sched = executor.scheduleAtFixedRate(new HeartbeatTask(worker), delay, period, TIME_UNIT);
            scheds.put(domain, sched);
        }
	}
	
	protected void send(SpiderWorker worker) throws Exception {
        String domain = worker.getSpider().getSite().getDomain();
        if (!StringUtils.hasText(domain)) {
            log.error("Domain is blank.");
        }
        
        URI uri = UriComponentsBuilder.fromHttpUrl(heartbeatUrl)
                .queryParam("hostname", hostname)
                .queryParam("uuid", worker.getUuid())
                .queryParam("status", worker.getSpiderEngine().getStatus())
                .queryParam("command", worker.getCommand())
                .queryParam("domain", domain)
                .queryParam("ts", System.currentTimeMillis())
                .build()
                .encode()
                .toUri();
        
        WorkerCommand command = restTemplate.getForObject(uri, WorkerCommand.class);
        
        String tag = "";
        if (command != null) {
            SpiderDriver.perform(worker, command);
            tag = command.toString();
        }
        if (log.isDebugEnabled()) {
            log.debug(String.format("%s,%s,%s", "heartbeat", domain, tag));
        }
	}
	
	class HeartbeatTask implements Runnable {
		private final SpiderWorker worker;

		public HeartbeatTask(SpiderWorker worker) {
			this.worker = worker;
		}

		@Override
		public void run() {
			try {
			    Timeout.execute(new Runnable() {
			        public void run() {
			            try {
			                send(worker);
                        } catch (Exception e) {
                            log.error(worker.getUuid(), e);
                        }
			        }
			    }, taskTimeout, TIME_UNIT);

            } catch (Exception e) {
                // just log
                log.error(worker.getUuid(), e);
            }
		}
	}

	public void setHeartbeatUrl(String heartbeatUrl) {
		this.heartbeatUrl = heartbeatUrl;
	}

	public void setRestTemplate(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	public void setHeartbeatDelay(long heartbeatDelay) {
		this.delay = heartbeatDelay;
	}

	public void setHeartbeatPeriod(long heartbeatPeriod) {
		this.period = heartbeatPeriod;
	}
}
