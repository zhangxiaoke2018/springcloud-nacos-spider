package com.jinguduo.spider.data.text;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * Created by gsw on 2017/2/17.
 */
@JsonPropertyOrder({
    "showId", "platformId", "code", "commentId", "userId", "nickName",
     "createdTime", "up", "replyCount", "replyCommentId", "content",
})
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CommentText implements Serializable {

    private static final long serialVersionUID = 7365253102227964011L;

    private Integer showId;
    private Integer platformId;
    private String code;

    private String commentId;  // 评论Id
    private String userId;  // 用户id
    private String nickName;  // 用户昵称
    private Timestamp createdTime;  // 创建时间(添加时间)
    private Integer up;  // 点赞量
    private Integer replyCount;  // 回复数
    private String replyCommentId;  // 回复评论Id
    private String content;  // 评论文本

    public  CommentText(){}

    public CommentText(String commentId, String userId, String nickName, Timestamp createdTime, Integer up, Integer replyCount, String content){

        this.commentId = commentId;
        this.userId = userId;
        this.nickName = nickName;
        this.createdTime = createdTime;
        this.up = up;
        this.replyCount = replyCount;
        this.content = content;
    }

}
