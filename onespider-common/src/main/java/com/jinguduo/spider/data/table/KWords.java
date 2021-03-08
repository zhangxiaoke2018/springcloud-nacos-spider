package com.jinguduo.spider.data.table;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.sql.Timestamp;

@Entity
@Table(name = "media_kwords")
@Data
public class KWords implements Serializable {

    @Id
    @GeneratedValue
    private Integer id;
    private String keyword;
    private Timestamp crawledAt = new Timestamp(System.currentTimeMillis());

    public KWords() {
    }
    public KWords(String keyword) {
        this.keyword = keyword;
    }
}
