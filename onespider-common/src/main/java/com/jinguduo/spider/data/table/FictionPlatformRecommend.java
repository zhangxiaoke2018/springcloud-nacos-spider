package com.jinguduo.spider.data.table;

import java.sql.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import com.jinguduo.spider.common.util.DateHelper;

import lombok.Data;

@Data
@Entity
@Table(name = "fiction_platform_recommend")
public class FictionPlatformRecommend {
	@Id
	@GeneratedValue
	private Integer id;
	
	private String code;

	private Integer platformId;
	
	private Integer recommendCount;

	private Date day =	DateHelper.getTodayZero(Date.class);
}
