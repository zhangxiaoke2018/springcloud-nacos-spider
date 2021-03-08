package com.jinguduo.spider.data.table;

import java.sql.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.validator.constraints.NotBlank;

import lombok.Data;

@Entity
@Table(name = "maoyan_actors")
@Data
public class MaoyanActor {
    @Id
    @GeneratedValue
    private Integer id;

    private String code;

    @NotBlank
    @Column(unique = true)
    private String name;
    
    private String url;
    
    private String school;
    
    private int height;
    
    private Date birth;
    
    private String nickname;
    
    private String constellation;
    
    private String blood;
    
    private String hometown;
    
    private String ethnic;
    
    private String gender;
    
    private String nationality;
}
