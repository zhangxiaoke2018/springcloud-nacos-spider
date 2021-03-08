package com.jinguduo.spider.data.table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * Created by lc on 2019/4/9
 */
@Entity
@Table
@Data
@NoArgsConstructor
public class StockBulletin implements Serializable {

    @Id
    @GeneratedValue
    @Column(name = "id")
    private Integer id;

    private String code;
    private String companyCode;
    private String title;
    private String url;
    private Date publishTime;

    public StockBulletin(String code, String companyCode, String title, String url, Date publishTime) {
        this.code = code;
        this.companyCode = companyCode;
        this.title = title;
        this.url = url;
        this.publishTime = publishTime;
    }
}
