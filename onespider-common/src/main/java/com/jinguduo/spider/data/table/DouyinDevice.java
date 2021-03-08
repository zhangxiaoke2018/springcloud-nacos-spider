package com.jinguduo.spider.data.table;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.springframework.data.annotation.CreatedDate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Entity
@Table(name = "douyin_devices", indexes = {
		@Index(name = "idx_device_id",  columnList="deviceId", unique = true)})
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DouyinDevice implements Serializable {

	private static final long serialVersionUID = -3689438577979874693L;

	@Id
	@GeneratedValue
	private Integer id;
	//private Integer seed;
	
	@Column(length = 32)
	private String deviceId;
	@Column(length = 32)
	private String installId;
	@Column(length = 32)
	private String openudid;
	@Column(length = 32)
	private String uuid;
	
	@JsonIgnore
	@Transient
	private String channel = "xiaomi";
	@JsonIgnore
	@Transient
	private String deviceType = "MI5X";
	@JsonIgnore
	@Transient
	private String deviceBrand = "Xiaomi";
	@JsonIgnore
	@Transient
	private String versionName = "166";
	@JsonIgnore
	@Transient
	private String versionCode = "1.6.6";
	@JsonIgnore
	@Transient
	private String manifestVersionCode = "166";
	@JsonIgnore
	@Transient
	private String updateVersionCode = "1662";
	
	
	@Column(insertable = false, updatable = false, columnDefinition="TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @CreatedDate
    private Timestamp createdAt = new Timestamp(System.currentTimeMillis());
}
