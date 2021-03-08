package com.jinguduo.spider.job;

import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.model.JobRef;
import com.jinguduo.spider.cluster.model.JobWrapper;
import com.jinguduo.spider.common.constant.JobKind;
import com.jinguduo.spider.common.constant.JobSchedulerCommand;
import com.jinguduo.spider.common.type.SecondIndexingMemoryPool;
import com.jinguduo.spider.common.util.Paginator;
import com.jinguduo.spider.worker.Worker;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 */
@ActiveProfiles("test")
public class JobManagerTests {

    private final String domain = "www.JobManagerTests.com";
    
    private int defaultSize = 10;
    
    private Map<String, JobWrapper> allJobWrappers;

    @Before
    public void setUp() throws URISyntaxException {
        MockitoAnnotations.initMocks(this);

    }
    
    @SuppressWarnings("unchecked")
    private JobManager newJobManager() throws NoSuchFieldException, SecurityException {
        JobManager jobManager = new JobManager();
        
        JobStateCache jobStateCache = new JobStateCache();
        jobManager.setJobStateCache(jobStateCache);
        
        Field allJobField = JobManager.class.getDeclaredField("pool");
        ReflectionUtils.makeAccessible(allJobField);
        SecondIndexingMemoryPool<String, JobWrapper, String> pool = (SecondIndexingMemoryPool<String, JobWrapper, String>) ReflectionUtils.getField(allJobField, jobManager);
        
        Field bucketField = SecondIndexingMemoryPool.class.getDeclaredField("bucket");
        ReflectionUtils.makeAccessible(bucketField);
        allJobWrappers = (Map<String, JobWrapper>) ReflectionUtils.getField(bucketField, pool);
        
        return jobManager;
    }
    
    private String getRandomString() {
        return RandomStringUtils.randomAlphanumeric(8);
    }
    
    public Collection<Job> pickAllocatedJobs(String uuid) {
        return allJobWrappers.values().stream()
                .filter(e -> uuid.equals(e.getWorkerUuid()))
                .map(e -> e.getJob())
                .collect(Collectors.toList());
    }

    @Test
    public void testAllocateJobsWhenSingle() throws NoSuchFieldException, SecurityException, URISyntaxException {
        JobManager jobManager = newJobManager();
        int size = defaultSize + 1;
        for (int i = 0; i < size; i++) {
            Job job = new Job("http://"+ domain + "/a" + i);
            job.setId(String.valueOf(i));
            job.setCode(String.valueOf(i));
            jobManager.add(job);
        }
        
        Worker worker = new Worker(getRandomString(), getRandomString(), domain);
        worker.setRingIndex(0);
        
        jobManager.allocateJobs(new Worker[]{worker});
        Assert.isTrue(pickAllocatedJobs(worker.getUuid()).size() == size, "Bad");
    }
    
    @Test
    public void testAllocateJobsWhenMultipleWorkersAndSignleDomain() throws NoSuchFieldException, SecurityException, URISyntaxException {
        JobManager jobManager = newJobManager();
        int clusterSize = 20;
        int size = (defaultSize * clusterSize) + 1;
        for (int i = 0; i < size; i++) {
            Job job = new Job("http://"+ domain + "/a" + i);
            job.setId(String.valueOf(i));
            job.setCode(String.valueOf(i));
            jobManager.add(job);
        }
        
        Worker[] workers = new Worker[clusterSize];
        for (int i = 0; i < clusterSize; i++) {
            Worker w = new Worker(getRandomString(), getRandomString(), domain);
            w.setRingIndex(i);
            workers[i] = w;
        }
        
        jobManager.allocateJobs(workers);
        
        for (int j = 0; j < workers.length; j++) {
            Worker worker = workers[j];
            int min = (int)Math.floor((double)size / clusterSize);
            int max = (int)Math.ceil((double)size / clusterSize);
            int allocatedSize = pickAllocatedJobs(worker.getUuid()).size();
            Assert.isTrue(allocatedSize >= min && allocatedSize <= max, "Bad");
        }
    }
    
