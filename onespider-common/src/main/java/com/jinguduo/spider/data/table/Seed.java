package com.jinguduo.spider.data.table;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import com.jinguduo.spider.common.constant.FrequencyConstant;
import com.jinguduo.spider.common.constant.StatusEnum;

import lombok.Data;

@Entity
@Table(name = "seeds", indexes = {@Index(name = "idx_url_code",  columnList="url,code", unique = true)})
@Data
public class Seed implements Serializable {

	private static final long serialVersionUID = -8656990746983847499L;

	@Id
	@GeneratedValue
	private Integer id;

	private Integer platformId;
	@NotNull
	private Integer priority = 1;
	@NotNull
	private Integer frequency = FrequencyConstant.BLANK;
	@NotBlank
	private String code;
	@NotBlank
	private String url;
	private String method = "GET";
	@NotNull
	private Integer status = StatusEnum.STATUS_OK.getValue();//0表示正常 -1表示删除

	@Column(updatable = false)
	@CreatedDate
	private Timestamp createdAt = new Timestamp(System.currentTimeMillis());
	@Column(updatable = false)
	@LastModifiedDate
	private Timestamp updatedAt = new Timestamp(System.currentTimeMillis());

}
