package com.jinguduo.spider.job;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.google.common.collect.Sets;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.model.JobRef;
import com.jinguduo.spider.cluster.model.JobWrapper;
import com.jinguduo.spider.common.constant.FrequencyConstant;
import com.jinguduo.spider.common.constant.JobKind;
import com.jinguduo.spider.common.constant.JobSchedulerCommand;
import com.jinguduo.spider.common.type.SecondIndexingMemoryPool;
import com.jinguduo.spider.common.type.Sequence;
import com.jinguduo.spider.common.util.Paginator;
import com.jinguduo.spider.worker.Worker;

import lombok.extern.apachecommons.CommonsLog;

@Component
@CommonsLog
public class JobManager {
    
    @Autowired
    private JobStateCache jobStateCache;
    
    private final static int INITIAL_CAPACITY = 2^19;
    private final static float LOAD_FACTOR = 0.8f;

    // Key:Job.Id -> Value:JobWrapper  SecondIndex: uuid -> Job.id 
    private final SecondIndexingMemoryPool<String, JobWrapper, String> pool = new SecondIndexingMemoryPool<>(INITIAL_CAPACITY, LOAD_FACTOR);

    private final Sequence sequence = new Sequence();

    @SuppressWarnings("deprecation")
    private HashFunction hash = Hashing.md5();

    private final static long STARTUP_TIMESTAMP = System.currentTimeMillis();
    private final static long RESTART_UP_DURATION = TimeUnit.MINUTES.toMillis(10);

    public void add(Job job) {
        add(job.getHost(), job);
    }

    private void add(String domain, Job job) {
        Assert.notNull(job, "The job maybe null.");
        Assert.notNull(job.getCode(), "The job.code maybe null. is url:" + job.getUrl());

        // generate id
        if (job.getId() == null) {
            job.setId(generateId(job));
        }

        JobWrapper wrap = pool.get(job.getId());
        if (wrap == null) {
            // 检查任务
            if (job.getKind() == JobKind.Once && checkJobState(job)) {
                // 不需要执行
                return;
            }
            // 加入任务队列 load-new
            wrap = new JobWrapper(job);
            wrap.setPartitionKey(Math.abs(sequence.incrementAndGet(domain)));
            pool.add(job.getId(), wrap, null);
            saveJobState(job);

        } else if (isUpdated(wrap, job)) {
            // 原有任务，属性修改
            wrap.setJob(job);
            saveJobState(job);
        }
    }
    
    private boolean isUpdated(JobWrapper wrap, Job job) {
    	// 不允许Once任务修改Forever任务
        //    广告监测和自动发现等自动漫游的任务会重新发现已录入的剧，
    	//    导致Forever任务被改为Once然后任务被删除
    	if (wrap.getJob().getKind() == JobKind.Forever
    			&& job.getKind() == JobKind.Once) {
    		return false;
    	}
    	// 基本属性
    	if (wrap.isUpdated(job)) {
    		return true;
    	}
    	// Job频率
    	if (job.getFrequency() != wrap.getJob().getFrequency()) {
			return true;
		}
    	return false;
    }
    
    private void saveJobState(Job job) {
        job.setCrawledAt(System.currentTimeMillis());
        jobStateCache.saveJobState(job);
    }

    private String generateId(Job job) {
        byte[] bytes = hash.newHasher()
                .putString(job.getUrl(), Charset.forName("UTF-8"))
                .putString(job.getCode(), Charset.forName("UTF-8"))
                .hash().asBytes();
        String s = Base64.getUrlEncoder().encodeToString(bytes);
        if (s.length() > 8) {  // 减少内存占用，在低概率冲突条件下
            s = s.substring(0, 8);
        }
        return s;
    }

    private final static double FREQUENCY_BIAS_RATE = 0.6;
    
