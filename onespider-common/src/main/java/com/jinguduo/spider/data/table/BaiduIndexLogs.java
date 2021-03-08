package com.jinguduo.spider.data.table;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @TODO
 * @DATE 05/12/2016 7:31 PM
 */
@Entity
@Table(name = "baidu_index_logs")
@Data
public class BaiduIndexLogs implements Serializable {

    private static final long serialVersionUID = 5490355439232771995L;

    @Id
    @GeneratedValue
    private Long id;

    private String keyword;

    private Integer exponent;

    private Date day;

}
