package com.jinguduo.spider.data.table;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

/**
 * Created by lc on 2018/9/10
 */
@Data
@Table(name = "comic_bodong")
@Entity
public class ComicBodong implements Serializable {
    @Id
    @GeneratedValue
    private Integer id;

    private String code;

    private Date day;

    private Long readCount;

    private Long readPicCount;

    private Long collectCount;

    private Long commentCount;
}
