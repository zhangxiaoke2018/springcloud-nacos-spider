package com.jinguduo.spider.data.table;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by lc on 2017/5/5.
 */
@Entity
@Table(name = "index_wechat_logs")
@Data
public class IndexWechatLogs implements Serializable {

    private static final long serialVersionUID = 3450762023590671975L;
    @Id
    @GeneratedValue
    @Column(name="id")
    private Integer id;

    @NotBlank
    @Column(name="code")
    private String code;

    @Column(name="index_day")
    private Date indexDay;

    @Column(name="index_count")
    private BigDecimal indexCount;

    @Column(name="created_at",updatable = false)
    private Date createdAt;

    @Column(name="updated_at",updatable = false)
    private Date updatedAt;



}
