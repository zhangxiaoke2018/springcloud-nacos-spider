package com.jinguduo.spider.data.table;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * Created by lc on 2019/4/9
 */
@Entity
@Table
@Data
public class StockCompany implements Serializable {

    @Id
    @GeneratedValue
    private Integer id;

    private String code;

}
