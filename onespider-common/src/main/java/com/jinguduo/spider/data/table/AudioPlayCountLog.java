package com.jinguduo.spider.data.table;

import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.data.annotation.CreatedDate;

import lombok.Data;

@Entity
@Data
@Table
public class AudioPlayCountLog {
	@Id
	@GeneratedValue
	private int id;
	
	private String code;
	
	private Integer platformId;
	
	@Column(nullable = false)
	private Long playCount;
	
	@CreatedDate
	private Timestamp crawledAt = new Timestamp(System.currentTimeMillis());
}
