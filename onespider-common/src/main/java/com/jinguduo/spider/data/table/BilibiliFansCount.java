package com.jinguduo.spider.data.table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @DATE 27/07/2017 10:51
 */
@Data
@Entity
@Table
public class BilibiliFansCount implements Serializable {

    @Id
    @GeneratedValue
    private Integer id;

    private String code;

    public Double score;

    public Integer scoreNumber;

    private Integer totalPlayCount;

    private Integer totalFansCount;

    private Integer totalDanmuCount;

    private Date day;
}
