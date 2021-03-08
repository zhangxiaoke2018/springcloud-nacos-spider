package com.jinguduo.spider.data.table;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
@Entity
@Table(name = "fictions")
public class Fiction implements Serializable {
	/**
	* 
	*/
	private static final long serialVersionUID = 4916393869455457404L;

	@Id
	@GeneratedValue
	private Integer id;
	
	private String author;

	private Integer channel;

	private Integer isFinish;

	private String name;

	@JsonInclude()
	@Transient
	private Integer platformId;
	
	@JsonInclude()
	@Transient
	private String code;

	private Integer totalLength;
	
	private String cover;
	
	private String intro;
	
	private String tags;//处理后以/隔开

	private Timestamp updateTime = new Timestamp(System.currentTimeMillis());

}
