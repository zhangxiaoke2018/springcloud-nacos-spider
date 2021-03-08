package com.jinguduo.spider.data.table;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * Created by lc on 2019/4/30
 */
@Entity
@Table(name = "wechat_sogou_cookie")
@Data
public class WechatSogouCookie implements Serializable {

    @Id
    @GeneratedValue
    private Integer id;

    private String cookie;
}
