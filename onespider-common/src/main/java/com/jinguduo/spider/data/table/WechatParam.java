package com.jinguduo.spider.data.table;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * @DATE 2018/10/11 11:08 AM
 */
@Table(name = "wechat_params")
@Entity
@Data
public class WechatParam implements Serializable {

    private static final long serialVersionUID = -3336008554651617243L;

    @Id
    @GeneratedValue
    private Integer id;

    private String paramKey;

    private String paramValue;

}
