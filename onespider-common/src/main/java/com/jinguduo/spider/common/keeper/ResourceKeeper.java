package com.jinguduo.spider.common.keeper;

import com.jinguduo.spider.common.type.Quota;

/**
 * 资源管理器
 *  按一定配额（Quota）控制资源的使用
 *
 * @param <T>
 */
public interface ResourceKeeper<T> {
	
	ResourceKeeper<T> setQuota(Quota quota);

	T create();
	
	T get();
	
	void close(T t);
}
