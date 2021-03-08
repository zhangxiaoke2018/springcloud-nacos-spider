package com.jinguduo.spider.data.table;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 23/06/2017 14:32
 */
@Data
@Entity
@Table
public class OpinionWordsFeature implements Serializable {

    private static final long serialVersionUID = -2675477482650276937L;

    @Id
    @GeneratedValue
    private Integer id;


    private String category;

    private String subject;

    private String relatedKeywords;

}
