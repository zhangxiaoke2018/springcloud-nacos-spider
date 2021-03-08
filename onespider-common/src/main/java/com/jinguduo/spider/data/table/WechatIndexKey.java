package com.jinguduo.spider.data.table;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

/**
 * @DATE 2018/10/11 11:08 AM
 */
@Table(name = "wechat_index_key")
@Entity
@Data
public class WechatIndexKey implements Serializable {

    private static final long serialVersionUID = 5968397790864249861L;

    @Id
    @GeneratedValue
    private Integer id;

    private String openId;

    private String searchKey;

    private Date keyUpdatedTime;

    @CreatedDate
    private Timestamp createdAt = new Timestamp(System.currentTimeMillis());

    public WechatIndexKey() {
    }

    public WechatIndexKey(String openId, String searchKey) {
        this.openId = openId;
        this.searchKey = searchKey;
    }
}
