package com.jinguduo.spider.data.table;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.validator.constraints.NotBlank;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;

import lombok.Data;

@Entity
@Table(name = "platforms")
@Data
public class Platform implements Serializable {

	private static final long serialVersionUID = 3587826316740079180L;

	@Id
	@GeneratedValue
	private Integer id;
	@Column(length = 64)
	@NotBlank
	private String name;
	@Column(length = 32)
	private String symbol;

	@CreatedDate
	private Timestamp createdAt = new Timestamp(System.currentTimeMillis());
	@LastModifiedBy
	private Timestamp updatedAt = new Timestamp(System.currentTimeMillis());

}
