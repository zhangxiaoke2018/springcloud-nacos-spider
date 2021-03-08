package com.jinguduo.spider.common.type;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.google.common.collect.Sets;


public class ConcurrentHashMultiMap<K, V> implements HashMultiMap<K, V> {

	private Map<K, Set<V>> bucket = new ConcurrentHashMap<>();
	
	@SuppressWarnings("unchecked")
	private Set<V> findOrCreate(Object key) {
		Set<V> set = bucket.get(key);
		if (set == null) {
			synchronized (this) {
				set = bucket.get(key);
				if (set == null) {
					set = Sets.newConcurrentHashSet();
					bucket.put((K)key, set);
				}
			}
		}
		return set;
	}
	
	/* (non-Javadoc)
     * @see com.jinguduo.spider.common.type.HashMultiMap#pop(K)
     */
	@Override
    public V pop(K key) {
	    Optional<V> v = findOrCreate(key).stream().findFirst();
	    if (v.isPresent()) {
	        findOrCreate(key).remove(v.get());
        }
	    return v.orElse(null);
	}

	/* (non-Javadoc)
     * @see com.jinguduo.spider.common.type.HashMultiMap#get(K)
     */
	@Override
    public Set<V> get(K key) {
		return findOrCreate(key);
	}

	/* (non-Javadoc)
     * @see com.jinguduo.spider.common.type.HashMultiMap#put(K, V)
     */
	@Override
    public boolean put(K key, V value) {
		return findOrCreate(key).add(value);
	}

	/* (non-Javadoc)
     * @see com.jinguduo.spider.common.type.HashMultiMap#remove(java.lang.Object, java.lang.Object)
     */
	@Override
    public boolean remove(Object key, Object value) {
		return findOrCreate(key).remove(value);
	}
	
	/* (non-Javadoc)
     * @see com.jinguduo.spider.common.type.HashMultiMap#stream(K)
     */
	@Override
    public Stream<V> stream(K key) {
		return findOrCreate(key).stream();
	}
	
	/* (non-Javadoc)
     * @see com.jinguduo.spider.common.type.HashMultiMap#removeIf(K, java.util.function.Predicate)
     */
	@Override
    public boolean removeIf(K key, Predicate<? super V> filter) {
        return findOrCreate(key).removeIf(filter);
    }
	
	/* (non-Javadoc)
     * @see com.jinguduo.spider.common.type.HashMultiMap#removeKey(K)
     */
	@Override
    public void removeKey(K key) {
	    bucket.remove(key);
	}
	
	/* (non-Javadoc)
     * @see com.jinguduo.spider.common.type.HashMultiMap#keySet()
     */
	@Override
    public Set<K> keySet() {
	    return bucket.keySet();
	}
}
