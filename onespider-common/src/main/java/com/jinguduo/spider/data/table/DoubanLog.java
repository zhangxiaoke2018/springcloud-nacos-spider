package com.jinguduo.spider.data.table;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.data.annotation.CreatedDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.sql.Timestamp;

@Entity
@Table(name = "douban_logs")
@Data
public class DoubanLog {

    @Id
    @GeneratedValue
    private Integer id;

    @NotBlank
    private String code;
    //评分
    private BigDecimal videoScore;
    //评分人数
    private Integer judgerCount;
    //讨论区数量
    private Integer discussionCount;
    //短评（评论）
    private Integer briefComment;
    //影评量
    private Integer reviewCount;
    //分集短评总量(豆瓣)
    private Integer allPdBriefComment;
    //5星人数
    @Column(name="score_5_num")
    private Integer score5Num;
    //4星人数
    @Column(name="score_4_num")
    private Integer score4Num;
    //3星人数
    @Column(name="score_3_num")
    private Integer score3Num;
    //2星人数
    @Column(name="score_2_num")
    private Integer score2Num;
    //1星人数
    @Column(name="score_1_num")
    private Integer score1Num;



    //创建时间
    @Column(updatable = false)
    @CreatedDate
    private Timestamp crawledAt = new Timestamp(System.currentTimeMillis());

}