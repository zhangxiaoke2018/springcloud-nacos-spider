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
public class Audio {
	@Id
	@GeneratedValue
	private Integer id;
	
	private String name;

	private Integer platformId;
	
	private String code;

	private String category;
	
	private String publisher;
	
	private String introduction;
	
	private Integer isFinish;
	
	private String tags;
	
	private String cover;
	
	private Date releaseDate;
	
	private String originalAuthor;
}
