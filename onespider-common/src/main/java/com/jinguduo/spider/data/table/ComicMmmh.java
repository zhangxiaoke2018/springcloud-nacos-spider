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
 * Date:2017/11/7
 * Time:18:25
 */
@Data
@Table(name = "comic_mmmh")
@Entity
public class ComicMmmh implements Serializable {

    private static final long serialVersionUID = 6389555017868054349L;
    @Id
    @GeneratedValue
    private Integer id;

    private String code;

    private Date day;

    private Integer likesNum;

    private Integer readsNum;

    private Integer commentNum;
}
