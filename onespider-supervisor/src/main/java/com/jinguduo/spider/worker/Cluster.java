package com.jinguduo.spider.worker;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import lombok.extern.apachecommons.CommonsLog;

@CommonsLog
public class Cluster {

    private Map<String, Ring> cluster = Collections.synchronizedMap(new HashMap<String, Ring>());

    public boolean insert(Worker worker) {
        return getRing(worker.getDomain()).insert(worker);
    }

    public Set<String> getAllDomain() {
        return cluster.keySet();
    }

    public Worker[] getWorkers(String domain) {
        Ring ring = getRing(domain);
        return Arrays.copyOf(ring.pearls, getSize(domain));
    }

    public int getSize(String domain) {
        Ring ring = getRing(domain);
        ring.reduce();
        return ring.size;
    }

    private Ring getRing(String domain) {
        Ring ring = cluster.get(domain);
        if (ring == null) {
            ring = new Ring();
            cluster.put(domain, ring);
        }
        return ring;
    }

    static class Ring {
        final static int DEFAULT_CAPACITY = 30;
        final static int MAX_CAPACITY = 512;  // magic
        Worker[] pearls = new Worker[DEFAULT_CAPACITY];
        volatile int size = 0;

        synchronized boolean insert(Worker worker) {
            boolean inserted = false;
            if (size >= pearls.length) {
                grow();
            }
            for (int i = 0; i < pearls.length; i++) {
                Worker w = pearls[i];
                if (!inserted && (w == null || w == worker || w.isDown())) {
                    pearls[i] = worker;
                    worker.setRingIndex(i);
                    size = Integer.max(i + 1, size);
                    inserted = true;
                    continue;
                } else if (inserted && w == worker) {
                    pearls[i] = null;
                }
            }
            return inserted;
        }
        
        synchronized void grow() {
            int oldCapacity = pearls.length;
            if (oldCapacity >= MAX_CAPACITY) {
                log.warn("The SpiderWorker Cluster is so big!");
            }
            int newCapacity = Integer.min(oldCapacity + (oldCapacity >> 1), MAX_CAPACITY);
            pearls = Arrays.copyOf(pearls, newCapacity);
        }

        synchronized void reduce() {
            for (int i = 0; i < size; i++) {
                Worker w = pearls[i];
                if (w == null || w.isDown()) {
                    if (w != null) {
                        w.setRingIndex(-1);
                    }
                    while (true) {
                        if (size == 0) {
                            break;
                        }
                        Worker lastest = pearls[--size];
                        if (lastest == w) {
                            pearls[size] = null;
                            break;
                        }

                        if (lastest != null && !lastest.isDown()) {
                            pearls[i] = lastest;
                            pearls[size] = null;
                            lastest.setRingIndex(i);
                            break;
                        }
                    }
                }
            }
        }
    }
}
