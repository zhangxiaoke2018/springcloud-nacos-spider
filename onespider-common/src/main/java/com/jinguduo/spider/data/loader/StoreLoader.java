package com.jinguduo.spider.data.loader;

public interface StoreLoader<E, V> {

    V load(E element);
    
}
