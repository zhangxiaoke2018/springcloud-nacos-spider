package com.jinguduo.spider.data.table;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

/**
 * Created by lc on 2019/11/21
 */
@Data
@Entity
@Table(name = "comic_bilibili")
@NoArgsConstructor
public class ComicBilibili implements Serializable {


    @Id
    @GeneratedValue
    private Integer id;

    private String code;

    private Date day;

    private Integer commentCount;

    private Integer monthTickets;

    private Integer fans;

    public ComicBilibili(String code, Date day, Integer commentCount) {
        this.code = code;
        this.day = day;
        this.commentCount = commentCount;
    }
}

