package com.jinguduo.spider.data.table;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * I'm a cute code dog
 * User: LingChen
 * Date:2017/7/3
 * Time:16:16
 */
@Entity
@Table(name = "vip_episode")
@Data
public class VipEpisode implements Serializable {

    private static final long serialVersionUID = -5707537420575476270L;
    @Id
    @GeneratedValue
    private Integer id;
    private String code;
    private Integer platformId;
    private Date vipStartTime;
    private Date vipEndTime;

    @Column(name = "create_time", updatable = false)
    private Date createTime;

    @Column(name = "update_time", updatable = false)
    private Date updateTime;
}
