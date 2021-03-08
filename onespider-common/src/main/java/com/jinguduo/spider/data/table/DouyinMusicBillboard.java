package com.jinguduo.spider.data.table;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Entity
@Table(name = "douyin_music_billboards", indexes = {
		@Index(name = "idx_activetime_ordinal",  columnList="activeTime,ordinal", unique = true)})
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DouyinMusicBillboard implements Serializable {

	private static final long serialVersionUID = -2616081451408291436L;

	@Id
	@GeneratedValue
	private Integer Id;
	
	private Integer ordinal;  // 从1开始计数
	private Integer hotValue;
	private Integer label;  // 0:None, 1:新, 2:荐, 3:热
	private Long mid;  // music id
	private Boolean original;
	
	private String activeTime;
	private String title;
	private String author;
	private String uri;
	private String coverUri;
	
	@Column(updatable = false, columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
	@CreatedDate
    private Timestamp createdAt = new Timestamp(System.currentTimeMillis());
    
    @Column(updatable = false, columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    @LastModifiedDate
    private Timestamp updatedAt = new Timestamp(System.currentTimeMillis());
}
