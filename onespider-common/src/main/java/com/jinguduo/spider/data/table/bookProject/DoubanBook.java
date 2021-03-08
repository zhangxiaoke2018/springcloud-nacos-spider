package com.jinguduo.spider.data.table.bookProject;

import lombok.Data;

import javax.persistence.*;

/**
 * Created by lc on 2020/1/14
 */
@Data
@Table(name = "douban_book")
@Entity
public class DoubanBook {
    @Id
    @GeneratedValue
    private Integer id;
    private String code;
    private Integer platformId;
    private String url;
    private String isbn;

    private String bookName;
    private Float score;
    private Integer scorePerson;
    @Column(name="score_5_proportion")
    private Float score5Proportion;
    @Column(name="score_4_proportion")
    private Float score4Proportion;
    @Column(name="score_3_proportion")
    private Float score3Proportion;
    @Column(name="score_2_proportion")
    private Float score2Proportion;
    @Column(name="score_1_proportion")
    private Float score1Proportion;
    private Integer comment;
}
