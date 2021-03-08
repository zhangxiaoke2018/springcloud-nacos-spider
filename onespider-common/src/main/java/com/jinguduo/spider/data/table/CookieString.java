package com.jinguduo.spider.data.table;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import lombok.Data;

@Entity
@Table(name = "cookie_strings", indexes = { @Index(name = "idx_domain", columnList = "domain", unique = false) })
@Data
public class CookieString implements Serializable, Comparable<CookieString> {

    private static final long serialVersionUID = -3806770307426809453L;

    @Id
    @GeneratedValue
    private Integer id;
    
    @Column(length = 64)
    private String domain;
    
    @Column(length = 255)
    private String value;
    
    @Column(updatable = false)
    @CreatedDate
    private Timestamp createdAt = new Timestamp(System.currentTimeMillis());
    @Column(updatable = false)
    @LastModifiedDate
    private Timestamp updatedAt = new Timestamp(System.currentTimeMillis());
    
    
    @Override
    public int compareTo(CookieString o) {
        int r = 1;
        if (o == null || o.value == null || o.value.length() == 0) {
            r = -1;
        } else {
            r = this.domain.compareTo(o.value);
        }
        return r;
    }
}
