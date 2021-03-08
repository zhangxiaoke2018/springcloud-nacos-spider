package com.jinguduo.spider.data.table;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(name = "fiction_code_relation")
public class FictionCodeRelation {
	@Id
	@GeneratedValue
	private int id;

	private Integer fictionId;

	private String code;

	private Integer platformId;
}
