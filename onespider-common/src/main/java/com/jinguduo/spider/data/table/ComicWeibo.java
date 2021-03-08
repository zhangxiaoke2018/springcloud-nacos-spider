package com.jinguduo.spider.data.table;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

@Data
@Table(name="comic_weibo")
@Entity
public class ComicWeibo implements Serializable {
    private static final long serialVersionUID = 6130540983294616265L;
    @Id
    @GeneratedValue
    private Integer id;
    private String code;
    private Long clickNum;
    private Long favoriteNum;
    private Long commentNum;
    private Long hotNum;
    private Date day;

}
