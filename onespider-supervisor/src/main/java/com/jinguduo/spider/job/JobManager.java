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
            // ????????????
            if (job.getKind() == JobKind.Once && checkJobState(job)) {
                // ???????????????
                return;
            }
            // ?????????????????? load-new
            wrap = new JobWrapper(job);
            wrap.setPartitionKey(Math.abs(sequence.incrementAndGet(domain)));
            pool.add(job.getId(), wrap, null);
            saveJobState(job);

        } else if (isUpdated(wrap, job)) {
            // ???????????????????????????
            wrap.setJob(job);
            saveJobState(job);
        }
    }
    
    private boolean isUpdated(JobWrapper wrap, Job job) {
    	// ?????????Once????????????Forever??????
        //    ????????????????????????????????????????????????????????????????????????????????????
    	//    ??????Forever???????????????Once?????????????????????
    	if (wrap.getJob().getKind() == JobKind.Forever
    			&& job.getKind() == JobKind.Once) {
    		return false;
    	}
    	// ????????????
    	if (wrap.isUpdated(job)) {
    		return true;
    	}
    	// Job??????
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
        if (s.length() > 8) {  // ????????????????????????????????????????????????
            s = s.substring(0, 8);
        }
        return s;
    }

    private final static double FREQUENCY_BIAS_RATE = 0.6;
    
    private boolean checkJobState(Job job) {
        // ???????????????????????????
        if (job.getCrawledAt() == null || job.getCrawledAt() == 0) {
            Long ts = jobStateCache.findJobState(job);
            if (ts != null) {
                job.setCrawledAt(ts);
            }
        }
        // ?????????????????????
        if (job.getCrawledAt() == null || job.getCrawledAt() == 0
                || maybeBrokenOnRestartUp(job)
                || isTimeToGetUp(job)) {
            // ????????????
            return false;
        }
        return true; // ?????????
    }
    
    private boolean isTimeToGetUp(Job job) {
        // ?????????????????????
        long mills = job.getFrequency() * 1000L; // second -> millis
        long interval = (long) Math.ceil(mills * FREQUENCY_BIAS_RATE); // ?????????
        return (System.currentTimeMillis() - job.getCrawledAt()) >= interval;
    }

    /**
     * Supervisor???????????????Job??????
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
     * ??????????????????Job
     * 
     * @param worker
     * @param jobRefs
     * @return
     */
    public Collection<Job> takeAllocatedJobs(Worker worker, Collection<JobRef> jobRefs) {
        // ???????????????????????????
        SortedSet<Job> r = Sets.newTreeSet(new JobComparator());
        
        // ?????????Job.id??????
        Set<String> allocedJobIds = pool.getKeySet(worker.getUuid());
        if (allocedJobIds == null || allocedJobIds.isEmpty()) {
            return r;
        }
        
        // ???????????????Job.id??????
        Set<String> takenIds = new HashSet<>();

        // ??????Worker??????????????????
        for (Iterator<JobRef> iter = jobRefs.iterator(); iter.hasNext(); iter.remove()) {
            JobRef jobRef = iter.next();
            if (jobRef.getId() == null) {
                continue;
            }
            JobWrapper stub = pool.get(jobRef.getId());
            if (stub == null) {
                // ??????job?????????worker???(Job???????????????,????????????)
                Job removed = new Job();
                removed.setId(jobRef.getId());
                removed.setCommand(JobSchedulerCommand.Delete);
                r.add(removed);

            } else if (jobRef.getHashCode() != null && jobRef.getHashCode().intValue() != stub.getJob().hashCode()) {
                // ???????????????????????????
                stub.getJob().setCommand(JobSchedulerCommand.Update);
                r.add(stub.getJob());
                takenIds.add(stub.getJob().getId());

            } else {
                // ?????????????????????Job????????????????????????????????????Worker???????????????
                takenIds.add(jobRef.getId());
            }
        }
        jobRefs.clear();  // for gc

        // ??????????????????????????????
        for (String id : allocedJobIds) {
            if (takenIds.contains(id)) {
                continue;  // ?????????????????????Job
            }
            JobWrapper jw = pool.get(id);
            if (jw == null) {
                log.info("Pool Blank: " + id);
                //pool.remove(id);  // XXX:
                continue;
            }
            // ??????Job
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

    // Job???????????????????????????????????????????????????????????????????????????
    private final static boolean AGGRESSIVED = true;
    
    public void allocateJobs(Worker[] workers) {
        allocateJobsAndLogging(workers, AGGRESSIVED);
    }
    
    public void allocateJobs(Worker[] workers, boolean isAggressive) {
        allocateJobsAndLogging(workers, isAggressive);
    }
    
    /**
     * ????????????????????????????????????????????????Worker?????????Job???????????????
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
            // ????????????????????????????????????Job???????????????
            if (wrap.getWorkerUuid() == null  // Job?????????
                    || !workerUuids.contains(wrap.getWorkerUuid()) // Job???????????????Worker??????
                    || (isAggressive && !worker.getUuid().equals(wrap.getWorkerUuid()))) {  // ????????????????????????Job?????????
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
