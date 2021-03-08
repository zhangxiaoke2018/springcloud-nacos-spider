package com.jinguduo.spider.data.table;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;


@Entity
@Table(name = "core_keyword")
@Data
public class CoreKeyword implements Serializable {

    private static final long serialVersionUID = -6265293896110184876L;

    @Id
    @GeneratedValue
    private Integer id;

    private Integer type;

    private Integer relevanceId;

    private String  keyword;

}
