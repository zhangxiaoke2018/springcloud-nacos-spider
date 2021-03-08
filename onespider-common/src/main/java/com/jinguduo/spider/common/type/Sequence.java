package com.jinguduo.spider.common.type;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.LongAdder;

public class Sequence {

    private Map<String, LongAdder> sequeues = new HashMap<String, LongAdder>();

    public final int incrementAndGet(String key) {
        LongAdder adder = getValue(key);
        adder.increment();
        return adder.intValue();
    }

    public final int getAndIncrement(String key) {
        LongAdder adder = getValue(key);
        int r = adder.intValue();
        adder.increment();
        return r;
    }

    private LongAdder getValue(String key) {
        LongAdder value = sequeues.get(key);
        if (value == null) {
            synchronized (this) {
                value = sequeues.get(key);
                if (value == null) {
                    value = new LongAdder();
                    sequeues.put(key, value);
                }
            }
        }
        return value;
    }
}
