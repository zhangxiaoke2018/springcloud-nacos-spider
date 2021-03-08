package com.jinguduo.spider.data.table;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;

@Entity
@Table(name = "barrage_logs")
@Data
public class BarrageLog implements Serializable {


    private static final long serialVersionUID = 614153060391986285L;
    @Id
    @GeneratedValue
    private Long id;
    private Integer showId;
    private Integer platformId;
    private Integer seedId;
    private Integer count;
    private Timestamp crawledAt = new Timestamp(System.currentTimeMillis());
    @Column(length = 100)
    private String code;

    public BarrageLog() {
    }

    public BarrageLog(Integer count) {
        this.count = count;
    }


}
