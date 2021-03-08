package com.jinguduo.spider.common.type;

import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;

import org.junit.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.Assert;

import com.google.common.collect.Sets;
import com.jinguduo.spider.cluster.model.Job;
import com.jinguduo.spider.cluster.model.JobRef;
import com.jinguduo.spider.cluster.scheduler.DelayRequest;
import com.jinguduo.spider.common.constant.JobKind;
import com.jinguduo.spider.common.type.TopicDelayQueue;

@ActiveProfiles("test")
public class TopicDelayQueueTests {
    
    private String domain = "www." + getClass().getSimpleName() +".com";

    private Random random = new Random();
    
    @Test
    public void testGet() {
        TopicDelayQueue<DelayRequest> queue = new TopicDelayQueue<>();
        Assert.notNull(queue.get(String.valueOf(random.nextInt(1000))));
    }
    
    @Test
    public void testTopicSet() throws URISyntaxException {
        TopicDelayQueue<DelayRequest> queue = new TopicDelayQueue<>();
        for (DelayRequest dr: generateDelayRequestObjects(20, 1000, false)) {
            queue.offer(dr.getJob().getHost(), dr);
        }
        Assert.isTrue(queue.topicSet().size() == 1);
    }
    
    @Test
    public void testTopicSet2() throws URISyntaxException {
        TopicDelayQueue<DelayRequest> queue = new TopicDelayQueue<>();
        for (DelayRequest dr: generateDelayRequestObjects(20, 1000, true)) {
            queue.offer(dr.getJob().getHost(), dr);
        }
        Assert.isTrue(queue.topicSet().size() == 20);
    }
    
    @Test
    public void testOffer() throws URISyntaxException {
        TopicDelayQueue<DelayRequest> queue = new TopicDelayQueue<>();
        for (DelayRequest dr: generateDelayRequestObjects(20, 100, false)) {
            queue.offer(dr.getJob().getHost(), dr);
        }
        Assert.isTrue(queue.get(domain).size() == 20);
    }
    
    @Test
    public void testPoll() throws URISyntaxException {
        TopicDelayQueue<DelayRequest> queue = new TopicDelayQueue<>();
        for (DelayRequest dr: generateDelayRequestObjects(20, 0, false)) {
            queue.offer(domain, dr);
        }
        for (int i = 0; i < 20; i++) {
            Assert.notNull(queue.poll(domain));
        }
    }
    
    @Test
    public void testPollWhenDelay() throws URISyntaxException, InterruptedException {
        TopicDelayQueue<DelayRequest> queue = new TopicDelayQueue<>();
        for (DelayRequest dr: generateDelayRequestObjects(20, 10, false)) {
            queue.offer(domain, dr);
        }
        for (int i = 0; i < 20; i++) {
            Thread.sleep(10);
            Assert.notNull(queue.poll(domain));
        }
    }
    
    @Test
    public void testRemove() throws URISyntaxException, InterruptedException {
        TopicDelayQueue<DelayRequest> queue = new TopicDelayQueue<>();
        Collection<DelayRequest> values = generateDelayRequestObjects(20, 1000, false);
        for (DelayRequest dr: values) {
            queue.offer(domain, dr);
        }
        for (DelayRequest dr : values) {
            Assert.isTrue(queue.remove(domain, dr));
        }
        Assert.isNull(queue.poll(domain));
    }
    
    @Test
    public void testRemoveIf() throws URISyntaxException, InterruptedException {
        TopicDelayQueue<DelayRequest> queue = new TopicDelayQueue<>();
        Collection<DelayRequest> values = generateDelayRequestObjects(20, 0, false);
        for (DelayRequest dr: values) {
            queue.offer(domain, dr);
        }
        Assert.isTrue(queue.removeIf(domain, e -> domain.equals(e.getJob().getHost())));
        Assert.isNull(queue.poll(domain));
    }
    
    public Collection<DelayRequest> generateDelayRequestObjects(int size, int delay, boolean isRandomDomain) throws URISyntaxException {
        Collection<DelayRequest> reqs = Sets.newHashSet();
        for (int i = 0; i < 20; i++) {
            String d = isRandomDomain ? i + "." + domain : domain;
            Job job = new Job("http://" + d + "/" + i, "GET");
            reqs.add(new DelayRequest(job, 0));
        }
        return reqs;
    }
    
    @Test
    public void testStreamFilter() throws URISyntaxException, InterruptedException {
        TopicDelayQueue<DelayRequest> queue = new TopicDelayQueue<>();
        Collection<DelayRequest> values = generateDelayRequestObjects(20, 0, false);
        for (DelayRequest dr: values) {
            dr.getJob().setKind(JobKind.Forever);
            queue.offer(domain, dr);
        }
        Assert.isTrue(queue.get(domain).size() == values.size());
        
        Collection<JobRef> jobRefs = queue.get(domain)
                .stream()
                .filter(Objects::nonNull)
                .filter(e -> e.getJob().getKind() == JobKind.Forever)
                .map(e -> JobRef.of(e.getJob()))
                .distinct()
                .collect(Collectors.toSet());
        Assert.isTrue(jobRefs.size() == values.size());
        
        Collection<JobRef> jobRefs2 = queue.get(domain)
                .stream()
                .filter(Objects::nonNull)
                .filter(e -> e.getJob().getKind() == JobKind.Once)
                .map(e -> JobRef.of(e.getJob()))
                .distinct()
                .collect(Collectors.toSet());
        Assert.isTrue(jobRefs2.size() == 0);
        //Assert.isNull(queue.poll(domain));
    }
}
