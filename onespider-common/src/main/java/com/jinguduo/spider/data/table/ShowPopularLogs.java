package com.jinguduo.spider.data.table;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by lc on 2018/9/3
 */

@Entity
@Table(name = "show_popular_logs")
@Data
public class ShowPopularLogs implements Serializable {

    @Id
    @GeneratedValue
    private Long id;
    @Column(length = 64)
    private String code;
    private Integer platformId;

    private Long hotCount;


}
