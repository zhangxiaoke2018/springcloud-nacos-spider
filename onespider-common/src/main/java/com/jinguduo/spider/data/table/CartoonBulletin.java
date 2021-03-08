package com.jinguduo.spider.data.table;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

/**
 * Created by lc on 2020/4/9
 */
@Data
@Table(name = "cartoon_bulletin")
@Entity
public class CartoonBulletin implements Serializable {

    @Id
    @GeneratedValue
    private Integer id;
    private String url;
    private String title;
    private Date day;
    private Integer status;
}

