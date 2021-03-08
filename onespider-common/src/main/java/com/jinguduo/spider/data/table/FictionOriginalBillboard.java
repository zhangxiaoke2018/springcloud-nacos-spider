package com.jinguduo.spider.data.table;

import java.io.Serializable;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;
import java.util.List;

@Data
@Entity
@Table(name = "fiction_original_billboard")
public class FictionOriginalBillboard implements Serializable {

    /**
    * id
    */
    @Id
    @GeneratedValue
    private Integer id;

    /**
    * platform_id
    */
    private Integer platformId;

    /**
    * type
    */
    private String type;

    /**
    * day
    */
    private Date day;

    /**
    * rank
    */
    private Integer rank;

    /**
    * code
    */
    private String code;

    private Date billboardUpdateTime;

}