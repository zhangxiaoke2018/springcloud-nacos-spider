package com.jinguduo.spider.job;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableSet;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.model.JobPackage;
import com.jinguduo.spider.cluster.model.JobRef;
import com.jinguduo.spider.cluster.model.JobWrapper;
import com.jinguduo.spider.worker.Worker;
import com.jinguduo.spider.worker.WorkerManager;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 */
@ActiveProfiles("test")
public class JobTrackerTests {

    @Mock
    private WorkerManager workerManager;

    @Mock
    private JobManager jobManager;

    @Autowired
    @InjectMocks
    private JobTracker jobTracker;

    private String hostname = RandomStringUtils.randomAlphanumeric(5);
    private String workUuid = RandomStringUtils.randomAlphanumeric(5);

    private Integer clientVersion = 0;

    private Collection<JobWrapper> unallocatedJobWrapperes;
    
    private List<Job> jobs;
    
    private String domain = "www.JobTrackerTests.com";

    @Before
    public void setUp() throws URISyntaxException {
        MockitoAnnotations.initMocks(this);
        
        unallocatedJobWrapperes = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            Job job = new Job("http://" + domain + "/" + i);
            job.setId(String.valueOf(i));
            unallocatedJobWrapperes.add(new JobWrapper(job));
        }
        
        jobs = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            Job job = new Job("http://" + domain + "/" + i);
            job.setId(String.valueOf(i + 1000));
            jobs.add(job);
        }
        
        Set<Job> takedJobs = jobs.stream().collect(Collectors.toSet());
        
        Worker worker = new Worker(hostname, workUuid, domain);
        Mockito.when(workerManager.getActivedWorkerByUuid(workUuid)).thenReturn(worker);
        Mockito.when(workerManager.getActivedWorkersByDomain(domain)).thenReturn(new Worker[]{worker});
        
        Mockito.when(jobManager.takeAllocatedJobs(Mockito.eq(worker), Mockito.any())).thenReturn(takedJobs);
    }
    
    @Test
    public void testRebalance() {
        Mockito.when(workerManager.getAllDomain()).thenReturn(ImmutableSet.copyOf(new String[]{domain}));
        jobTracker.rebalance(false);
        // TODO: test
    }
    
    @Test
    public void testSync(){
        JobPackage sync = jobTracker.sync(workUuid, clientVersion, jobs.stream().map(e -> JobRef.of(e)).collect(Collectors.toList()));
        Assert.isTrue(sync.getVersion() == 1);
        Assert.isTrue(sync.getJobs().size() == jobs.size());

    }
    
    @Test
    public void testSyncByBadVersion(){
        int badVersion = new Random().nextInt(999) + 101;
        JobPackage sync = jobTracker.sync(workUuid, badVersion, jobs.stream().map(e -> JobRef.of(e)).collect(Collectors.toList()));
        Assert.isTrue(sync.getVersion() != badVersion);
        Assert.isTrue(sync.getJobs().size() == jobs.size());
    }

}
