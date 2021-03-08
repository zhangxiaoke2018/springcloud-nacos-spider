package com.jinguduo.spider.data.table;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.sql.Timestamp;

@Entity
@Table(name = "media_account")
@Data
public class MediaAccount implements Serializable {

    private static final long serialVersionUID = -5035868425401780318L;

    @Id
    @GeneratedValue
    private Integer id;
    /** 账户类别 1：weibo */
    private Integer accountType;
    private Timestamp createTime;
    private String domain;
    private String userName;
    private String password;
    private String cookie;
    private String userAgent;
    /** 登陆状态 0：未登录，1：已登录，-1：登录失败，-2：异常 */
    private Integer status;
    private String workIp;
    private String workMark;
    private Integer loginCount;
    private Integer failCount;
    private String headers;
    private Timestamp modfiyTime = new Timestamp(System.currentTimeMillis());

    public boolean isSuccess(){
        return 1 == this.status;
    }

    public boolean isFail(){
        return -1 == this.status;
    }
    public boolean isOverDue(){
        return 2 == this.status;
    }
}
