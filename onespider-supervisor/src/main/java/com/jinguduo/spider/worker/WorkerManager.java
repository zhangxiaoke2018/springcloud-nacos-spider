package com.jinguduo.spider.worker;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.jinguduo.spider.common.constant.SpiderStatus;
import com.jinguduo.spider.common.constant.WorkerCommand;

@Component
public class WorkerManager {

    // for test: no final
    private static long timeout = TimeUnit.MINUTES.toMillis(1);

    private Map<String, Worker> workers = new ConcurrentHashMap<String, Worker>();

    private Cluster cluster = new Cluster();

    public WorkerCommand heartbeat(String hostname, String uuid, String domain, SpiderStatus status, WorkerCommand command) {
        Worker worker = workers.get(uuid);
        if (worker == null) {
            worker = new Worker(hostname, uuid, domain);
            workers.put(uuid, worker);
            cluster.insert(worker);

        } else if (status != SpiderStatus.Stopped
                && (worker.getRingIndex() < 0 || worker.isDown())) {
            // re-insert
            cluster.insert(worker);
        }
        return worker.touch(status, command);
    }

    public int getClusterSize(String domain) {
        timeout();
        return cluster.getSize(domain);
    }
    
    public Set<String> getAllDomain() {
        return cluster.getAllDomain();
    }
    
    public Worker[] getActivedWorkersByDomain(String domain) {
        timeout();
        return cluster.getWorkers(domain);
    }

    public Set<Worker> getAllActivedWorkers() {
        timeout();
        return workers.values().stream().filter(e -> !e.isDown()).collect(Collectors.toSet());
    }

    public Worker getActivedWorkerByUuid(String workerUuid) {
        Worker worker = workers.get(workerUuid);
        if (worker == null || worker.isDown()) {
            return null;
        }
        disconnect(worker);
        return worker.isDown() ? null : worker;
    }
    
    private void timeout() {
        for (Worker w : workers.values()) {
            disconnect(w);
        }
    }

    private void disconnect(Worker worker) {
        if (worker.isDown()) {
            return;
        }
        final long bar = System.currentTimeMillis() - timeout;
        if (worker.getTimestamp() < bar) {
            worker.disconnected();
        }
    }

}
