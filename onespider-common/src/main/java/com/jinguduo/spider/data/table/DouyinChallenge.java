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
@Table(name = "douyin_challenges", indexes = {
		@Index(name = "idx_cid",  columnList="cid", unique = true)})
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DouyinChallenge implements Serializable {

	private static final long serialVersionUID = 1986342033791151893L;

	@Id
	private Long cid;
	//@GeneratedValue
	//private Integer Id;
	
	private Long authorId;
	private Integer userCount;
	private Long viewCount;

	private String authorName;  // nickname
	//@Column(columnDefinition = "varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL")
	private String name; // cha_name
	//@Column(columnDefinition = "TEXT CHARACTER SET utf8mb4 DEFAULT NULL")
	private String description;
	
	@Column(updatable = false, columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
	@CreatedDate
    private Timestamp createdAt = new Timestamp(System.currentTimeMillis());
    
    @Column(updatable = false, columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    @LastModifiedDate
    private Timestamp updatedAt = new Timestamp(System.currentTimeMillis());
}
