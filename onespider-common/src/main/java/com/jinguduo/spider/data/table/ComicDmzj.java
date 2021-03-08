package com.jinguduo.spider.data.table;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

/**
 * I'm a cute code dog
 * User: LingChen
 * Date:2017/11/4
 * Time:17:44
 */
@Data
@Table(name = "comic_dmzj")
@Entity
public class ComicDmzj implements Serializable {

    private static final long serialVersionUID = -2360601953101404862L;
    @Id
    @GeneratedValue
    private Integer id;

    private String code;

    private Integer hotTotal;

    private Integer apphotTotal;

    private Integer hitTotal;

    private Integer apphitTotal = 0;

    private Integer totalAddNum;

    private Integer copyright;

    private Integer hotHits;

    private Integer hits;

    private Integer voteAmount;

    private Integer subAmount;

    private Integer commentCount;

    private Date day;
}
