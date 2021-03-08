package com.jinguduo.spider.common.type;

import java.util.Iterator;
import java.util.Set;

public class MemoryIndexing<I, V> {

    private final HashMultiMap<I, V> indexing = new ConcurrentHashMultiMap<>();
    
    public void add(I key, V val) {
        indexing.put(key, val);
    }
    
    public void remove(I key, V val) {
        indexing.remove(key, val);
    }
    
    public boolean removeValue(V val) {
        for (Iterator<I> iterator = indexing.keySet().iterator(); iterator.hasNext();) {
            I k = iterator.next();
            Set<V> set = indexing.get(k);
            if (set != null && set.contains(val)) {
                return set.remove(val);
            }
        }
        return false;
    }
    
    public void reduce(Set<I> keys) {
        for (Iterator<I> iterator = indexing.keySet().iterator(); iterator.hasNext();) {
            I k = iterator.next();
            if (!keys.contains(k)) {
                Set<V> set = indexing.get(k);
                if (set != null) {
                    set.clear();
                }
                iterator.remove();
            }
        }
    }
    
    public Set<V> get(I key) {
        return indexing.get(key);
    }
}
