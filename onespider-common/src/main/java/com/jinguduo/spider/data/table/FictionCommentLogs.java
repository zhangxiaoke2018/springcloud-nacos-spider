package com.jinguduo.spider.data.table;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import com.jinguduo.spider.common.util.DateHelper;

import lombok.Data;

@Data
@Entity
@Table(name = "fiction_comment_logs")
public class FictionCommentLogs implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1722315301342908214L;

	@Id
	@GeneratedValue
	private Integer id;

	private String code;

	private Integer commentCount;

	private Date day = DateHelper.getTodayZero(Date.class);

	private Integer platformId;

	private Timestamp updateTime = new Timestamp(System.currentTimeMillis());

}
