package com.jinguduo.spider.data.table;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * Created by lc on 2017/6/7.
 */
@Data
@Entity
@Table(name = "alias" )
public class Alias implements Serializable {
    private static final long serialVersionUID = -805447789959754294L;
    @Id
    @GeneratedValue
    private Long id;
    private Integer relevanceId;
    private String code;
    private String alias;
    private String classify;
    private String category;
    private Byte type;//0剧;1演员
}
