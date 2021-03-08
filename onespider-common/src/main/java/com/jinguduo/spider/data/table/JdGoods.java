package com.jinguduo.spider.data.table;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * Created by lc on 2019/10/31
 */
@Entity
@Table(name = "jd_goods")
@Data
public class JdGoods {
    @Id
    @GeneratedValue
    private Integer id;

    private Date day;

    private String goodsId;

    private String title;

    private Integer commentCount;

    private Integer afterCount;

    private Integer goodCount;

    private Integer defaultGoodCount;

    private Integer generalCount;

    private Integer poorCount;
}
