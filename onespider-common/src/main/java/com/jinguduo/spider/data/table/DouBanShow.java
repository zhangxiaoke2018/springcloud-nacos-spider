package com.jinguduo.spider.data.table;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.List;

/**
 * Created by csonezp on 2016/8/15.
 */
@Entity
@Table(name = "doubanshows", uniqueConstraints = {@UniqueConstraint(columnNames = {"code"})})
@Getter
@Setter
@ToString
public class DouBanShow {
    @Id
    @GeneratedValue
    private Integer id;

    private Integer showId;

    @NotBlank
    private String name;

    private String year;

    private String director;

    private String type;

    private String nickName;

    @Column(name = "code")
    private String code;

    private String url;

    //存放actors的name，以/分隔
    private String actorNames;


    private String cover;

    @Lob
    @Column(length = 4096)
    private String intro;

    private Integer episodes;

    //分钟数
    private String duration;

    private String screenWrite;



//    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
//    @JoinTable(
//            name = "douban_show_actor",
//            joinColumns ={ @JoinColumn(name = "show_id", referencedColumnName = "id")},
//            inverseJoinColumns = @JoinColumn(name = "actor_id", referencedColumnName = "id"))

    @Transient
    private List<DouBanActor> actors;//关系自己维护，不交给jpa

    @CreatedDate
    private Timestamp createdAt = new Timestamp(System.currentTimeMillis());
    @LastModifiedBy
    private Timestamp updatedAt = new Timestamp(System.currentTimeMillis());


}
