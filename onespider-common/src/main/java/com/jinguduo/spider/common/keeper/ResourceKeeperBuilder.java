package com.jinguduo.spider.common.keeper;

import com.jinguduo.spider.common.type.Quota;

public class ResourceKeeperBuilder<T> {

	private Quota quota;
	private ResourceKeeper<? super T> resource;
	
	private ResourceKeeperBuilder() {
	}
	
	public static ResourceKeeperBuilder<Object> custom() {
		return new ResourceKeeperBuilder<>();
	}

	public ResourceKeeperBuilder<T> quota(Quota quota) {
		this.quota = quota;
		return this;
	}

	@SuppressWarnings("unchecked")
	public <T1 extends T> ResourceKeeperBuilder<T1> resource(ResourceKeeper<? super T1> resource) {
		this.resource = (ResourceKeeper<T>) resource;
		return (ResourceKeeperBuilder<T1>) this;
	}

	@SuppressWarnings("unchecked")
	public ResourceKeeper<T> build() {
		return (ResourceKeeper<T>)resource.setQuota(quota);
	}

}
