package com.jinguduo.spider.data.table;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 28/06/2017 14:42
 */
@Entity
@Table(name = "weibo_texts")
@Data
public class WeiboText implements Serializable {

    private static final long serialVersionUID = -4694471762113068436L;

    @Id
    @GeneratedValue
    private Integer id;

    private String mid;

    private String code;

    private String url;

    private String userId;

    private String userType;

    private Integer isForward;

    private String nickName;

    private String touxiang;

    private String content;

    private String location;

    private Date postTime;

    private String postPlatform;

    private Integer zhuan;

    private Integer ping;

    private Integer zan;

    // 转发微博信息
    private String originalMid;

    private String originalUrl;

    private String originalUserId;

    private String originalUserType;

    private String originalNickName;

    private String originalContent;

    private String originalLocation;

    private String originalPostTime;

    private String originalPostPlatform;

    private Integer originalZhuan;

    private Integer originalPing;

    private Integer originalZan;

    private Timestamp crawledAt = new Timestamp(System.currentTimeMillis());

    public void setContent(String s) {
        if (s != null) {
            s = s.replaceAll("\\r|\\n|\\t", "").trim();
        }
        this.content = s;
    }

    public String getContent(){
        if (this.content != null) {
            return this.content.replaceAll("\\r|\\n|\\t", "").trim();
        }
        return this.content;
    }

    public String getOriginalContent() {
        if (this.originalContent != null) {
            return this.originalContent.replaceAll("\\r|\\n|\\t", "").trim();
        }
        return this.originalContent;
    }

    public void setOriginalContent(String originalContent) {

        if (originalContent != null) {
            originalContent = originalContent.replaceAll("\\r|\\n|\\t", "").trim();
        }
        this.originalContent = originalContent;

    }
}