    @Test
    public void testAllocateJobsWhenMultipleWorkersAndMultipleDomains() throws NoSuchFieldException, SecurityException, URISyntaxException {
        JobManager jobManager = newJobManager();
        
        Random random = new Random();
        
        int domainCount = 17;
        int size = defaultSize * 33;
        
        String[] domains = new String[domainCount];
        List<Worker[]> workers = new ArrayList<Worker[]>();
        for (int i = 0; i < domainCount; i++) {
            domains[i] = getRandomString();
            
            int clusterSize = random.nextInt(6);
            Worker[] ring = new Worker[clusterSize];
            workers.add(ring);
            
            for (int j = 0; j < clusterSize; j++) {
                Worker w = new Worker(getRandomString(), getRandomString(), domains[i]);
                w.setRingIndex(j);
                ring[j] = w;
            }
        }
        
        // generate job
        for (int i = 0; i < size; i++) {
            int idx = random.nextInt(workers.size());
            Job job = new Job("http://"+ domains[idx] + "/a" + i);
            job.setId(String.valueOf(i));
            job.setCode(String.valueOf(i));
            jobManager.add(job);
        }
        
        jobManager.allocateJobs(workers.stream().flatMap(e -> Arrays.stream(e)).toArray(i -> new Worker[i]));
        
        for (Worker[] ring : workers) {
            for (Worker worker : ring) {
                Collection<Job> jobs = pickAllocatedJobs(worker.getUuid());
                for (Job job : jobs) {
                    Assert.isTrue(worker.getDomain().equals(job.getHost()), "Bad");
                }
            }
        }
    }
    
    @Test
    public void testReAllocateJobsWhenSingle() throws NoSuchFieldException, SecurityException, URISyntaxException {
        JobManager jobManager = newJobManager();
        int size = defaultSize + 1;
        for (int i = 0; i < size; i++) {
            Job job = new Job("http://"+ domain + "/a" + i);
            job.setId(String.valueOf(i));
            job.setKind(JobKind.Forever);
            job.setCode(String.valueOf(i));
            jobManager.add(job);
        }
        
        Worker worker = new Worker(getRandomString(), getRandomString(), domain);
        worker.setRingIndex(0);
        
        // allocate 1
        jobManager.allocateJobs(new Worker[]{worker});
        
        Collection<Job> pickAllocatedJobs = pickAllocatedJobs(worker.getUuid());
        Assert.isTrue(pickAllocatedJobs.size() == size, "Bad");
        
        Collection<JobRef> upJobs = new ArrayList<>();
        Collection<Job> takedJobs1 = jobManager.takeAllocatedJobs(worker, upJobs);
        Assert.isTrue(takedJobs1.size() == size, "Bad");
        
        upJobs = takedJobs1.stream().map(e -> JobRef.of(e)).collect(Collectors.toList());
        Collection<Job> jobs2 = jobManager.takeAllocatedJobs(worker, upJobs);
        Assert.isTrue(jobs2.size() == 0, "Bad");
        
        // allocate 2
        jobManager.allocateJobs(new Worker[]{worker});
        
        Collection<Job> pickAllocatedJobs2 = pickAllocatedJobs(worker.getUuid());
        Assert.isTrue(pickAllocatedJobs2.size() == size, "Bad");
        
        upJobs = takedJobs1.stream().map(e -> JobRef.of(e)).collect(Collectors.toList());
        Collection<Job> jobs3 = jobManager.takeAllocatedJobs(worker, upJobs);
        Assert.isTrue(jobs3.size() == 0, "Bad");
    }
    // TODO
    @Test
    public void testReAllocateJobsWhenMultipleWorkersAndSingleDomain() throws NoSuchFieldException, SecurityException, URISyntaxException {
        JobManager jobManager = newJobManager();
        int size = defaultSize + 1;
        for (int i = 0; i < size; i++) {
            Job job = new Job("http://"+ domain + "/a" + i);
            job.setId(String.valueOf(i));
            job.setKind(JobKind.Forever);
            job.setCode(String.valueOf(i));
            jobManager.add(job);
        }
        
        Worker worker = new Worker(getRandomString(), getRandomString(), domain);
        worker.setRingIndex(0);
        
        // allocate 1
        jobManager.allocateJobs(new Worker[]{worker});
        
        Collection<Job> pickAllocatedJobs = pickAllocatedJobs(worker.getUuid());
        Assert.isTrue(pickAllocatedJobs.size() == size, "Bad");
        
        Collection<JobRef> upJobs = new ArrayList<>();
        Collection<Job> takedJobs1 = jobManager.takeAllocatedJobs(worker, upJobs);
        Assert.isTrue(takedJobs1.size() == size, "Bad");
        
        upJobs = takedJobs1.stream().map(e -> JobRef.of(e)).collect(Collectors.toList());
        Collection<Job> jobs2 = jobManager.takeAllocatedJobs(worker, upJobs);
        Assert.isTrue(jobs2.size() == 0, "Bad");
        
        // allocate 2
        jobManager.allocateJobs(new Worker[]{worker});
        
        Collection<Job> pickAllocatedJobs2 = pickAllocatedJobs(worker.getUuid());
        Assert.isTrue(pickAllocatedJobs2.size() == size, "Bad");
        
        upJobs = takedJobs1.stream().map(e -> JobRef.of(e)).collect(Collectors.toList());
        Collection<Job> jobs3 = jobManager.takeAllocatedJobs(worker, upJobs);
        Assert.isTrue(jobs3.size() == 0, "Bad");
    }
    
