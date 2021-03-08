package com.jinguduo.spider.common.type;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 分主题的延时队列
 */
public class TopicDelayQueue<E extends Delayed> implements TopicQueue<E> {

    private Map<String, DelayQueue<E>> buckets = new ConcurrentHashMap<String, DelayQueue<E>>();
    
    public DelayQueue<E> get(String topic) {
        DelayQueue<E> q = buckets.get(topic);
        if (q == null) {
            q = new DelayQueue<E>();
            buckets.put(topic, q);
        }
        return q;
    }

    public Set<String> topicSet() {
        return buckets.keySet().stream().collect(Collectors.toSet());
    }
    
    public boolean offer(String topic, E e) {
        return get(topic).offer(e);
    }
    
    public E poll(String topic) {
        return get(topic).poll();
    }
    
    public boolean remove(String topic, E value) {
        return get(topic).remove(value);
    }
    
    public boolean removeIf(String topic, Predicate<? super E> filter) {
        return get(topic).removeIf(filter);
    }
    
    public void forEach(String topic, Consumer<? super E> action) {
        get(topic).forEach(action);
    }
    
    public Stream<E> stream(String topic) {
        return get(topic).stream();
    }
    
    public Stream<E> filter(String topic, Predicate<? super E> predicate) {
        return stream(topic).filter(predicate);
    }
}
