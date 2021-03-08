package com.jinguduo.spider.data.table;

import java.sql.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import com.jinguduo.spider.common.util.DateHelper;
import com.jinguduo.spider.common.util.DateUtil;

import lombok.Data;
@Data
@Entity
@Table(name = "fiction_platform_favorite")
public class FictionPlatformFavorite {
	@Id
	@GeneratedValue
	private Integer id;
	
	private String code;

	private Integer platformId;
	
	private Integer favoriteCount;

	private Date day = DateHelper.getTodayZero(Date.class);
}
