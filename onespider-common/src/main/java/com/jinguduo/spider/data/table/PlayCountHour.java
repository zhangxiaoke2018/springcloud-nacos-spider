package com.jinguduo.spider.data.table;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

/**
 * 
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @author liuxinglong
 * @DATE 2017年7月28日 下午4:42:27
 *
 */
@Entity
@Table(name = "play_count_hour")
@Data
public class PlayCountHour implements Serializable {

    private static final long serialVersionUID = -7855211856699994656L;

    @Id
    @GeneratedValue
    private Integer id;

    private Integer showId;

    private Integer platformId;

    private Long playCount;

    private Timestamp crawledAt;

    private String category;

    private Boolean failure = Boolean.FALSE;

    public PlayCountHour(){}

    public PlayCountHour(Long playCount){
        this.playCount = playCount;
    }

    public PlayCountHour(Long playCount,Timestamp crawledAt){
        this.playCount = playCount;
        this.crawledAt = crawledAt;
    }

}
