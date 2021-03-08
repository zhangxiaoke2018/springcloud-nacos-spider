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
@Table(name = "relation_keywords")
public class RelationKeywords implements Serializable {
    private static final long serialVersionUID = -7533600644605652322L;
    @Id
    @GeneratedValue
    private Integer id;
    private Integer relevanceId;
    private String relationWords;
    private Byte type;
    private String classify;
    private Integer modify;
}
