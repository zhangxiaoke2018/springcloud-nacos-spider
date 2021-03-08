package com.jinguduo.spider.worker;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.Assert;

import com.jinguduo.spider.common.constant.WorkerCommand;
import com.jinguduo.spider.common.constant.SpiderStatus;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 */
@ActiveProfiles("test")
public class ClusterTests {

    private final String domain = "www.ClusterTests.com";
    
    private String getRandomString() {
        return RandomStringUtils.randomAlphanumeric(8);
    }

    @Test
    public void testInsertSuccess() {
        Cluster cluster = new Cluster();

        Worker worker = new Worker(getRandomString(), getRandomString(), domain);
        Assert.isTrue(cluster.insert(worker));
    }

    @Test
    public void testGetAllDomain() {
        Cluster cluster = new Cluster();

        cluster.insert(new Worker(getRandomString(), getRandomString(), getRandomString()));
        Assert.isTrue(cluster.getAllDomain().size() == 1);

        cluster.insert(new Worker(getRandomString(), getRandomString(), getRandomString()));
        Assert.isTrue(cluster.getAllDomain().size() == 2);

        Worker worker = new Worker(getRandomString(), getRandomString(), getRandomString());
        cluster.insert(worker);
        worker.disconnected();
        Assert.isTrue(cluster.getAllDomain().size() == 3);
    }

    @Test
    public void testGetWorkers() {
        Cluster cluster = new Cluster();
        
        cluster.insert(new Worker(getRandomString(), getRandomString(), domain));
        Assert.isTrue(cluster.getWorkers(domain).length == 1);

        cluster.insert(new Worker(getRandomString(), getRandomString(), domain));
        Assert.isTrue(cluster.getWorkers(domain).length == 2);

        Worker worker = new Worker(getRandomString(), getRandomString(), domain);
        cluster.insert(worker);
        worker.disconnected();
        Assert.isTrue(cluster.getWorkers(domain).length == 2);
        
        cluster.insert(new Worker(getRandomString(), getRandomString(), domain));
        Assert.isTrue(cluster.getWorkers(domain).length == 3);
    }

    @Test
    public void testWorkerDownAndUp() {
        Cluster cluster = new Cluster();

        Worker worker = new Worker(getRandomString(), getRandomString(), domain);
        Assert.isTrue(cluster.insert(worker));
        Assert.isTrue(cluster.getSize(domain) == 1);

        worker.disconnected();
        Assert.isTrue(cluster.getSize(domain) == 0);

        worker.touch(SpiderStatus.Running, WorkerCommand.Noop);
        Assert.isTrue(cluster.insert(worker));
        Assert.isTrue(cluster.getSize(domain) == 1);
    }

    @Test
    public void test2WorkersRollDownAndUp() {
        Cluster cluster = new Cluster();

        // one up
        Worker worker1 = new Worker(getRandomString(), getRandomString(), domain);
        Assert.isTrue(cluster.insert(worker1));
        Assert.isTrue(cluster.getSize(domain) == 1);

        // two up
        Worker worker2 = new Worker(getRandomString(), getRandomString(), domain);
        Assert.isTrue(cluster.insert(worker2));
        Assert.isTrue(cluster.getSize(domain) == 2);

        // one down
        worker1.disconnected();
        Assert.isTrue(cluster.getSize(domain) == 1);

        // one up
        worker1.touch(SpiderStatus.Running, WorkerCommand.Noop);
        Assert.isTrue(cluster.insert(worker1));
        Assert.isTrue(cluster.getSize(domain) == 2);

        // one & two down
        worker1.disconnected();
        Assert.isTrue(cluster.getSize(domain) == 1);
        worker2.disconnected();
        Assert.isTrue(cluster.getSize(domain) == 0);

        // one up
        worker1.touch(SpiderStatus.Running, WorkerCommand.Noop);
        Assert.isTrue(cluster.insert(worker1));
        Assert.isTrue(cluster.getSize(domain) == 1);

        // two up
        worker2.touch(SpiderStatus.Running, WorkerCommand.Noop);
        Assert.isTrue(cluster.insert(worker2));
        Assert.isTrue(cluster.getSize(domain) == 2);
    }

    @Test
    public void testRandomReInsert() {
        Cluster cluster = new Cluster();

        Worker[] members = new Worker[10];
        for (int i = 0; i < members.length; i++) {
            members[i] = new Worker(getRandomString(), getRandomString(), domain);
            cluster.insert(members[i]);
            Assert.isTrue(cluster.getSize(domain) == i + 1);
        }

        // 重复测试多次
        for (int j = 0; j < 30; j++) {
            // set all down
            for (int i = 0; i < members.length; i++) {
                members[i].disconnected();
            }

            // shuffle
            List<Worker> list = Arrays.asList(members);
            Collections.shuffle(list);
            list.toArray(members);

            for (int i = 0; i < members.length; i++) {
                // re-insert
                cluster.insert(members[i]);
                members[i].touch(SpiderStatus.Running, WorkerCommand.Noop);
                int size = cluster.getSize(domain);
                Assert.isTrue(size == i + 1);
            }
        }
    }

    @Test
    public void testRingSize() {
        Cluster cluster = new Cluster();

        for (int i = 1; i < 10; i++) {
            Worker worker = new Worker(getRandomString(), getRandomString(), domain);
            cluster.insert(worker);
            Assert.isTrue(cluster.getSize(domain) == i);
        }
    }
    
    @Test
    public void testInsertGrow() {
        Cluster cluster = new Cluster();

        for (int i = 1; i <= Cluster.Ring.MAX_CAPACITY; i++) {
            Worker worker = new Worker(getRandomString(), getRandomString(), domain);
            boolean inserted = cluster.insert(worker);
            Assert.isTrue(inserted, "Bad");
            Assert.isTrue(cluster.getSize(domain) == i, "bad");
        }
        Worker worker = new Worker(getRandomString(), getRandomString(), domain);
        boolean inserted = cluster.insert(worker);
        Assert.isTrue(!inserted, "Bad");
        Assert.isTrue(cluster.getSize(domain) == Cluster.Ring.MAX_CAPACITY, "bad");
    }
    
    @Test
    public void testRingSizeForRestart() {
        final int hosts = 10;
        Cluster cluster = new Cluster();

        // start
        Worker[] workers = new Worker[hosts];
        for (int i = 0; i < hosts; i++) {
            Worker worker = new Worker("hostname-" + i, getRandomString(), domain);
            workers[i] = worker;
            cluster.insert(worker);
            Assert.isTrue(cluster.getSize(domain) == i + 1);
        }
        
        // restart
        Worker[] workers2 = new Worker[hosts];
        for (int i = 0; i < hosts; i++) {
            Worker worker = new Worker("hostname-" + i, getRandomString(), domain);
            workers2[i] = worker;
            cluster.insert(worker);
            Assert.isTrue(cluster.getSize(domain) == 1 + hosts + i, "The cluster size:" + cluster.getSize(domain) + " The i:" + i);
        }
        
        // timeout
        for (int i = 0; i < hosts; i++) {
            workers[i].disconnected();
            Assert.isTrue(cluster.getSize(domain) == (hosts *2) - (i + 1), "The cluster size:" + cluster.getSize(domain) + " The i:" + i);
        }
        
        Assert.isTrue(cluster.getSize(domain) == hosts, "The cluster size:" + cluster.getSize(domain));
        
        for (int i = 0; i < hosts; i++) {
            Assert.isTrue(workers2[0].getRingIndex() < hosts, "The ring index:" + workers2[0].getRingIndex());
        }
    }
}
