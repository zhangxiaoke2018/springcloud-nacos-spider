package com.jinguduo.spider.data.table;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

@Entity
@Table(name = "wechat_sougou_logs")
@Data
public class WechatSougouLog implements Serializable {

	private static final long serialVersionUID = -1439447609172829302L;

	@Id
	@GeneratedValue
	private Long id;
	
	@Column(nullable = false)
	private Integer count;
	
	@CreatedDate
	private Timestamp crawledAt = new Timestamp(System.currentTimeMillis());
	
	@Column(length = 64)
	private String code;

	private Date day;
}
