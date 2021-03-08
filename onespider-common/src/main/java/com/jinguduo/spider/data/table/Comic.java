package com.jinguduo.spider.data.table;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

/**
 * 版权所有：北京金骨朵文化传播有限公司
 *
 * @DATE 31/07/2017 14:20
 */
@Data
@Table(name = "comic")
@Entity
public class Comic implements Serializable {

    private static final long serialVersionUID = -92959955483878422L;

    @Id
    @GeneratedValue
    private Integer id;

    private String code;

    private Integer platformId;

    private String name;

    private String headerImg;

    private Byte sex;

    private String author;

    private String subject;

    private String tags;

    private String intro;

    private Boolean signed;

    private Boolean exclusive;

    private Boolean finished;

    private Integer episode;

    private Date endEpisodeTime;

    private String innerImgUrl;

}
