package com.jinguduo.spider.data.table;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * I'm a cute code dog
 * User: LingChen
 * Date:2017/7/31
 * Time:17:06
 */
@Entity
@Table(name = "comic_kuaikan")
@Data
public class ComicKuaiKan implements Serializable {

    private static final long serialVersionUID = -4864379763572742235L;
    @Id
    @GeneratedValue
    @Column(name = "id")
    private Integer id;

    @Column(name = "code")
    private String code;

    @Column(name = "hot_num")
    private Long hotNum;

    @Column(name = "praise_num")
    private Long praiseNum;

    @Column(name = "comment_num")
    private Long commentNum;

    @Column(name = "fav_count")
    private Long favCount;

    @Column(name = "create_time", updatable = false)
    private Date create_time;

    @Column(name = "update_time", updatable = false)
    private Date update_time;


}