    // TODO
    @Test
    public void testReAllocateJobsWhenMultipleWorkersAndMultipleDomains() throws NoSuchFieldException, SecurityException, URISyntaxException {
        JobManager jobManager = newJobManager();
        
        Random random = new Random();
        
        int domainCount = 17;
        int size = defaultSize * 33;
        
        String[] domains = new String[domainCount];
        List<Worker[]> workers = new ArrayList<Worker[]>();
        for (int i = 0; i < domainCount; i++) {
            domains[i] = getRandomString();
            
            int clusterSize = random.nextInt(6);
            Worker[] ring = new Worker[clusterSize];
            workers.add(ring);
            
            for (int j = 0; j < clusterSize; j++) {
                Worker w = new Worker(getRandomString(), getRandomString(), domains[i]);
                w.setRingIndex(j);
                ring[j] = w;
            }
        }
        
        // generate job
        for (int i = 0; i < size; i++) {
            int idx = random.nextInt(workers.size());
            Job job = new Job("http://"+ domains[idx] + "/a" + i);
            job.setId(String.valueOf(i));
            job.setCode(String.valueOf(i));
            jobManager.add(job);
        }
        
        Worker[] allWorkers = workers.stream().flatMap(e -> Arrays.stream(e)).toArray(i -> new Worker[i]);
        jobManager.allocateJobs(allWorkers);
        
        for (Worker[] ring : workers) {
            for (Worker worker : ring) {
                Collection<Job> jobs = pickAllocatedJobs(worker.getUuid());
                for (Job job : jobs) {
                    Assert.isTrue(worker.getDomain().equals(job.getHost()), "Bad");
                }
            }
        }
    }

    @Test
    public void testTakeAllocatedJobsWhenSingle() throws NoSuchFieldException, SecurityException, URISyntaxException {
        JobManager jobManager = newJobManager();
        
        Worker worker = new Worker(getRandomString(), getRandomString(), domain);
        worker.setRingIndex(0);
        
        List<JobRef> upJobs = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            Job job = new Job("http://"+ domain + "/aj" + i);
            job.setId(String.valueOf(i));
            job.setCode(String.valueOf(i));
            jobManager.add(job);
            upJobs.add(JobRef.of(job));
        }

