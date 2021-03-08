package com.jinguduo.spider.worker;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

import com.jinguduo.spider.common.constant.WorkerCommand;
import com.jinguduo.spider.common.constant.SpiderStatus;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 */
@ActiveProfiles("test")
public class WorkerManagerTests {

    private WorkerManager workerManager = new WorkerManager();

    private final String domain = "www.WorkerManagerTests.com";

    @Before
    public void setUp() {
    }
    
    private String getRandomString() {
        return RandomStringUtils.randomAlphanumeric(8);
    }
    
    @Test
    public void testGetAllDomain() {
        workerManager.heartbeat(getRandomString(), getRandomString(), domain, SpiderStatus.Running, WorkerCommand.Noop);
        Assert.isTrue(workerManager.getAllDomain().size() == 1);
        
        String uuid = getRandomString();
        workerManager.heartbeat(getRandomString(), uuid, getRandomString(), SpiderStatus.Running, WorkerCommand.Noop);
        Assert.isTrue(workerManager.getAllDomain().size() == 2);
        
        workerManager.heartbeat(getRandomString(), getRandomString(), domain, SpiderStatus.Running, WorkerCommand.Noop);
        Assert.isTrue(workerManager.getAllDomain().size() == 2);
        
        Worker worker = workerManager.getActivedWorkerByUuid(uuid);
        worker.disconnected();
        Assert.isTrue(workerManager.getAllDomain().size() == 2);
    }
    
    @Test
    public void testGetActiveWorkers() {
        workerManager.heartbeat(getRandomString(), getRandomString(), domain, SpiderStatus.Running, WorkerCommand.Noop);
        Assert.isTrue(workerManager.getActivedWorkersByDomain(domain).length == 1);
        
        String d = getRandomString();
        workerManager.heartbeat(getRandomString(), getRandomString(), d, SpiderStatus.Running, WorkerCommand.Noop);
        Assert.isTrue(workerManager.getActivedWorkersByDomain(d).length == 1);
        
        String hostname = getRandomString();
        String uuid = getRandomString();
        workerManager.heartbeat(hostname, uuid, domain, SpiderStatus.Running, WorkerCommand.Noop);
        Assert.isTrue(workerManager.getActivedWorkersByDomain(domain).length == 2);
        
        Worker worker = workerManager.getActivedWorkerByUuid(uuid);
        worker.disconnected();
        Assert.isTrue(workerManager.getActivedWorkersByDomain(domain).length == 1);
    }

    @Test
    public void testHeartbeat() {
        String uuid = getRandomString();
        String hostname = getRandomString();

        WorkerCommand cmd = workerManager.heartbeat(hostname, uuid, domain, SpiderStatus.Running, WorkerCommand.Noop);
        Assert.isTrue(cmd == WorkerCommand.Noop);

        cmd = workerManager.heartbeat(hostname, uuid, domain, SpiderStatus.Running, WorkerCommand.Run);
        Assert.isTrue(cmd == WorkerCommand.Noop);
    }
    
    @Test
    public void testHeartbeat2() throws NoSuchFieldException, SecurityException, InterruptedException {
        String[] uuids = new String[10];
        String[] hosts = new String[10];
        for (int i = 0; i < uuids.length; i++) {
            uuids[i] = getRandomString();
            hosts[i] = getRandomString();
            WorkerCommand cmd = workerManager.heartbeat(hosts[i], uuids[i], domain, SpiderStatus.Running, WorkerCommand.Noop);
            Assert.isTrue(cmd == WorkerCommand.Noop);
            Assert.isTrue(workerManager.getClusterSize(domain) == i + 1);
        }

        long timeout = 200;
        Field timeoutField = WorkerManager.class.getDeclaredField("timeout");
        ReflectionUtils.makeAccessible(timeoutField);
        ReflectionUtils.setField(timeoutField, workerManager, timeout);
        Thread.sleep(timeout * 2);

        Assert.isTrue(workerManager.getClusterSize(domain) == 0);
        
        ReflectionUtils.setField(timeoutField, workerManager, timeout * 10000);

        List<String> uuidList = Arrays.asList(uuids);
        Collections.shuffle(uuidList);
        uuidList.toArray(uuids);
        for (int i = 0; i < uuids.length; i++) {
            WorkerCommand cmd = workerManager.heartbeat(hosts[i], uuids[i], domain, SpiderStatus.Running, WorkerCommand.Noop);
            Assert.isTrue(cmd == WorkerCommand.Noop);
            Assert.isTrue(workerManager.getClusterSize(domain) == i + 1);
        }
    }
    
    @Test
    public void testHeartbeat3() throws NoSuchFieldException, SecurityException, InterruptedException {
        String[] uuids = new String[10];
        String[] hosts = new String[10];
        for (int i = 0; i < uuids.length; i++) {
            uuids[i] = getRandomString();
            hosts[i] = getRandomString();
            WorkerCommand cmd = workerManager.heartbeat(hosts[i], uuids[i], domain, SpiderStatus.Running, WorkerCommand.Noop);
            Assert.isTrue(cmd == WorkerCommand.Noop);
            Assert.isTrue(workerManager.getClusterSize(domain) == i + 1);
        }

        
        workerManager.heartbeat(hosts[0], uuids[0], domain, SpiderStatus.Stopped, WorkerCommand.Terminate);

        Assert.isTrue(workerManager.getClusterSize(domain) == 9);
        
        List<String> uuidList = Arrays.asList(uuids);
        Collections.shuffle(uuidList);
        uuidList.toArray(uuids);
        for (int i = 0; i < uuids.length; i++) {
            WorkerCommand cmd = workerManager.heartbeat(hosts[i], uuids[i], domain, SpiderStatus.Running, WorkerCommand.Noop);
            Assert.isTrue(cmd == WorkerCommand.Noop);
        }
        Assert.isTrue(workerManager.getClusterSize(domain) == 10);
    }

    @SuppressWarnings("deprecation")
    @Test
    public void testGetAllActivedWorkers() {
        String uuid = getRandomString();
        String hostname = getRandomString();

        WorkerCommand cmd = workerManager.heartbeat(hostname, uuid, domain, SpiderStatus.Running, WorkerCommand.Noop);
        Assert.isTrue(cmd == WorkerCommand.Noop);

        Set<Worker> allActivedWorkers = workerManager.getAllActivedWorkers();
        Assert.isTrue(allActivedWorkers.size() == 1);
    }

    @Test
    public void testGetClusterSize() {
        String uuid = getRandomString();
        String hostname = getRandomString();

        WorkerCommand cmd = workerManager.heartbeat(hostname, uuid, domain, SpiderStatus.Running, WorkerCommand.Noop);
        Assert.isTrue(cmd == WorkerCommand.Noop);
        Assert.isTrue(workerManager.getClusterSize(domain) == 1);
    }
}
