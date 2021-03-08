package com.jinguduo.spider.data.table;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * I'm a cute code dog
 * User: LingChen
 * Date:2017/10/20
 * Time:10:17
 */
@Data
@Table(name = "comic_zymk")
@Entity
public class ComicZymk implements Serializable {
    private static final long serialVersionUID = -1333484945882010797L;
    @Id
    @GeneratedValue
    @Column(name = "id")
    private Integer id;

    @Column(name = "code")
    private String code;

    @Column(name = "day")
    private Date day;

    private Long readCount;
    private Integer collectCount;
    private Integer rewardCount;
    private Integer monthlyTicket;
    private Integer recommendCount;
    private Integer commentCount;

    @Column(name = "create_time", updatable = false)
    private Date create_time;

    @Column(name = "update_time", updatable = false)
    private Date update_time;
}
