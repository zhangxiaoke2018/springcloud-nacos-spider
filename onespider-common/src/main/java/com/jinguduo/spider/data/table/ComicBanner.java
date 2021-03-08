package com.jinguduo.spider.data.table;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * Created by lc on 2019/8/12
 */
@Data
@Table
@Entity
@NoArgsConstructor
public class ComicBanner {
    @Id
    @GeneratedValue
    private Integer id;

    private String code;

    private Integer platformId;

    private Date day;

    private String name;

    private String source;

    public ComicBanner(String code, Integer platformId, Date day, String name, String source) {
        this.code = code;
        this.platformId = platformId;
        this.day = day;
        this.name = name;
        this.source = source;
    }
}
