package com.jinguduo.spider.data.table;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * I'm a cute code dog
 * User: LingChen
 * Date:2017/8/2
 * Time:16:20
 */
@Entity
@Table(name = "comic_tengxun_comment")
@Data
public class ComicTengxunComment implements Serializable {

    private static final long serialVersionUID = 5037197758337062868L;
    @Id
    @GeneratedValue
    @Column(name = "id")
    private Integer id;

    @Column(name = "code")
    private String code;

    @Column(name = "comment_num")
    private Integer commentNum;

    @Column(name = "create_time", updatable = false)
    private Date create_time;

    @Column(name = "update_time", updatable = false)
    private Date update_time;


}
