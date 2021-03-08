package com.jinguduo.spider.data.text;


import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;

@JsonPropertyOrder({
    "code", "platformId", "commentId","chapterId", "userName",
     "commentRate","replyCount", "content", "createTime"
})
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FictionCommentText {
	private String code;
	private Integer platformId;
	private String commentId;
	private String chapterId;
	private String userName;
	private String content;
	private Float commentRate;
	private Integer replyCount;
	private Timestamp createTime;
}
