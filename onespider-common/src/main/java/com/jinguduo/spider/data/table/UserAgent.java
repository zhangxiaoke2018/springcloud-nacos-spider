package com.jinguduo.spider.data.table;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import com.jinguduo.spider.common.constant.UserAgentKind;

import lombok.Data;

@Entity
@Table(name = "user_agents")
@Data
public class UserAgent implements Serializable {

    private static final long serialVersionUID = 4522354071010765565L;

    @Id
    @GeneratedValue
    private Integer id;
    
    @Enumerated(EnumType.ORDINAL)
    private UserAgentKind kind;
    
    @Column(length = 255)
    private String value;
    
    @Column(updatable = false)
    @CreatedDate
    private Timestamp createdAt = new Timestamp(System.currentTimeMillis());
    @Column(updatable = false)
    @LastModifiedDate
    private Timestamp updatedAt = new Timestamp(System.currentTimeMillis());
}
