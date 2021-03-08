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
@Table(name = "doubanactors")
@Getter
@Setter
@ToString
public class DouBanActor {
    @Id
    @GeneratedValue
    private Integer id;

    private String code;

    @NotBlank
    @Column(unique = true)
    private String name;

    @Column(unique = true)
    private String url;

    private String cover;

    private String weibo;

    private String fansWeibo;

    private String tieba;

    private String sex;

    private String constellation;

    private String birthday;

    private String birthplace;

    private String job;

    //imdb编号
    private String imdbNumber;

    private String otherChineseName;

    private String otherEnglishName;


//    @ManyToMany(mappedBy = "actors", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
//    private List<DouBanShow> shows;

    @CreatedDate
    private Timestamp createdAt = new Timestamp(System.currentTimeMillis());

    @LastModifiedBy
    private Timestamp updatedAt = new Timestamp(System.currentTimeMillis());

    public void setUrl(String url){

        if(url.startsWith("http")){
            this.url = url;
        } else if(url.startsWith("/celebrity")) {
            this.url = "https://movie.douban.com" + url;
        }



    }

}
