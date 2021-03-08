package com.jinguduo.spider.data.table;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Entity
@Table(name = "douyin_videos", indexes = {
		@Index(name = "idx_aweme_id",  columnList="awemeId", unique = true)
})
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DouyinVideo implements Serializable {

	private static final long serialVersionUID = -2194858131678272933L;
	
	@Id
	private Long awemeId;
	//@GeneratedValue
	//private Integer seq;
	
	private Long userId;
	private Long mid;
	private Integer createTime;
	private Integer awemeType;  // aweme_type
	private Integer mediaType;
	private Long cid;
	
	// statistic
	private Integer diggCount = 0;
	private Integer commentCount = 0;
	private Integer forwardCount = 0;
	private Integer shareCount = 0;
	
	private Integer width;
	private Integer height;
	
	private String uri;
	private String coverUri;
	//@Column(columnDefinition = "TEXT CHARACTER SET utf8mb4 DEFAULT NULL")
	private String description;  // desc
	
	@Column(insertable = false, updatable = false, columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
	@CreatedDate
    private Timestamp createdAt = new Timestamp(System.currentTimeMillis());
    
    @Column(insertable = false, updatable = false, columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    @LastModifiedDate
    private Timestamp updatedAt = new Timestamp(System.currentTimeMillis());
}