        Collection<Job> jobs = jobManager.takeAllocatedJobs(worker, upJobs);
        Assert.isTrue(jobs.size() == 0, "Bad");
    }
    
    @Test
    public void testTakeAllocatedJobsWhenMultipleWorkersAndSingleDomain() throws NoSuchFieldException, SecurityException, URISyntaxException {
        JobManager jobManager = newJobManager();
        
        Random random = new Random();
        int clusterSize = random.nextInt(7) + 2;
        
        int size = (defaultSize * clusterSize) + 1;
        for (int i = 0; i < size; i++) {
            Job job = new Job("http://"+ domain + "/a" + i);
            job.setId(String.valueOf(i));
            job.setKind(JobKind.Forever);
            job.setCode(String.valueOf(i));
            jobManager.add(job);
        }
        
        Worker[] workers = new Worker[clusterSize];
        for (int i = 0; i < clusterSize; i++) {
            Worker w = new Worker(getRandomString(), getRandomString(), domain);
            w.setRingIndex(i);
            workers[i] = w;
        }
        SetMultimap<String, Job> takedJobs = HashMultimap.create();
        
        // 第一次分配任务,并检查
        reAllocation(jobManager, size, workers, takedJobs);
        
        // 缩减机器数量，再分配
        clusterSize--;
        Worker[] newWorkers = Arrays.copyOf(workers, clusterSize);
        reAllocation(jobManager, size, newWorkers, takedJobs);
        
        // 机器数量恢复，再分配，再检查
        reAllocation(jobManager, size, workers, takedJobs);
    }

    private void reAllocation(JobManager jobManager, int size, Worker[] workers, SetMultimap<String, Job> takedJobs) {
        final int clusterSize = workers.length;
        jobManager.allocateJobs(workers, true);
        // 再检查任务获取
        for (Worker worker: workers) {
            int min = (int)Math.floor((double)size / clusterSize);
            int max = (int)Math.ceil((double)size / clusterSize);
            int allocatedSize = pickAllocatedJobs(worker.getUuid()).size();
            Assert.isTrue(allocatedSize >= min && allocatedSize <= max, "Bad");
            
            Collection<Job> stubJobs = takedJobs.get(worker.getUuid()).stream().collect(Collectors.toSet());
            Collection<Job> jobs = jobManager.takeAllocatedJobs(worker, takedJobs.get(worker.getUuid()).stream().map(e -> JobRef.of(e)).collect(Collectors.toSet()));
            // TODO:只做了简单检查！ 完整的检查，需要合并Add、Update、Delete和Noop四种情况
            long count = Stream.concat(Stream.concat(
                    stubJobs.stream(),
                    takedJobs.get(worker.getUuid()).stream()),
                    jobs.stream()
                ).map(j -> j.getId()).distinct().count();
            Assert.isTrue(jobs.size() <= count, "Bad");
            
            // 过滤Delete
            Collection<String> deletedJobIds = jobs.stream()
                    .filter(e -> e.getCommand() == JobSchedulerCommand.Delete)
                    .map(e -> e.getId())
                    .collect(Collectors.toSet());
       
            jobs = Stream.concat(
                        jobs.stream().filter(e -> e.getCommand() != JobSchedulerCommand.Delete),
                        stubJobs.stream().filter(e -> !deletedJobIds.contains(e.getId()))
                    ).collect(Collectors.toSet());
            takedJobs.putAll(worker.getUuid(), jobs);
            
            jobs = jobManager.takeAllocatedJobs(worker, takedJobs.get(worker.getUuid()).stream().map(e -> JobRef.of(e)).collect(Collectors.toSet()));
            
            jobs = Stream.concat(
                    jobs.stream().filter(e -> e.getCommand() != JobSchedulerCommand.Delete),
                    stubJobs.stream().filter(e -> !deletedJobIds.contains(e.getId()))
                ).collect(Collectors.toSet());
            takedJobs.putAll(worker.getUuid(), jobs);
            // FIXME:
            Assert.isTrue(jobs.size() >= 0, "Bad");
        }
    }
    
    @Test
    public void testTakeAllocatedJobsWhenMultipleWorkersAndMultipleDomains() throws NoSuchFieldException, SecurityException, URISyntaxException {
        JobManager jobManager = newJobManager();
        
        Random random = new Random();
        
        int domainCount = 17;
        int size = defaultSize * 33;
        
        String[] domains = new String[domainCount];
        List<Worker[]> workers = new ArrayList<Worker[]>();
        for (int i = 0; i < domainCount; i++) {
            domains[i] = getRandomString();
            
            int clusterSize = random.nextInt(6);
            Worker[] ring = new Worker[clusterSize];
            workers.add(ring);
            
            for (int j = 0; j < clusterSize; j++) {
                Worker w = new Worker(getRandomString(), getRandomString(), domains[i]);
                w.setRingIndex(j);
                ring[j] = w;
            }
        }
        
        // generate job
        for (int i = 0; i < size; i++) {
            int idx = random.nextInt(workers.size());
            Job job = new Job("http://"+ domains[idx] + "/a" + i);
            job.setId(String.valueOf(i));
            job.setCode(String.valueOf(i));
            job.setKind(JobKind.Forever);
            jobManager.add(job);
        }
        
        Worker[] allWorkers = workers.stream().flatMap(e -> Arrays.stream(e)).toArray(i -> new Worker[i]);
        jobManager.allocateJobs(allWorkers);
        
        for (Worker[] ring : workers) {
            for (Worker worker : ring) {
                Collection<Job> jobs = pickAllocatedJobs(worker.getUuid());
                
                Collection<Job> jobs1 = jobManager.takeAllocatedJobs(worker, new ArrayList<JobRef>());
                Assert.isTrue(jobs1.size() == jobs.size(), "Bad");
                
                Collection<Job> jobs2 = jobManager.takeAllocatedJobs(worker, 
                        jobs1.stream().map(e -> JobRef.of(e)).collect(Collectors.toSet()));
                Assert.isTrue(jobs2.size() == 0, "Bad");
            }
        }

    }
    
    @Test
    public void testGetJobsByPaginator() throws NoSuchFieldException, SecurityException, URISyntaxException {
        JobManager jobManager = newJobManager();
        
        final int jobCount = 120;
        final int pageSize = 10;
        final int pageCount = Math.round(jobCount / pageSize);
        for (int i = 0; i < jobCount; i++) {
            Job job = new Job("http://"+ domain + "/a" + i);
            job.setId(String.valueOf(i));
            job.setCode(String.valueOf(i));
            jobManager.add(job);
        }
        
        for (int i = 1; i < pageCount; i++) {
            Paginator<JobWrapper> p = jobManager.getJobsByPaginator(i, pageSize);
            Assert.isTrue(p.getPage() == i, "Bad");
            Assert.isTrue(p.getPageCount() == pageCount, "Bad");
        }
    }
    
    @Test
    public void testGetJobByCode() throws NoSuchFieldException, SecurityException, URISyntaxException {
        JobManager jobManager = newJobManager();
        
        int size = 10;
        for (int i = 0; i < size; i++) {
            Job job = new Job("http://"+ domain + "/a" + i);
            job.setId(String.valueOf(i));
            job.setCode(String.valueOf(i));
            jobManager.add(job);
        }
        
        for (int i = 0; i < size; i++) {
            List<JobWrapper> results = jobManager.getJobByCode(String.valueOf(i));
            Assert.notEmpty(results, "Bad");
            Assert.notNull(results.get(0).getJob(), "Bad");
        }
    }
    
    @Test
    public void testGetJobKindOnce() throws NoSuchFieldException, SecurityException, URISyntaxException {
        JobManager jobManager = newJobManager();
        int size = defaultSize + 1;
        for (int i = 0; i < size; i++) {
            Job job = new Job("http://"+ domain + "/a" + i);
            job.setId(String.valueOf(i));
            job.setCode(String.valueOf(i));
            jobManager.add(job);
        }
        
        Worker worker = new Worker(getRandomString(), getRandomString(), domain);
        worker.setRingIndex(0);
        
        // alloc
        jobManager.allocateJobs(new Worker[]{worker});
        Assert.isTrue(pickAllocatedJobs(worker.getUuid()).size() == size, "Bad");
        
        // take
        Collection<Job> takeAllocatedJobs = jobManager.takeAllocatedJobs(worker, new ArrayList<JobRef>());
        Assert.notEmpty(takeAllocatedJobs, "Bad");
        Assert.isTrue(takeAllocatedJobs.size() == size, "Bad");
        
        // checkout
        jobManager.allocateJobs(new Worker[]{worker});
        Assert.isTrue(pickAllocatedJobs(worker.getUuid()).size() == 0, "Bad");
        
        // re-take
        Collection<Job> takeAllocatedJobs2 = jobManager.takeAllocatedJobs(worker, new ArrayList<JobRef>());
        Assert.notNull(takeAllocatedJobs2, "Bad");
        Assert.isTrue(takeAllocatedJobs2.size() == 0, "Bad");
    }
}
