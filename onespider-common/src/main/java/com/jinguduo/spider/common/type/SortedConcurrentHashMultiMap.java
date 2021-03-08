package com.jinguduo.spider.common.type;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.Predicate;
import java.util.stream.Stream;


public class SortedConcurrentHashMultiMap<K, V> implements HashMultiMap<K, V> {

	private Map<K, Set<V>> bucket = new ConcurrentHashMap<>();
	
	@SuppressWarnings("unchecked")
	private Set<V> findOrCreate(Object key) {
		Set<V> set = bucket.get(key);
		if (set == null) {
			synchronized (this) {
				set = bucket.get(key);
				if (set == null) {
					set = new ConcurrentSkipListSet<>();
					bucket.put((K)key, set);
				}
			}
		}
		return set;
	}
	
	@Override
	public V pop(K key) {
	    Optional<V> v = findOrCreate(key).stream().findFirst();
	    if (v.isPresent()) {
	        findOrCreate(key).remove(v.get());
        }
	    return v.orElse(null);
	}

	@Override
	public Set<V> get(K key) {
		return findOrCreate(key);
	}

	@Override
	public boolean put(K key, V value) {
		return findOrCreate(key).add(value);
	}

	@Override
	public boolean remove(Object key, Object value) {
		return findOrCreate(key).remove(value);
	}
	
	@Override
	public Stream<V> stream(K key) {
		return findOrCreate(key).stream();
	}
	
	@Override
	public boolean removeIf(K key, Predicate<? super V> filter) {
        return findOrCreate(key).removeIf(filter);
    }

    @Override
    public void removeKey(K key) {
        bucket.remove(key);
    }

    @Override
    public Set<K> keySet() {
        return bucket.keySet();
    }
}
