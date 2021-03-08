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
@Table(name = "douyin_musics", indexes = {
		@Index(name = "idx_mid",  columnList="mid", unique = true)})
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DouyinMusic implements Serializable {

	private static final long serialVersionUID = -8466066306150400808L;

	@Id
	private Long mid;
	//@GeneratedValue
	//private Integer id;
	
	private Integer userCount;
	private Integer source;
	private Integer status;
	private Integer duration;
	private Long ownerId;
	private Boolean original;
	
	//@Column(columnDefinition = "varchar(128) CHARACTER SET utf8mb4 DEFAULT NULL")
	private String title;  // title
	//@Column(columnDefinition = "varchar(32) CHARACTER SET utf8mb4 DEFAULT NULL")
	private String author;
	//@Column(columnDefinition = "varchar(128) CHARACTER SET utf8mb4 DEFAULT NULL")
	private String album;
	private String owner;
	private String coverUri;
	private String playUri;
	
	@Column(insertable = false, updatable = false, columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
	@CreatedDate
    private Timestamp createdAt = new Timestamp(System.currentTimeMillis());
    
    @Column(insertable = false, updatable = false, columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    @LastModifiedDate
    private Timestamp updatedAt = new Timestamp(System.currentTimeMillis());
}
