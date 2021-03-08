package com.jinguduo.spider.common.type;

import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

public interface HashMultiMap<K, V> {

    V pop(K key);

    Set<V> get(K key);

    boolean put(K key, V value);

    boolean remove(Object key, Object value);

    Stream<V> stream(K key);

    boolean removeIf(K key, Predicate<? super V> filter);

    void removeKey(K key);

    Set<K> keySet();

}