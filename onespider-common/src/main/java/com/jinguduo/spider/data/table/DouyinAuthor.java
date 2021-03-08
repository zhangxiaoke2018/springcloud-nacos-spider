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
@Table(name = "douyin_authors", indexes = {
		@Index(name = "idx_user_id",  columnList="userId", unique = true)})
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DouyinAuthor implements Serializable {

	private static final long serialVersionUID = 4548044075346583298L;
	
	@Id
	private Long userId;

	//@GeneratedValue
	//private Integer id;
	private Integer shortId; // short_id
	
	// statistics
	private Integer awemeCount;  // 作品 
	private Integer dongtaiCount;  // ?
	private Integer favoritingCount;  // 喜欢
	private Integer followerCount; // 粉丝
	private Integer followingCount;  // 关注
	private Integer totalFavorited;  // 获赞
	
	private Integer gender;
	private Integer constellation;
	private Integer commerceUserLevel;  //commerce_user_level
	private Integer verificationType;  // verification_type
	private Boolean govMediaVip;
	
	//@Column(columnDefinition = "varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL")
	private String nickname;
	private String avatarUri;
	private String birthday;
	private String region;
	private String location;
	//@Column(columnDefinition = "TEXT CHARACTER SET utf8mb4 DEFAULT NULL")
	private String signature;
	private String weiboName;
	private String weiboUrl;
	//@Column(columnDefinition = "varchar(64) CHARACTER SET utf8mb4 DEFAULT NULL")
	private String enterpriseVerifyReason;  // enterprise_verify_reason
	
	@Column(insertable = false, updatable = false, columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
	@CreatedDate
    private Timestamp createdAt = new Timestamp(System.currentTimeMillis());
    
    @Column(insertable = false, updatable = false, columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    @LastModifiedDate
    private Timestamp updatedAt = new Timestamp(System.currentTimeMillis());
}
