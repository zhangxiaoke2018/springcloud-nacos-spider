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
@Table
@Data
public class ComicAuthorRelation implements Serializable {

    @Id
    @GeneratedValue
    private Integer id;

    private Integer platformId;

    private String comicCode;

    private String authorName;

    private String authorId;

    private Integer status = 0;


}
