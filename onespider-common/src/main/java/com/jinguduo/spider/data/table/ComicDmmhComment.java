package com.jinguduo.spider.data.table;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * Created by lc on 2018/9/10
 */
@Data
@Table(name = "comic_dmmh_comment")
@Entity
public class ComicDmmhComment implements Serializable {

    @Id
    @GeneratedValue
    private Long id;

    private String code;

    private Integer episode;

    private Integer commentCount;
}
