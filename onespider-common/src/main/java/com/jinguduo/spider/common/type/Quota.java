package com.jinguduo.spider.common.type;

/**
 * 配额
 * <p>
 *   基于预配置的额度检查，但不做阻塞等控制。<br>
 *   只是记录、检查额度值，并返回boolean值。<br>
 *   由调用者决定如何处理<br>
 *
 */
public interface Quota {

    boolean isAboved(String key);
    
    void reset(String key);
}
