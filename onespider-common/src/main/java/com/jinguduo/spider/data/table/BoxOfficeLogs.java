package com.jinguduo.spider.data.table;


import lombok.Data;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.sql.Timestamp;

@Entity
@Table(name ="box_office_logs")
@Data
public class BoxOfficeLogs implements Serializable {


    private static final long serialVersionUID = -2648812503425203553L;

    @Id
    @GeneratedValue
    public Long id;

    public String code;

    public Long allBoxOffice;

    public Long todayBoxOffice;

    @CreatedDate
    private Timestamp crawledAt = new Timestamp(System.currentTimeMillis());


}
