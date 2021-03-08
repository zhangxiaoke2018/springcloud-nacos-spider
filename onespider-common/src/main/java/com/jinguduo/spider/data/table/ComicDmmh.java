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
@Table(name = "comic_dmmh")
@Entity
public class ComicDmmh implements Serializable {
    @Id
    @GeneratedValue
    private Integer id;

    private String code;

    private Long readCount;

    private Long favoriteCount;

    private Long mana;

    private Long likeitCount;

}
