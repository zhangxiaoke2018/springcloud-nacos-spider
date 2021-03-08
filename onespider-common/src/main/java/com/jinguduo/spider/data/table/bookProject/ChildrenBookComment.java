package com.jinguduo.spider.data.table.bookProject;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

/**
 * Created by lc on 2019/11/29
 */
@Data
@Table(name = "children_book_comment")
@Entity
public class ChildrenBookComment implements Serializable {
    @Id
    @GeneratedValue
    private Integer id;
    private Integer platformId;
    private String code;
    private Date day;
    private Integer  commentCount;
    private Integer  greatCount;
    private Integer  indifferentCount;
    private Integer  detestCount;
    private Float  goodRate;
}
