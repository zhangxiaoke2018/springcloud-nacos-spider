package com.jinguduo.spider.cluster.model;

import java.io.Serializable;
import java.util.Collection;

import lombok.Data;

@Data
public class JobPackage implements Serializable {

	private static final long serialVersionUID = -6437150121104670365L;

	private int version = 0;
	private String workerUuid;
	private String domain;
	private Collection<Job> jobs;
	
	public JobPackage() {
		
	}

	public JobPackage(int version, String workerUuid, String domain, Collection<Job> jobs) {
		super();
		this.version = version;
		this.workerUuid = workerUuid;
		this.domain = domain;
		this.jobs = jobs;
	}

}
