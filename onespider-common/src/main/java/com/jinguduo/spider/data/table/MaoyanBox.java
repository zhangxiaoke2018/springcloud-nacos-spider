package com.jinguduo.spider.data.table;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * Created by lc on 2017/5/10.
 */

@Entity
@Table(name = "maoyan_box")
@Data
public class MaoyanBox implements Serializable {

    private static final long serialVersionUID = 2244652712708495394L;
    @Id
    @GeneratedValue
    private Integer id;

    private String category;

    private Date day;

    private Integer dailyBox;

    private Integer movieId;

    private String movieName;

    private Integer releaseDays;

    private Integer sumBox;

    private Integer weeklyBox;

    @Column(name = "create_time", updatable = false)
    private Date createTime;

    @Column(name = "update_time", updatable = false)
    private Date updateTime;

}