    private boolean checkJobState(Job job) {
        // 从缓存加载运行状态
        if (job.getCrawledAt() == null || job.getCrawledAt() == 0) {
            Long ts = jobStateCache.findJobState(job);
            if (ts != null) {
                job.setCrawledAt(ts);
            }
        }
        // 是否到执行时间
        if (job.getCrawledAt() == null || job.getCrawledAt() == 0
                || maybeBrokenOnRestartUp(job)
                || isTimeToGetUp(job)) {
            // 需要执行
            return false;
        }
        return true; // 不需要
    }
    
    private boolean isTimeToGetUp(Job job) {
        // 是否到执行时间
        long mills = job.getFrequency() * 1000L; // second -> millis
        long interval = (long) Math.ceil(mills * FREQUENCY_BIAS_RATE); // 偏差率
        return (System.currentTimeMillis() - job.getCrawledAt()) >= interval;
    }

    /**
     * Supervisor重启会导致Job状态
     * @param state
     * @return
     */
    private boolean maybeBrokenOnRestartUp(Job job) {
        final long t = STARTUP_TIMESTAMP - job.getCrawledAt();
        return t > 0 && t < RESTART_UP_DURATION;
    }

    private int distribute(final int clusterSize, final int partitionKey) {
        return partitionKey % clusterSize;
    }

    /**
     * 获取已分配的Job
     * 
     * @param worker
     * @param jobRefs
     * @return
     */
    public Collection<Job> takeAllocatedJobs(Worker worker, Collection<JobRef> jobRefs) {
        // 按任务按优先级分配
        SortedSet<Job> r = Sets.newTreeSet(new JobComparator());
        
        // 已分配Job.id集合
        Set<String> allocedJobIds = pool.getKeySet(worker.getUuid());
        if (allocedJobIds == null || allocedJobIds.isEmpty()) {
            return r;
        }
        
        // 暂存已下发Job.id集合
        Set<String> takenIds = new HashSet<>();

        // 循环Worker当前任务队列
        for (Iterator<JobRef> iter = jobRefs.iterator(); iter.hasNext(); iter.remove()) {
            JobRef jobRef = iter.next();
            if (jobRef.getId() == null) {
                continue;
            }
            JobWrapper stub = pool.get(jobRef.getId());
            if (stub == null) {
                // 删除job从当前worker中(Job已重新分配,或已删除)
                Job removed = new Job();
                removed.setId(jobRef.getId());
                removed.setCommand(JobSchedulerCommand.Delete);
                r.add(removed);

            } else if (jobRef.getHashCode() != null && jobRef.getHashCode().intValue() != stub.getJob().hashCode()) {
                // 更新（任务已修改）
                stub.getJob().setCommand(JobSchedulerCommand.Update);
                r.add(stub.getJob());
                takenIds.add(stub.getJob().getId());

            } else {
                // 暂存无需处理的Job（任务未修改，且已在当前Worker任务队列）
                takenIds.add(jobRef.getId());
            }
        }
        jobRefs.clear();  // for gc

        // 循环已发布待分配队列
        for (String id : allocedJobIds) {
            if (takenIds.contains(id)) {
                continue;  // 跳过无需处理的Job
            }
            JobWrapper jw = pool.get(id);
            if (jw == null) {
                log.info("Pool Blank: " + id);
                //pool.remove(id);  // XXX:
                continue;
            }
            // 分配Job
            Job job = jw.getJob();
            job.setCommand(JobSchedulerCommand.Add);
            r.add(job);

            if (job.getKind() == JobKind.Once) {
                remove(job, jw.getWorkerUuid());
            }
        }
        takenIds.clear();  // cleanup
        return r;
    }

    private void remove(Job job, String workerUuid) {
        String id = job.getId();
        if (!StringUtils.hasText(id)) {
            id = this.generateId(job);
        }
        if (StringUtils.hasText(workerUuid)) {
            pool.remove(id, workerUuid);
        } else {
            pool.remove(id);
        }
    }

    public void remove(Job job) {
        remove(job, null);
    }

