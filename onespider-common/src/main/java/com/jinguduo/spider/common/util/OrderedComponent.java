package com.jinguduo.spider.common.util;

import lombok.Data;

@Data
public class OrderedComponent<T> {
    final T component;
    final int order;
}