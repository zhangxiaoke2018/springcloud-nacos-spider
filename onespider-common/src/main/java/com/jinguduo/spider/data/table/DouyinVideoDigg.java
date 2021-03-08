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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;

@Entity
@Table(name = "douyin_video_diggs", indexes = {
		@Index(name = "idx_aweme_user",  columnList="awemeId,userId", unique = true)})
@Data
@JsonPropertyOrder({"userId", "awemeId", "authorId"})
@JsonIgnoreProperties(ignoreUnknown = true)
public class DouyinVideoDigg implements Serializable {

	private static final long serialVersionUID = -5172726087854360323L;

	@Id
	@GeneratedValue
	private Long id;
	
	private Long userId;
	private Long awemeId;
	private Long authorId;
	
	@Column(insertable = false, updatable = false, columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
	@CreatedDate
    private Timestamp crawledAt = new Timestamp(System.currentTimeMillis());
}
