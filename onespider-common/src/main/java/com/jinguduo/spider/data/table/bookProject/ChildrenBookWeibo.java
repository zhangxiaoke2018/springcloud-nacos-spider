package com.jinguduo.spider.data.table.bookProject;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 28/06/2017 14:42
 */
@Entity
@Table(name = "children_book_weibo")
@Data
public class ChildrenBookWeibo implements Serializable {

    @Id
    @GeneratedValue
    private Integer id;

    private String mid;

    private String code;

    private Integer platformId;

    private Date postTime;

    private String queryName;

    private Integer zhuan;
    private Integer ping;
    private Integer zan;
}
