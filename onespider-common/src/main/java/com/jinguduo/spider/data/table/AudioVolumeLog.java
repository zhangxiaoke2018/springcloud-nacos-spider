package com.jinguduo.spider.data.table;

import java.sql.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Data
@Table
public class AudioVolumeLog {
	@Id
	@GeneratedValue
	private int id;
	
	private String code;
	
	private Integer platformId;
	
	private Integer volumes;
	
	private Date day = new Date(System.currentTimeMillis());
	
}
