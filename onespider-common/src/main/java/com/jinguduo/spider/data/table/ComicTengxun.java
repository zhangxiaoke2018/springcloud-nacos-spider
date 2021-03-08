package com.jinguduo.spider.data.table;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * I'm a cute code dog
 * User: LingChen
 * Date:2017/8/2
 * Time:16:20
 */
@Entity
@Table(name = "comic_tengxun")
@Data
public class ComicTengxun implements Serializable {

    @Id
    @GeneratedValue
    @Column(name = "id")
    private Integer id;

    @Column(name = "code")
    private String code;

    private String comicId;

    @Column(name = "day")
    private Date day;

    @Column(name = "collect_num")
    private Integer collectNum;

    @Column(name = "hot_num")
    private Long hotNum;

    @Column(name = "praise_num")
    private Integer praiseNum;

    @Column(name = "comment_num")
    private Integer commentNum;

    @Column(name = "score_num")
    private Double scoreNum;

    @Column(name = "score_count")
    private Integer scoreCount;

    @Column(name = "month_ticket_num")
    private Integer monthTicketNum;

    @Column(name = "today_ticket_num")
    private Integer todayTicketNum;

    @Column(name = "dashang_num")
    private Integer dashangNum;

    @Column(name = "weekly_ticket_num")
    private Integer weeklyTicketNum;

    @Column(name = "weekly_ticket_rank")
    private Integer weeklyTicketRank;

    @Column(name = "create_time", updatable = false)
    private Date create_time;

    @Column(name = "update_time", updatable = false)
    private Date update_time;


}
