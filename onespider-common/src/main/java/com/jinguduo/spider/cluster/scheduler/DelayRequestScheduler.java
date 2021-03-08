package com.jinguduo.spider.cluster.scheduler;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.lang3.RandomUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.model.JobPackage;
import com.jinguduo.spider.cluster.model.JobRef;
import com.jinguduo.spider.cluster.worker.SpiderWorker;
import com.jinguduo.spider.common.constant.JobKind;
import com.jinguduo.spider.common.metric.MetricFactory;
import com.jinguduo.spider.common.metric.Metrizable;
import com.jinguduo.spider.common.thread.AsyncTask;
import com.jinguduo.spider.common.type.TopicDelayQueue;
import com.jinguduo.spider.common.type.TopicQueue;
import com.jinguduo.spider.common.util.DbEntityHelper;
import com.jinguduo.spider.common.util.HostUtils;
import com.jinguduo.spider.webmagic.Request;
import com.jinguduo.spider.webmagic.Task;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DelayRequestScheduler implements DistributedScheduler {

    private String jobUrl;

    private RestTemplate restTemplate;
    
    // 每个域名的任务队列
    private final static int MAX_QUEUE_SIZE_PER_DOMAIN = 30000;  // Magic Number

    private TopicQueue<DelayRequest> topicQueue = new TopicDelayQueue<>();

    private Set<SpiderWorker> workers = Sets.newConcurrentHashSet();

    private final Timer timer = new Timer();
    private final static long DEFAULT_DELAY = RandomUtils.nextLong(
    		TimeUnit.SECONDS.toMillis(10), TimeUnit.SECONDS.toMillis(20));
    private final static long DEFAULT_PERIOD = TimeUnit.SECONDS.toMillis(15);
    
    private Map<String, Metrizable> queueSizeCounteres = Maps.newHashMap();

    public DelayRequestScheduler() {
        this(DEFAULT_DELAY, DEFAULT_PERIOD);
    }

    public DelayRequestScheduler(long delay, long period) {
        timer.schedule(new JobSyncTask(), delay, period);
    }

    @Override
    public void addSpiderWorker(SpiderWorker spiderWorker) {
        workers.add(spiderWorker);
        String domain = spiderWorker.getSpider().getSite().getDomain();
        Metrizable counter = MetricFactory.builder()
        		.namespace("onespider_queue")
                .metricName("queue_size")
                .addDimension("Domain", domain)
                .addDimension("Host", HostUtils.getHostName())
                .build();
        queueSizeCounteres.put(domain, counter);
    }

    @Override
    public void removeSpiderWorker(SpiderWorker iSpiderWorker) {
        workers.remove(iSpiderWorker);
    }

    @Override
    public void push(Request request, Task task) {
        if (request instanceof DelayRequest) {
            topicQueue.offer(task.getSite().getDomain(), (DelayRequest) request);
        }
    }

    @Override
    public Request poll(Task task) {
        String domain = task.getSite().getDomain();
        DelayRequest dr = topicQueue.poll(domain);
        if (dr != null) {
            //String tag = "over";
            if (dr.getJob().getKind() == JobKind.Forever) {
                // Cycle: reinsert queue
                topicQueue.offer(domain, dr.resetStartTime());
                // clone
                Job j = DbEntityHelper.copy(dr.getJob(), new Job(), null);
                j.setKind(JobKind.Once);
                dr = new DelayRequest(j);
            }
            return dr;
        }
        return null;
    }
    
    private final static float THROW_UP_PROPORTION = 1.0F;
    /**
     * 删除队列里部分一次性任务，
     * @param domain
     * @return
     */
    private int throwUp(String domain) {
    	final int t = (int)(MAX_QUEUE_SIZE_PER_DOMAIN * THROW_UP_PROPORTION);
    	int i = 0;
    	Iterator<DelayRequest> iter = topicQueue.get(domain).iterator();
    	while (i < t && iter.hasNext()) {
    		DelayRequest dr = iter.next();
			if (dr.getJob().getKind() == JobKind.Once) {
				iter.remove();
				i++;
			}
		}
    	return i;
    }

    private void syncJobForWorker(SpiderWorker worker) {
        String domain = worker.getSpider().getSite().getDomain();
        String uuid = worker.getUuid();

        String uri = UriComponentsBuilder.fromHttpUrl(jobUrl)
                .queryParam("uuid", uuid)
                .queryParam("version", worker.getSyncVersion())
                .build()
                .encode()
                .toString();

        Collection<JobRef> jobRefs = topicQueue.get(domain)
                .stream()
                .filter(Objects::nonNull)
                .filter(e -> e.getJob().getKind() == JobKind.Forever)
                .map(e -> JobRef.of(e.getJob()))
                .collect(Collectors.toSet());
        
        final Integer frequency = worker.getSpider().getSite().getFrequency();

        JobPackage jobPack = restTemplate.postForObject(uri, jobRefs, JobPackage.class);
        if (jobPack != null) {
            if (jobPack.getJobs() != null && !jobPack.getJobs().isEmpty()) {
                for (Job job : jobPack.getJobs()) {
                    // 容量检查
                    if (job.getKind() == JobKind.Once
                            && topicQueue.get(domain).size() >= MAX_QUEUE_SIZE_PER_DOMAIN) {
                        int tu = throwUp(domain);  // 吃撑着了，呕吐去
                        // TODO: 增加一个自定义监控报警
                        log.error("The DelayQueue maybe full {} throw up {}", domain, tu);
                        if (tu == 0) {
							continue; // 没空间跳过
						}
                    }
                    //  
                    if (job.getFrequency() <= 0) {
                        job.setFrequency(frequency);
                    }
                    DelayRequest delayRequest = new DelayRequest(job, 0); // 立即运行一次

                    switch (job.getCommand()) {
                        case Add:
                        	// Forever任务检查是否重复
                        	if (job.getKind() == JobKind.Forever) {
                        		Optional<DelayRequest> j = topicQueue
                        				.filter(domain, e -> e.getJob().getId().equals(job.getId()))
                        				.filter(e -> e.getJob().getKind() == JobKind.Forever)
                        				.findAny();
                        		if (!j.isPresent()) {
                        			topicQueue.offer(domain, delayRequest);
                        		}
							} else {
								// Once任务直接添加
								topicQueue.offer(domain, delayRequest);
							}
                            break;
                        case Update:
                            topicQueue.removeIf(domain, e -> e.getJob().getId().equals(job.getId()));
                            topicQueue.offer(domain, delayRequest);
                            break;
                        case Delete:
                            topicQueue.removeIf(domain, e -> e.getJob().getId().equals(job.getId()));
                            break;
                        default:
                            // nothing
                            break;
                    }
                    //JsonLogger.log("sync", job.getCommand().toString(), job);
                }
            }
            worker.setSyncVersion(jobPack.getVersion());
        }
        // aliyun cms
        Metrizable counter = queueSizeCounteres.get(domain);
        if (counter != null) {
            counter.getAndSet(topicQueue.get(domain).size());
        }
    }

    protected final class JobSyncTask extends TimerTask {
        AsyncTask task = new AsyncTask(2, "JobSync");

        @Override
        public void run() {
            for (SpiderWorker worker : workers) {
                task.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // sync
                            syncJobForWorker(worker);
                        } catch (Exception e) {
                            String domain = worker.getSpider().getSite().getDomain();
                            log.error(domain + ":" + worker.getUuid(), e);
                        }
                    }
                });
            }
        }
    }

    public void setJobUrl(String jobUrl) {
        this.jobUrl = jobUrl;
    }

    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
}
