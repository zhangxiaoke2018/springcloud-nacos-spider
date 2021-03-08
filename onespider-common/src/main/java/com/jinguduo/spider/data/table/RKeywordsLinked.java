package com.jinguduo.spider.data.table;

import lombok.Data;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Collection;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 16/01/2017 10:21 AM
 */
@Entity
@Data
@Table(name = "r_keywords_linked")
public class RKeywordsLinked implements Serializable {

    private static final long serialVersionUID = -3447133614401580198L;

    @Id
    @GeneratedValue
    private Integer id;

    private Integer keywordsId;

    private Integer linkedId;

    public RKeywordsLinked(){}

    public RKeywordsLinked(Integer linkedId, Integer keywordsId){
        this.linkedId = linkedId;
        this.keywordsId = keywordsId;
    }


}
