package com.jinguduo.spider.common.type;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SecondIndexingMemoryPool<K, V, I> {
    
    // jobId -> job
    private final Map<K, V> bucket;
    
    // uuid -> job.id
    private final MemoryIndexing<I, K> indexing = new MemoryIndexing<>();
    
    public SecondIndexingMemoryPool() {
       bucket = new ConcurrentHashMap<>();
    }
    
    public SecondIndexingMemoryPool(int capacity, float loadFactor) {
        bucket = new ConcurrentHashMap<>(capacity, loadFactor);
    }
    
    public void add(K key, V value, I idx) {
        bucket.put(key, value);
        if (idx != null) {
            indexing.add(idx, key);
        }
    }
    
    public void addIndex(I idx, K key) {
        indexing.add(idx, key);
    }
    
    public void remove(K key, I idx) {
        bucket.remove(key);
        if (idx != null) {
            indexing.remove(idx, key);
        }
    }
    
    public void remove(K key) {
        bucket.remove(key);
        indexing.removeValue(key);
    }
    
    public V get(K key) {
        return bucket.get(key);
    }
    
    public Set<K> getKeySet(I idx) {
        return indexing.get(idx);
    }
    
    public Collection<V> values() {
        return bucket.values();
    }

    public int size() {
        return bucket.size();
    }

    public void reduceIndex(Set<I> idxes) {
        indexing.reduce(idxes);
    }
}
