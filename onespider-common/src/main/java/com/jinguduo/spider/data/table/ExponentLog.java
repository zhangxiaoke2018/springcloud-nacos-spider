package com.jinguduo.spider.data.table;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Table(name = "exponent_logs")
@Data
public class ExponentLog implements Serializable {

    private static final long serialVersionUID = 8265857076764438598L;

    @Id
    @GeneratedValue
    private Long id;
    private String code;
    private Date exponentDate;/** 指数日期 */
    private Long exponentNum;/** 指数数目 */
    private Timestamp createTime = new Timestamp(System.currentTimeMillis());

    public ExponentLog() {}

    public ExponentLog(Date exponentDate, Long exponentNum) {
        super();
        this.exponentDate = exponentDate;
        this.exponentNum = exponentNum;
    }
}
