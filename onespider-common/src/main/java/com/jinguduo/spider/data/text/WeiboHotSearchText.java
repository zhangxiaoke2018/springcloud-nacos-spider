package com.jinguduo.spider.data.text;

import java.io.Serializable;
import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;

@JsonPropertyOrder({ "text", "hotValue", "crawledAt"})
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WeiboHotSearchText implements Serializable {
	/**
	* 
	*/
	private static final long serialVersionUID = 1L;

	private String text;
	private Integer hotValue;
	private Timestamp crawledAt;

}
