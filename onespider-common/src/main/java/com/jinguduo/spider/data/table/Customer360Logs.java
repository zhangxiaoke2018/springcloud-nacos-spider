package com.jinguduo.spider.data.table;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * I'm a cute code dog
 * User: LingChen
 * Date:2017/7/10
 * Time:18:16
 */
@Entity
@Table(name = "customer_360_logs")
@Data
public class Customer360Logs implements Serializable {
    private static final long serialVersionUID = 4772054543557583663L;
    @Id
    @GeneratedValue
    @Column(name = "id")
    private Integer id;

    @Column(name = "code")
    private String code;

    private String province;

    @Column(name = "day")
    private Date day;

    private Byte maleRatio = 0;
    private Byte femaleRatio = 0;

    //18以下
    @Column(name = "age_18_ratio")
    private Byte age18ratio = 0;
    @Column(name = "age_18_male")
    private Byte age18male = 0;
    @Column(name = "age_18_female")
    private Byte age18female = 0;

    //19-24
    @Column(name = "age_24_ratio")
    private Byte age24ratio = 0;
    @Column(name = "age_24_male")
    private Byte age24male = 0;
    @Column(name = "age_24_female")
    private Byte age24female = 0;

    //25-34
    @Column(name = "age_34_ratio")
    private Byte age34ratio = 0;
    @Column(name = "age_34_male")
    private Byte age34male = 0;
    @Column(name = "age_34_female")
    private Byte age34female = 0;

    //35-49
    @Column(name = "age_49_ratio")
    private Byte age49ratio = 0;
    @Column(name = "age_49_male")
    private Byte age49male = 0;
    @Column(name = "age_49_female")
    private Byte age49female = 0;

    //大于50
    @Column(name = "age_50_ratio")
    private Byte age50ratio = 0;
    @Column(name = "age_50_male")
    private Byte age50male = 0;
    @Column(name = "age_50_female")
    private Byte age50female = 0;

    @Column(name = "created_at", updatable = false)
    private Date createdAt;

    @Column(name = "updated_at", updatable = false)
    private Date updatedAt;
}
