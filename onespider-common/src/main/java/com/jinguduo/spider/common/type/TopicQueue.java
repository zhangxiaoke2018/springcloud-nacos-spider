package com.jinguduo.spider.common.type;

import java.util.Queue;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

public interface TopicQueue<E> {
    
    Queue<E> get(String topic);
    
    Set<String> topicSet();
    
    boolean offer(String topic, E value);
    
    E poll(String topic);
    
    boolean remove(String topic, E value);
    
    boolean removeIf(String topic, Predicate<? super E> filter);
    
    void forEach(String topic, Consumer<? super E> action);
    
    Stream<E> stream(String topic);
    
    Stream<E> filter(String topic, Predicate<? super E> predicate);
}
