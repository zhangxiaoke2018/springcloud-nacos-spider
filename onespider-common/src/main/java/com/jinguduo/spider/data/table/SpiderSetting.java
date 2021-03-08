package com.jinguduo.spider.data.table;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

import com.jinguduo.spider.common.constant.FrequencyConstant;

import lombok.Data;

@Entity
@Table(name = "spider_settings")
@Data
public class SpiderSetting implements Serializable {

	private static final long serialVersionUID = 3957623803489600207L;

	@Id
	@GeneratedValue
	private Integer id;
	
	@Column(nullable = false) 
    @NotNull
	private Integer threadNum = 1;  // 并行线程数量
	
	@Column(nullable = false)
	@NotNull
	private Integer sleepTime = 0;  // 每次请求间隔, 毫秒
	
	@Column(nullable = false)
    @NotNull
	private Integer retryTimes = 0;  // http request IOException失败重试次数
	
	@Column(nullable = false)
    @NotNull
	private Integer timeOut = 7000;  // http request的超时时间, 毫秒
	
	@Column(nullable = false)
    @NotNull
	private Integer emptySleepTime = 1000;  // 无任务等待时间
	
	@Column(nullable = false)
	@NotBlank
	private String domain;

	private Integer frequency = FrequencyConstant.DEFAULT;  // 任务默认频率频率
	
	@Column(nullable = false)
	@NotNull
	private Boolean httpProxyEnabled = false;

	@Column(nullable = false)
	@NotNull
	private Boolean vpsHttpProxyEnabled = false;

	@Column(nullable = false)
	@NotNull
	private Boolean kdlHttpProxyEnabled = false;

	@Column(nullable = false)
	@NotNull
	private Integer cycleRetryTimes = 1;  // Spider失败重试次数
	
	@Column(nullable = false)
    @NotNull
    private Integer retryDelayTime = 60;  // Spider失败, Request重试间隔时间, 秒
}
