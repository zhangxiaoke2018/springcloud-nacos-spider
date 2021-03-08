package com.jinguduo.spider.data.table;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

import org.springframework.data.annotation.LastModifiedDate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;

@Entity
@Table(name = "douyin_video_logs", indexes = {
		@Index(name = "idx_aweme_id",  columnList="awemeId", unique = false),
		@Index(name = "idx_mid",  columnList="mid", unique = false),
		@Index(name = "idx_cid",  columnList="cid", unique = false),
		@Index(name = "idx_user_id",  columnList="userId", unique = false)})
@Data
@JsonPropertyOrder({
	"awemeId", "userId", "mid", "cid", "diggCount",  "commentCount", 
	"forwardCount", "shareCount", "updatedAt"})
@JsonIgnoreProperties(ignoreUnknown = true)
public class DouyinVideoStatistic implements Serializable {

	private static final long serialVersionUID = -6096991034799125456L;

	@Id
	@GeneratedValue
	private Long id;
	
	private Long awemeId;
	private Long userId;
	private Long mid;
	private Long cid;
	private Integer diggCount = 0;
	private Integer commentCount = 0;
	private Integer forwardCount = 0;
	private Integer shareCount = 0;
    
    @Column(updatable = false, columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    @LastModifiedDate
    private Timestamp updatedAt = new Timestamp(System.currentTimeMillis());
}
