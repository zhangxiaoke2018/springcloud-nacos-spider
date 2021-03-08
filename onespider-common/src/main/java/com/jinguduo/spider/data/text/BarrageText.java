package com.jinguduo.spider.data.text;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * 弹幕文本
 * Created by Baohao on 2016/12/26.
 */

@JsonPropertyOrder({
    "showId", "platformId", "code", "barrageId", "userId", 
    "nickName", "showTime", "createdTime", "up", "replyCount", 
    "isReplay", "replyBarrageId", "content"
})
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BarrageText implements Serializable{

    private static final long serialVersionUID = -3831125315548894446L;

    private Integer showId;
    private Integer platformId;
    private String code;

    private String barrageId;  // 弹幕Id
    private String userId;  // 用户id
    private String nickName;  // 用户昵称
    private Long showTime;  // 出现时间(单位:秒)
    private Timestamp createdTime;  // 弹幕创建时间(添加时间)
    private Integer up;  // 点赞量
    private Integer replyCount;  // 回复数
    private Boolean isReplay = false;  // 是否是回复弹幕
    private String replyBarrageId;  // 回复弹幕Id
    private String content;  // 文本


    public BarrageText() {
    }

    /**
     * 基本构造器
     */
    public BarrageText(String barrageId, String userId, String nickName, Long showTime, Timestamp createdTime, Integer up, String content) {
        this.barrageId = barrageId;
        this.userId = userId;
        this.nickName = nickName;
        this.showTime = showTime;
        this.createdTime = createdTime;
        this.up = up;
        this.content = content;
    }
    
    public void setContent(String s) {
        if (s != null) {
            s = s.replaceAll("\\r|\\n|\\t", "").trim();
        }
        this.content = s;
    }


}
