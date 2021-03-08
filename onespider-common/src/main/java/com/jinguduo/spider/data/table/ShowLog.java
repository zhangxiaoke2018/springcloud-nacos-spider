package com.jinguduo.spider.data.table;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.data.annotation.CreatedDate;

import lombok.Data;

@Entity
@Table(name = "show_logs")
@Data
public class ShowLog implements Serializable {

	private static final long serialVersionUID = -1833413590436072844L;

	@Id
	@GeneratedValue
	private Long id;
	
	private Integer showId;
	private Integer platformId;
	private Integer seedId;

	@Column(nullable = false)
	private Long playCount;
	
	@CreatedDate
	private Timestamp crawledAt = new Timestamp(System.currentTimeMillis());
	
	@Column(length = 64)
	private String code;
}