    // Job均衡模式，为了兼容历史遗留测试用例默认为：激进模式
    private final static boolean AGGRESSIVED = true;
    
    public void allocateJobs(Worker[] workers) {
        allocateJobsAndLogging(workers, AGGRESSIVED);
    }
    
    public void allocateJobs(Worker[] workers, boolean isAggressive) {
        allocateJobsAndLogging(workers, isAggressive);
    }
    
    /**
     * 非激进的温和模式下，对于已分配且Worker还存在Job不重新分配
     * @param workers
     * @param isAggressive
     */
    private void allocateJobsAndLogging(Worker[] workers, boolean isAggressive) {
        if (workers == null || workers.length == 0) {
            log.info("Skip Allocation Job.");
            return;
        }
        // stamp
        int allocCounter = 0;
        int jobCounter = 0;
        final long ts = System.currentTimeMillis();
        
        // Worker.uuid
        Set<String> workerUuids = Arrays.stream(workers).map(e -> e.getUuid()).distinct().collect(Collectors.toSet());
        // Domain -> Worker[]
        Map<String, List<Worker>> table = Arrays.stream(workers)
                .sorted(Comparator.comparing(Worker::getRingIndex))
                .collect(Collectors.groupingBy(Worker::getDomain));
        
        for (JobWrapper wrap : pool.values()) {
            jobCounter++;
            String domain = wrap.getJob().getHost();
            List<Worker> ws = table.get(domain);
            if (ws == null || ws.isEmpty()) {
                log.info("Worker List is empty: " + domain);
                continue;
            }
            // mapping: job -> worker
            final int distributeIndex = distribute(ws.size(), wrap.getPartitionKey());
            final Worker worker = ws.get(distributeIndex);
            if (worker == null) {
                log.info("The Worker[" + distributeIndex + "] not found.");
                continue;
            }
            // 非激进模式下，对于已分配Job不重新分配
            if (wrap.getWorkerUuid() == null  // Job未分配
                    || !workerUuids.contains(wrap.getWorkerUuid()) // Job已分配，但Worker失效
                    || (isAggressive && !worker.getUuid().equals(wrap.getWorkerUuid()))) {  // 激进模式下，所有Job重分配
                // remove old
                if (wrap.getWorkerUuid() != null) {
                    pool.remove(wrap.getJob().getId(), wrap.getWorkerUuid());
                }
                // allocation
                wrap.setWorkerUuid(worker.getUuid());
                // add to pool
                pool.add(wrap.getJob().getId(), wrap, worker.getUuid());
                allocCounter++;
            }
        }
        
        // Cleanup indexing
        pool.reduceIndex(workerUuids);
        
        // logging
        log.info("Aggressived:" + isAggressive +
                " Time:" + (System.currentTimeMillis() - ts) +
                " Worker:" + workers.length +
                " Allocation:" + allocCounter +
                " Job:" + jobCounter);
    }

    static class JobComparator implements Comparator<Job> {
        @Override
        public int compare(Job o1, Job o2) {
            int r = -1;
            if (o1.getFrequency() > o2.getFrequency()) {
                r = 1;
            }
            return r;
        }
    }

    public Paginator<JobWrapper> getJobsByPaginator(int page, int size) {
        Paginator<JobWrapper> p = new Paginator<>(page, size, pool.size());

        List<JobWrapper> entites = pool.values().stream()
                .skip(p.getOffset())
                .limit(p.getSize())
                .collect(Collectors.toList());
        p.setEntites(entites);

        return p;
    }

    public List<JobWrapper> getJobByCode(String code) {
        if (!StringUtils.hasText(code)) {
            return null;
        }
        List<JobWrapper> results = pool.values().stream()
                .filter(e -> code.equals(e.getJob().getCode()))
                .collect(Collectors.toList());
        return results;
    }

    public void setJobStateCache(JobStateCache jobStateCache) {
        this.jobStateCache = jobStateCache;
    }

}
